package com.varsel.firechat.view.signedIn.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentFriendListBinding
import com.varsel.firechat.model.User.User
import com.varsel.firechat.utils.ExtensionFunctions.Companion.hideKeyboard
import com.varsel.firechat.utils.ExtensionFunctions.Companion.showKeyboard
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.utils.UserUtils
import com.varsel.firechat.utils.gestures.FriendsSwipeGesture
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.view.signedIn.adapters.FriendListAdapter
import com.varsel.firechat.viewModel.FriendListFragmentViewModel

class FriendListFragment : Fragment() {
    private var _binding: FragmentFriendListBinding? = null
    private val binding get() = _binding!!
    private var adapter: FriendListAdapter? = null
    private lateinit var parent: SignedinActivity
    private val viewModel: FriendListFragmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFriendListBinding.inflate(layoutInflater, container, false)
        val view = binding.root

        parent = activity as SignedinActivity

        setupSearchBar()

        binding.addFriendsClickable.setOnClickListener {
            navigateToAddfriends()
        }

        adapter = FriendListAdapter(parent, { id, user, base64 ->
            parent.firebaseViewModel.selectedUser.value = user
            parent.profileImageViewModel.selectedOtherUserProfilePic.value = base64

            val action = FriendListFragmentDirections.actionFriendListFragmentToOtherProfileFragment(id)
            view.findNavController().navigate(action)
        }, { profileImage, user ->
            ImageUtils.displayProfilePicture(profileImage, user, parent)
        })
        binding.allFriendsRecyclerView.adapter = adapter

        val swipeGesture = object : FriendsSwipeGesture(parent){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if(direction == ItemTouchHelper.LEFT){
                    Log.d("LLL", "Unfriend swiped")
                    if(adapter != null){
                        parent.firebaseViewModel.unfriendUser(adapter!!.friends[viewHolder.adapterPosition], parent.firebaseAuth, parent.mDbRef)
                        removeFromAdapter(adapter!!, viewHolder)
                    }
                }
            }
        }

        val touchHelper = ItemTouchHelper(swipeGesture)
        touchHelper.attachToRecyclerView(binding.allFriendsRecyclerView)

        val friends = parent.firebaseViewModel.friends.value
        if(friends != null && friends.isNotEmpty()){
            val sorted = UserUtils.sortUsersByName(friends)

            setFriendCount(friends)

            if(adapter != null){
                adapter!!.friends.addAll(sorted as ArrayList<User>)
                adapter!!.notifyDataSetChanged()
            }
        } else {
            setFriendCount(0)

        }

        binding.backButton.setOnClickListener {
            popNavigation()
        }


        return view
    }

    private fun navigateToAddfriends(){
        val action = FriendListFragmentDirections.actionFriendListFragmentToAddFriends()
        findNavController().navigate(action)
    }

    private fun checkIfEmptyFriends(friends: List<User?>?){
        if(friends?.isNotEmpty() == true){
            binding.noFriends.visibility = View.GONE
            binding.allFriendsRecyclerView.visibility = View.VISIBLE
        } else {
            binding.noFriends.visibility = View.VISIBLE
            binding.allFriendsRecyclerView.visibility = View.GONE
        }
    }

    private fun setupSearchBar(){
        // resets the searchbar visibility
        viewModel.setSearchBarVisibility(false)

        binding.cancelButton.setOnClickListener {
            binding.searchBox.setText("")
        }

        // Actual search code
        binding.searchBox.doAfterTextChanged {
            val friends = parent.firebaseViewModel.friends.value as ArrayList<User>

            if(it != null){
                searchRecyclerView(friends, it)
            }
        }

        parent.firebaseViewModel.friends.observe(viewLifecycleOwner, Observer {
            checkIfEmptyFriends(it)
        })
        // Sets the visibilities of the search bar and the soft keyboard
        viewModel.isSearchBarVisible.observe(viewLifecycleOwner, Observer {
            if(it){
                binding.searchBar.visibility = View.VISIBLE
                binding.searchBox.requestFocus()
                requireContext().showKeyboard()
            } else {
                binding.searchBar.visibility = View.GONE
                hideKeyboard()
            }
        })

        binding.searchIcon.setOnClickListener {
            viewModel.toggleSearchBarVisible()
        }

        toggleCancelIconVisibility()
    }

    private fun toggleCancelIconVisibility(){
        binding.searchBox.doAfterTextChanged {
            if (it.toString().isNotEmpty()){
                binding.cancelButton.visibility = View.VISIBLE
            } else {
                binding.cancelButton.visibility = View.GONE
            }
        }
    }

    private fun searchRecyclerView(friends: ArrayList<User>, it: Editable){
        Log.d("LLL", "Friend count: ${friends.count()}")
        if(it.toString().isEmpty()){
            // Text box is empty
            submitListToAdapter(friends)
        } else {
            val term = binding.searchBox.text.toString()
            val results = UserUtils.searchListOfUsers(term, friends)

            if(results.isNotEmpty()){
                // There are matches
                submitListToAdapter(results)
            } else {
                // No match is found
                submitListToAdapter(arrayListOf())
                binding.noMatch.visibility = View.VISIBLE
                binding.allFriendsRecyclerView.visibility = View.GONE
            }
        }
    }

    private fun setFriendCount(friends: List<User?>){
        if(friends.size == 1){
            binding.friendCount.text = getString(R.string.people_count_one)
        } else {
            binding.friendCount.text = getString(R.string.people_count, friends.size)
        }
    }

    private fun setFriendCount(count: Int){
        if(count == 1){
            binding.friendCount.text = getString(R.string.people_count_one)
        } else {
            binding.friendCount.text = getString(R.string.people_count, count)
        }
    }

    private fun removeFromAdapter(adapter: FriendListAdapter, viewHolder: RecyclerView.ViewHolder){
        val currentList = adapter.friends.toMutableList()
        currentList.removeAt(viewHolder.absoluteAdapterPosition)
        adapter.friends = currentList as ArrayList<User>

        setFriendCount(currentList)

        adapter.notifyDataSetChanged()
    }

    private fun submitListToAdapter(list: ArrayList<User>){
        adapter?.friends = list

        setFriendCount(list)
        binding.allFriendsRecyclerView.visibility = View.VISIBLE
        binding.noMatch.visibility = View.GONE
        binding.noFriends.visibility = View.GONE


        adapter?.notifyDataSetChanged()
    }

    private fun popNavigation(){
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}