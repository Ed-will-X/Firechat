package com.varsel.firechat.presentation.signedIn.fragments.screens.add_friends

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.varsel.firechat.databinding.FragmentAddFriendsBinding
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.utils.ExtensionFunctions.Companion.showKeyboard
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.adapters.AddFriendsSearchAdapter
import com.varsel.firechat.presentation.signedIn.adapters.RecentSearchAdapter
import com.varsel.firechat.utils.ExtensionFunctions.Companion.collectLatestLifecycleFlow
import com.varsel.firechat.common._utils.UserUtils
import com.varsel.firechat.domain.use_case.current_user.CheckServerConnectionUseCase
import com.varsel.firechat.domain.use_case.profile_image.DisplayProfileImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.lang.IllegalArgumentException
import javax.inject.Inject

@AndroidEntryPoint
class AddFriendsFragment : Fragment() {
    private var _binding: FragmentAddFriendsBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity
    private lateinit var viewModel: AddFriendsViewModel
    private var timer: CountDownTimer? = null
    private lateinit var recentSearchAdapter: RecentSearchAdapter
    private lateinit var addFriendsSearchAdapter: AddFriendsSearchAdapter

    @Inject
    lateinit var displayProfileImage: DisplayProfileImage

    @Inject
    lateinit var checkServerConnection: CheckServerConnectionUseCase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddFriendsBinding.inflate(inflater, container, false)
        val view = binding.root
        parent = activity as SignedinActivity

        viewModel = ViewModelProvider(this).get(AddFriendsViewModel::class.java)

//        LifecycleUtils.observeInternetStatus(parent, this, {
//            binding.addFriendsSearchBox.isEnabled = true
//        }, {
//            binding.addFriendsSearchBox.isEnabled = false
//        })

        checkServerConnection().onEach {
            try {
                binding.addFriendsSearchBox.isEnabled = it
            } catch (e: Exception) { }
        }.launchIn(lifecycleScope)

        setupRecentSearchAdapter()

        this.showKeyboard()

        collectFlowsFromViewModel()
        setupAddFriendsSearchAdapter()
        setClickListeners()
//        searchBar()

        return view
    }

    private fun collectFlowsFromViewModel() {
        collectLatestLifecycleFlow(viewModel.state) {
            toggleNotFoundIconVisibility(it.users.isEmpty() && it.textCount > 1 && !it.isLoading)
            toggleRecentSearchVisibility(it.textCount < 2)
            showLoadingSpinner(it.isLoading)
            toggleCancelIconVisibility(it.textCount > 1)
            if(it.users.isNotEmpty()) {
                submitToResultsAdapter(it.users)
            }
        }
    }

    private fun setClickListeners() {
        binding.addFriendsSearchBox.doAfterTextChanged {
            viewModel.onSearch(binding.addFriendsSearchBox.text.toString())
        }

        binding.addFriendsBackButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.addFriendsCancelButton.setOnClickListener {
            clearInput()
        }
    }

    private fun showKeyboard(){
        binding.addFriendsSearchBox.requestFocus()
        requireContext().showKeyboard()
    }

    private fun navigateToOtherProfileFragment(user: User, base64: String?) {
        try {
            val action = AddFriendsFragmentDirections.actionAddFriendsToOtherProfileFragment(user.userUID)

            parent.firebaseViewModel.addToRecentSearch(user.userUID, parent.firebaseAuth, parent.mDbRef)
            parent.profileImageViewModel.selectedOtherUserProfilePic.value = base64

            parent.hideKeyboard()
            view?.findNavController()?.navigate(action)

//            parent.firebaseViewModel.selectedUser.value = user

        } catch (e: IllegalArgumentException){

        }
    }

    private fun setupRecentSearchAdapter() {
        val recentSearches = viewModel.getCurrentUserRecurrentUseCase().value.data?.recent_search
        recentSearchAdapter = RecentSearchAdapter(parent, this) {
            navigateToOtherProfileFragment(it, null)

        }

        addRecyclerViewSeparator()
        if (recentSearches != null){
            submitToRecentSearch(recentSearches)
        } else {
            // TODO: Show no previous search UX thingy
        }

        binding.recentSearchRecyclerView.adapter = recentSearchAdapter
    }

    private fun setupAddFriendsSearchAdapter() {
        addFriendsSearchAdapter = AddFriendsSearchAdapter(parent, this, viewModel, { user, base64 ->

            navigateToOtherProfileFragment(user, base64)

        }, { profileImage, user ->
            displayProfileImage(profileImage, user, parent)
            parent.hideKeyboard()
        })

        binding.searchRecyclerView.adapter = addFriendsSearchAdapter
    }

    private fun addRecyclerViewSeparator() {
        binding.recentSearchRecyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun submitToRecentSearch(searches: HashMap<String, Long>){
        val positioned = searches.toSortedMap()
        val sorted = UserUtils.sortByTimestamp(positioned)

        recentSearchAdapter.recentSearches = sorted.keys.toList().reversed()
        recentSearchAdapter.notifyDataSetChanged()
    }

    private fun submitToResultsAdapter(results: List<User>) {
        addFriendsSearchAdapter.run {
            users = results as ArrayList<User>
            notifyDataSetChanged()
        }
    }

    private fun toggleCancelIconVisibility(shouldShow: Boolean){
        if (shouldShow){
            binding.addFriendsCancelButton.visibility = View.VISIBLE
        } else {
            binding.addFriendsCancelButton.visibility = View.GONE
        }
    }

    private fun toggleNotFoundIconVisibility(notFound: Boolean){
        if(notFound){
            binding.searchRecyclerView.visibility = View.GONE
            binding.notFound.visibility = View.VISIBLE
        } else {
            binding.searchRecyclerView.visibility = View.VISIBLE
            binding.notFound.visibility = View.GONE
        }
    }

    private fun toggleRecentSearchVisibility(shouldShow: Boolean){
        if(!shouldShow){
            binding.recentSearch.visibility = View.GONE
            binding.searchRecyclerView.visibility = View.VISIBLE
        } else {
            binding.recentSearch.visibility = View.VISIBLE
            binding.searchRecyclerView.visibility = View.GONE
        }
    }

    private fun showLoadingSpinner(shouldShow: Boolean) {
        // TODO: Implement show loading spinner
    }

    private fun clearInput(){
        binding.addFriendsSearchBox.setText("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}