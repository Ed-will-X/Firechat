package com.varsel.firechat.view.signedIn.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.varsel.firechat.databinding.FragmentAddFriendsBinding
import com.varsel.firechat.model.User.User
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.utils.LifecycleUtils
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.view.signedIn.adapters.AddFriendsSearchAdapter
import com.varsel.firechat.view.signedIn.adapters.RecentSearchAdapter
import com.varsel.firechat.viewModel.AddFriendsViewModel
import com.varsel.firechat.viewModel.FirebaseViewModel


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

        LifecycleUtils.observeInternetStatus(parent.firebaseViewModel, this, {
            binding.addFriendsSearchBox.isEnabled = true
        }, {
            binding.addFriendsSearchBox.isEnabled = false
        })

        setupRecentSearchAdapter()

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

    private fun navigateToOtherProfileFragment(user: User) {
        parent.firebaseViewModel.selectedUser.value = user

        val action = AddFriendsFragmentDirections.actionAddFriendsToOtherProfileFragment(user.userUID)
        view?.findNavController()?.navigate(action)
    }

    private fun setupRecentSearchAdapter() {
        val recentSearches = parent.firebaseViewModel.currentUser.value?.recent_search
        recentSearchAdapter = RecentSearchAdapter(parent, this) {
            navigateToOtherProfileFragment(it)

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
        // TODO: Sort by keys
        val positioned = searches.toSortedMap()
        val sorted = positioned.toList()
            .sortedBy { (key, value) -> value }
            .toMap()

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