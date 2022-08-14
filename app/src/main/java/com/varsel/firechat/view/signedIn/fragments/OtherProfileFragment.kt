package com.varsel.firechat.view.signedIn.fragments

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentOtherProfileBinding
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.viewModel.FirebaseViewModel

class OtherProfileFragment : Fragment() {
    private var _binding: FragmentOtherProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity
    private val firebaseViewModel: FirebaseViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentOtherProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        parent = activity as SignedinActivity


        val uid = OtherProfileFragmentArgs.fromBundle(requireArguments()).userId

        firebaseViewModel.getUserById(uid, parent.mDbRef, {

        })

        firebaseViewModel.selectedUser.observe(viewLifecycleOwner, Observer {
            Log.d("LLL", "${it?.name}")

            if(it?.occupation != null){
                binding.firstName.setText(it?.name)
                binding.occupation.setText(it?.occupation)
                binding.userProps.visibility = View.VISIBLE
                binding.nameWithoutOccupation.visibility = View.GONE
            } else {
                binding.nameWithoutOccupation.text = it?.name
                binding.nameWithoutOccupation.visibility = View.VISIBLE
                binding.userProps.visibility = View.GONE
            }

            if (it?.about != null){
                binding.aboutTextHeader.setText(it?.name?.let { it1 -> getFirstName(it1) })
                binding.aboutTextBody.setText(truncate(it?.about!!, 150))
                binding.moreAboutClickable.setOnClickListener { it2 ->
                    showAboutActionSheet(getFirstName(it?.name!!), it?.about!!)
                }
                binding.aboutTextBody.visibility = View.VISIBLE
                binding.aboutTextHeader.visibility = View.VISIBLE
                binding.moreAboutClickable.visibility = View.VISIBLE
            } else {
                binding.aboutTextBody.visibility = View.GONE
                binding.aboutTextHeader.visibility = View.GONE
                binding.moreAboutClickable.visibility = View.GONE
            }
        })

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("LLL", "View destroyed")
        _binding = null
    }

    // TODO: Implement show and hide spinner by switching visibilities
    private fun showSpinner(){

    }

    private fun hideSpinner(){

    }

    fun truncate(about: String, length: Int): String{
        if(about.length > length){
            return "${about.subSequence(0, length)}..."
        } else {
            return about
        }
    }

    // TODO: Implement Show About actionsheet
    fun showAboutActionSheet(headerText: String, bodyText: String){
        val dialog = BottomSheetDialog(parent)
        dialog.setContentView(R.layout.action_sheet_about_user)

        val header = dialog.findViewById<TextView>(R.id.dialog_about_header)
        val body = dialog.findViewById<TextView>(R.id.dialog_about_body)

        header?.text = headerText
        body?.text = bodyText

        dialog.show()
    }

    //  TODO: make status bar transparent on this page
    private fun setTransparent(){

    }

    private fun removeTransparent(){

    }

    fun getFirstName(name: String): String{
        val arr = name.split(" ").toTypedArray()
        return "About ${arr[0]}"
    }

}