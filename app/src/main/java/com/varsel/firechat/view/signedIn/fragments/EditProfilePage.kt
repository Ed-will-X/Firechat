package com.varsel.firechat.view.signedIn.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.databinding.ActionSheetEditProfileBinding
import com.varsel.firechat.databinding.FragmentEditProfilePageBinding
import com.varsel.firechat.model.User.User
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.viewModel.FirebaseViewModel

class EditProfilePage : Fragment() {
    private var _binding: FragmentEditProfilePageBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditProfilePageBinding.inflate(layoutInflater, container, false)
        val view = binding.root

        parent = activity as SignedinActivity

        setBindings()


        binding.actionSheetClickable.setOnClickListener {
            openEditProfileActionsheet()
        }

        return view
    }

    private fun openEditProfileActionsheet(){
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = ActionSheetEditProfileBinding.inflate(layoutInflater, this.binding.root, false)
        val view = dialogBinding.root

        setActionsheetBindings(dialogBinding)
        dialog.setContentView(view)

        dialogBinding.editProfileBtn.setOnClickListener {
            editUser(dialogBinding)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setBindings(){
        parent.firebaseViewModel.currentUser.observe(viewLifecycleOwner, Observer {
            binding.emailText.text = it?.email ?: "-"
            binding.nameText.text = it?.name ?: "-"
            binding.locationText.text = it?.location ?: "-"
            binding.phoneText.text = it?.phone ?: "-"
            binding.occupationText.text = it?.occupation ?: "-"
            binding.genderText.text = it?.gender ?: "-"
        })
    }

    private fun setActionsheetBindings(dialogBinding: ActionSheetEditProfileBinding){
        val firebaseViewModel: FirebaseViewModel = parent.firebaseViewModel

        dialogBinding.nameEditText.setText(firebaseViewModel.currentUser.value?.name)
        dialogBinding.emailEditText.setText(firebaseViewModel.currentUser.value?.email)

        if(firebaseViewModel.currentUser.value?.phone == null){
            dialogBinding.phoneEditText.setText("+")
        } else {
            dialogBinding.phoneEditText.setText(firebaseViewModel.currentUser.value?.phone)
        }

        dialogBinding.occupationEditText.setText(firebaseViewModel.currentUser.value?.occupation)
        dialogBinding.locationEditText.setText(firebaseViewModel.currentUser.value?.location)
        dialogBinding.aboutEditText.setText(firebaseViewModel.currentUser.value?.about)
    }

    private fun editUser(dialogBinding: ActionSheetEditProfileBinding){
        parent.firebaseViewModel.editUser("name",dialogBinding.nameEditText.text.toString(), parent.firebaseAuth, parent.mDbRef)
        if(dialogBinding.emailEditText.text.isNotEmpty()){
            parent.firebaseViewModel.editUser("email",dialogBinding.emailEditText.text.toString(), parent.firebaseAuth, parent.mDbRef)
        }
        parent.firebaseViewModel.editUser("about",dialogBinding.aboutEditText.text.toString(), parent.firebaseAuth, parent.mDbRef)

        if(binding.phoneText.text.length in 8..14){
            parent.firebaseViewModel.editUser("phone",dialogBinding.phoneEditText.text.toString(), parent.firebaseAuth, parent.mDbRef)
        }
        parent.firebaseViewModel.editUser("occupation",dialogBinding.occupationEditText.text.toString(), parent.firebaseAuth, parent.mDbRef)
        parent.firebaseViewModel.editUser("location",dialogBinding.locationEditText.text.toString(), parent.firebaseAuth, parent.mDbRef)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}