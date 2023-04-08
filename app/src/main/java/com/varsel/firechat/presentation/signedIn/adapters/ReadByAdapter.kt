package com.varsel.firechat.presentation.signedIn.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.varsel.firechat.R
import com.varsel.firechat.common.Resource
import com.varsel.firechat.data.local.Message.Message
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.databinding.ViewHolderReadByBinding
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.SignedinViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ReadByAdapter(
    val message: Message,
    val fragment: Fragment,
    val viewModel: SignedinViewModel,
    val activity: SignedinActivity,
    val parentClick: (user: User)-> Unit
) : RecyclerView.Adapter<ReadByAdapter.ReadByViewHolder>() {
    var items = listOf<Pair<String, Long>>()
    class ReadByViewHolder(binding: ViewHolderReadByBinding): RecyclerView.ViewHolder(binding.root) {
        val parent = binding.root
        val profileImageParent = binding.profileImageParent
        val profileImage = binding.profileImage
        val name = binding.name
        val time = binding.time
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadByViewHolder {
        val binding = ViewHolderReadByBinding.inflate(fragment.layoutInflater, parent, false)

        return ReadByAdapter.ReadByViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReadByViewHolder, position: Int) {
        val item = items[position]

        getUser(item.first) { user ->
            fragment.lifecycleScope.launch {
                setProfilePic(user, holder)

                holder.name.setText(user.name)
                holder.time.setText(viewModel.formatStampMessageDetail(message.time))
            }

            holder.parent.setOnClickListener {
                parentClick(user)
            }
        }


    }

    private fun getUser(userId: String, successCallback: (user: User)-> Unit) {
        viewModel.getOtherUserSingle(userId).onEach {
            when(it) {
                is Resource.Success -> {
                    if(it.data != null) {
                        successCallback(it.data)
                    } else {
                        // TODO: Show error thingy
                    }
                }
                is Resource.Loading -> {
                    // TODO: Show loading thingy
                }
                is Resource.Error -> {
                    // TODO: Show error thingy
                }
            }
        }.launchIn(fragment.lifecycleScope)
    }

    private suspend fun setProfilePic(user: User, holder: ReadByViewHolder) {
        viewModel.getOtherUserProfileImageUseCase(user).onEach {
            if(it?.image != null) {
                viewModel.setProfilePicUseCase(it.image!!, holder.profileImage, holder.profileImageParent, activity)
            } else {
                holder.profileImageParent.visibility = View.GONE
            }
        }.launchIn(fragment.lifecycleScope)
    }

    override fun getItemCount(): Int {
        return items.count()
    }
}