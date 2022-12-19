package com.varsel.firechat.presentation.signedIn.fragments.screens.add_friends

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.varsel.firechat.databinding.FragmentAddFriendsBinding
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.utils.ExtensionFunctions.Companion.showKeyboard
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.utils.LifecycleUtils
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.adapters.AddFriendsSearchAdapter
import com.varsel.firechat.presentation.signedIn.adapters.RecentSearchAdapter
import com.varsel.firechat.presentation.viewModel.AddFriendsViewModel
import com.varsel.firechat.utils.UserUtils
import java.lang.IllegalArgumentException


class AddFriendsFragment : Fragment() {
    private var _binding: FragmentAddFriendsBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity
    private val viewModel: AddFriendsViewModel by activityViewModels()
    private var timer: CountDownTimer? = null
    private lateinit var recentSearchAdapter: RecentSearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddFriendsBinding.inflate(inflater, container, false)
        val view = binding.root
        parent = activity as SignedinActivity

        LifecycleUtils.observeInternetStatus(parent, this, {
            binding.addFriendsSearchBox.isEnabled = true
        }, {
            binding.addFriendsSearchBox.isEnabled = false
        })

        setupRecentSearchAdapter()

        this.showKeyboard()

        val friendsSearchAdapter = AddFriendsSearchAdapter(parent, { id, user, base64 ->
            parent.firebaseViewModel.addToRecentSearch(id, parent.firebaseAuth, parent.mDbRef)

            parent.hideKeyboard()

            parent.profileImageViewModel.selectedOtherUserProfilePic.value = base64

            navigateToOtherProfileFragment(user)
        }, { profileImage, user ->
            ImageUtils.displayProfilePicture(profileImage, user, parent)
            parent.hideKeyboard()
        })

        binding.searchRecyclerView.adapter = friendsSearchAdapter

        parent.firebaseViewModel.usersQuery.observe(viewLifecycleOwner, Observer {
            toggleNotFoundIconVisibility(it)
            toggleRecentSearchVisibility(binding.addFriendsSearchBox)
            friendsSearchAdapter.run {
                users = it as ArrayList<User>
                notifyDataSetChanged()
            }
        })

        toggleCancelIconVisibility()
        backButton()
        cancelButton()
        searchBar()

        return view
    }

    private fun showKeyboard(){
        binding.addFriendsSearchBox.requestFocus()
        requireContext().showKeyboard()
    }

    private fun navigateToOtherProfileFragment(user: User) {
        try {
            val action = AddFriendsFragmentDirections.actionAddFriendsToOtherProfileFragment(user.userUID)

            view?.findNavController()?.navigate(action)
            parent.firebaseViewModel.selectedUser.value = user

        } catch (e: IllegalArgumentException){

        }
    }

    private fun setupRecentSearchAdapter() {
        val recentSearches = parent.firebaseViewModel.currentUser.value?.recent_search
        recentSearchAdapter = RecentSearchAdapter(parent, this) {
            navigateToOtherProfileFragment(it)

            parent.hideKeyboard()
            parent.firebaseViewModel.addToRecentSearch(it.userUID, parent.firebaseAuth, parent.mDbRef)

        }

        addRecyclerViewSeparator()
        if (recentSearches != null){
            submitToRecentSearch(recentSearches)
        } else {
            // TODO: Show no previous search UX thingy
        }

        binding.recentSearchRecyclerView.adapter = recentSearchAdapter
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

    private fun toggleCancelIconVisibility(){
        binding.addFriendsSearchBox.doAfterTextChanged {
            if (it.toString().isNotEmpty()){
                binding.addFriendsCancelButton.visibility = View.VISIBLE
            } else {
                binding.addFriendsCancelButton.visibility = View.GONE
            }
        }
    }

    private fun toggleNotFoundIconVisibility(users: List<User>){
        if(users.isEmpty() && binding.addFriendsSearchBox.text.isNotEmpty()){
            binding.searchRecyclerView.visibility = View.GONE
            binding.notFound.visibility = View.VISIBLE
        } else {
            binding.searchRecyclerView.visibility = View.VISIBLE
            binding.notFound.visibility = View.GONE
        }
    }

    private fun backButton(){
        binding.addFriendsBackButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun cancelButton(){
        binding.addFriendsCancelButton.setOnClickListener {
            clearInput()
        }
    }

    private fun searchBar(){
        viewModel.shouldRun.observe(viewLifecycleOwner, Observer { shouldRun ->
            binding.addFriendsSearchBox.doAfterTextChanged {
                val text = binding.addFriendsSearchBox.text.toString()

                if(timer != null){
                    timer?.cancel()
                }

                timer = viewModel.debounce {
                    parent.firebaseViewModel.queryUsers(text, parent.mDbRef, parent.firebaseAuth, {

                    }, {})
                }
            }
        })
    }

    private fun toggleRecentSearchVisibility(editText: EditText) {
        if(editText.text.toString().length > 1){
            binding.recentSearch.visibility = View.GONE
            binding.searchRecyclerView.visibility = View.VISIBLE
        } else {
            binding.recentSearch.visibility = View.VISIBLE
            binding.searchRecyclerView.visibility = View.GONE
        }
    }

    private fun clearInput(){
        binding.addFriendsSearchBox.setText("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}