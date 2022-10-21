package com.varsel.firechat.view.signedIn.fragments

import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.varsel.firechat.databinding.FragmentAddFriendsBinding
import com.varsel.firechat.model.User.User
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.view.signedIn.adapters.AddFriendsSearchAdapter
import com.varsel.firechat.viewModel.AddFriendsViewModel
import com.varsel.firechat.viewModel.FirebaseViewModel

class AddFriendsFragment : Fragment() {
    private var _binding: FragmentAddFriendsBinding? = null
    private val binding get() = _binding!!
    private val firebaseViewModel: FirebaseViewModel by activityViewModels()
    private lateinit var parent: SignedinActivity
    private val viewModel: AddFriendsViewModel by activityViewModels()
    private var timer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddFriendsBinding.inflate(inflater, container, false)
        val view = binding.root
        parent = activity as SignedinActivity

        val friendsSearchAdapter = AddFriendsSearchAdapter(parent) { id, user, base64 ->
            parent.firebaseViewModel.selectedUser.value = user
            parent.profileImageViewModel.selectedOtherUserProfilePic.value = base64

            val action = AddFriendsFragmentDirections.actionAddFriendsToOtherProfileFragment(id)
            view.findNavController().navigate(action)
        }

        firebaseViewModel.getAllUsers(parent.mDbRef, parent.firebaseAuth, {
            friendsSearchAdapter.users.clear()
        })

        binding.searchRecyclerView.adapter = friendsSearchAdapter

        parent.firebaseViewModel.usersQuery.observe(viewLifecycleOwner, Observer {
            toggleNotFoundIconVisibility(it)
            friendsSearchAdapter.run {
                friendsSearchAdapter.users = it as ArrayList<User>
                friendsSearchAdapter.notifyDataSetChanged()
            }
        })

        toggleCancelIconVisibility()
        backButton()
        cancelButton()
        searchBar()

        return view
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
                if(timer != null){
                    timer?.cancel()
                }

                timer = viewModel.debounce {
                    parent.firebaseViewModel.queryUsers(binding.addFriendsSearchBox.text.toString(), parent.mDbRef, parent.firebaseAuth, {

                    }, {})
                }
            }
        })
    }

    private fun clearInput(){
        binding.addFriendsSearchBox.setText("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}