package com.varsel.firechat.view.signedIn.fragments.viewPager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentGroupBinding
import com.varsel.firechat.view.signedIn.SignedinActivity

class GroupFragment : Fragment() {
    private var _binding: FragmentGroupBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGroupBinding.inflate(layoutInflater, container, false)
        val view = binding.root

        parent = activity as SignedinActivity

        toggleVisibility()

        binding.createGroupClickable.setOnClickListener {
            view.findNavController().navigate(R.id.action_chatsFragment_to_createGroupFragment)
        }

        return view
    }

    private fun toggleVisibility(){
        parent.firebaseViewModel.groupRooms.observe(viewLifecycleOwner, Observer {
            if(it.isNotEmpty()){
                binding.noGroups.visibility = View.GONE
            } else {
                binding.noGroups.visibility = View.VISIBLE

            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}