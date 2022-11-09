package com.varsel.firechat.view.signedIn.fragments

import android.app.Activity
import android.os.Bundle
import android.os.CountDownTimer
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
import com.varsel.firechat.databinding.FragmentAddFriendsBinding
import com.varsel.firechat.model.User.User
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.utils.LifecycleUtils
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

        LifecycleUtils.observeInternetStatus(parent.firebaseViewModel, this, {
            binding.addFriendsSearchBox.isEnabled = true
        }, {
            binding.addFriendsSearchBox.isEnabled = false
        })

        val friendsSearchAdapter = AddFriendsSearchAdapter(parent, { id, user, base64 ->
            hideKeyboard(parent)

            parent.firebaseViewModel.selectedUser.value = user
            parent.profileImageViewModel.selectedOtherUserProfilePic.value = base64

            val action = AddFriendsFragmentDirections.actionAddFriendsToOtherProfileFragment(id)
            view.findNavController().navigate(action)
        }, { profileImage, user ->
            ImageUtils.displayProfilePicture(profileImage, user, parent)
            hideKeyboard(parent)
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

    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
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