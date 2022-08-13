package com.varsel.firechat.view.signedIn.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentAddFriendsBinding
import com.varsel.firechat.databinding.FragmentProfileBinding
import com.varsel.firechat.model.User.User
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.view.signedIn.adapters.AddFriendsSearchAdapter
import com.varsel.firechat.viewModel.AppbarTag
import com.varsel.firechat.viewModel.AppbarViewModel
import com.varsel.firechat.viewModel.FirebaseViewModel

class AddFriendsFragment : Fragment() {
    private var _binding: FragmentAddFriendsBinding? = null
    private val binding get() = _binding!!
    private val appbarViewModel: AppbarViewModel by activityViewModels()
    private val firebaseViewModel: FirebaseViewModel by activityViewModels()
    lateinit var parent: SignedinActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddFriendsBinding.inflate(inflater, container, false)
        val view = binding.root
        parent = activity as SignedinActivity

        val friendsSearchAdapter = AddFriendsSearchAdapter {

        }

        firebaseViewModel.getAllUsers(parent.mDbRef, parent.firebaseAuth) {
            friendsSearchAdapter.users.clear()
        }

        binding.searchRecyclerView.adapter = friendsSearchAdapter

        binding.addFriendsBackButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.addFriendsCancelButton.setOnClickListener {
            clearInput()
        }

        firebaseViewModel.usersLiveData.observe(viewLifecycleOwner, Observer {
            friendsSearchAdapter.run {
                friendsSearchAdapter.users = it as ArrayList<User>
                friendsSearchAdapter.notifyDataSetChanged()
            }
        })

        binding.addFriendsSearchBox.doAfterTextChanged {
            if (it.toString().isNotEmpty()){
                binding.addFriendsCancelButton.visibility = View.VISIBLE
            } else {
                binding.addFriendsCancelButton.visibility = View.GONE
            }
        }

        appbarViewModel.setPage(AppbarTag.ADD_FRIENDS)
        appbarViewModel.setNavProps(activity, context, view)

        return view
    }

    private fun clearInput(){
        binding.addFriendsSearchBox.setText("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}