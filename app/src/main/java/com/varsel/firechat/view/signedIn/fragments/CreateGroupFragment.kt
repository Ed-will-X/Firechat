package com.varsel.firechat.view.signedIn.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentCreateGroupBinding
import com.varsel.firechat.model.User.User
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.view.signedIn.adapters.CreateGroupAdapter

class CreateGroupFragment : Fragment() {
    private var _binding: FragmentCreateGroupBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCreateGroupBinding.inflate(layoutInflater, container, false)
        val view = binding.root
        parent = activity as SignedinActivity

        val adapter = CreateGroupAdapter()
        binding.friendsRecyclerView.adapter = adapter

        if(parent.firebaseViewModel.friends.value?.isNotEmpty() == true){
            adapter.friends = parent.firebaseViewModel.friends.value as ArrayList<User?>
            adapter.notifyDataSetChanged()
        } else {

        }

        binding.createGroupBtn.setOnClickListener {
            showActionsheet()
        }

        return view
    }

    private fun showActionsheet(){
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(R.layout.actionsheet_create_group)
        val groupName = dialog.findViewById<EditText>(R.id.group_name)
        val btn = dialog.findViewById<Button>(R.id.btn_create_group)

        groupName?.doAfterTextChanged {
            btn?.isEnabled = !groupName.text.isEmpty()
        }

        dialog.show()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}