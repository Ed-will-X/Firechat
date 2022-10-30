package com.varsel.firechat.view.signedIn.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.varsel.firechat.databinding.FragmentProfileImageBinding
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.utils.MessageUtils
import com.varsel.firechat.view.signedIn.SignedinActivity

class ProfileImageFragment : Fragment() {
    var _binding: FragmentProfileImageBinding? = null
    val binding get() = _binding!!
    lateinit var name: String
    var type: String? = null
    var timestamp: Long = 0L
    lateinit var parent: SignedinActivity
    lateinit var image: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileImageBinding.inflate(layoutInflater, container, false)
        val view = binding.root

        parent = activity as SignedinActivity

        name = ProfileImageFragmentArgs.fromBundle(requireArguments()).userName
        type = ProfileImageFragmentArgs.fromBundle(requireArguments()).type
        timestamp = ProfileImageFragmentArgs.fromBundle(requireArguments()).timestamp
        image = ProfileImageFragmentArgs.fromBundle(requireArguments()).image

//        parent.imageViewModel.imageToExpand.observe(viewLifecycleOwner, Observer {
//            if(it != null){
//                setImageToDisplay(it)
//            }
//        })
        setBindings()

        return view
    }

    private fun popNavigation(){
        findNavController().navigateUp()
    }

    private fun setBindings(){
        binding.backBtn.setOnClickListener {
            popNavigation()
        }
        binding.type.text = type
        binding.timestamp.text = MessageUtils.formatStampMessage(timestamp.toString())
        binding.userName.text = name
        setImage()

        binding.moreBtn.setOnClickListener {
            showOptionsActionsheet()
        }
    }

    private fun setImage(){
        val bitmap = ImageUtils.base64ToBitmap(image)
        binding.image.setImageBitmap(bitmap)
    }

    private fun setImageToDisplay(image: String){
        val bitmap = ImageUtils.base64ToBitmap(image)
        binding.image.setImageBitmap(bitmap)
    }

    private fun showOptionsActionsheet(){

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}