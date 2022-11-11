package com.varsel.firechat.view.signedIn.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentFriendListBinding
import com.varsel.firechat.model.User.User
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.utils.UserUtils
import com.varsel.firechat.utils.gestures.FriendsSwipeGesture
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.view.signedIn.adapters.FriendListAdapter
import com.varsel.firechat.view.signedIn.adapters.FriendRequestsAdapter

class FriendListFragment : Fragment() {
    private var _binding: FragmentFriendListBinding? = null
    private val binding get() = _binding!!
    private var adapter: FriendListAdapter? = null
    private lateinit var parent: SignedinActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFriendListBinding.inflate(layoutInflater, container, false)
        val view = binding.root

        parent = activity as SignedinActivity

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
        if(friends != null){
            val sorted = UserUtils.sortUsersByName(friends)

            setFriendCount(friends)

            if(adapter != null){
                adapter!!.friends.addAll(sorted as ArrayList<User>)
                adapter!!.notifyDataSetChanged()
            }
        }

        binding.backButton.setOnClickListener {
            popNavigation()
        }


        return view
    }

    private fun setFriendCount(friends: List<User?>){
        if(friends.size == 1){
            binding.friendCount.text = getString(R.string.people_count_one)
        } else {
            binding.friendCount.text = getString(R.string.people_count, friends.size)
        }
    }

    private fun removeFromAdapter(adapter: FriendListAdapter, viewHolder: RecyclerView.ViewHolder){
        val currentList = adapter.friends.toMutableList()
        currentList.removeAt(viewHolder.absoluteAdapterPosition)
        adapter.friends = currentList as ArrayList<User>

        setFriendCount(currentList)

        adapter.notifyDataSetChanged()
    }

    private fun popNavigation(){
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}