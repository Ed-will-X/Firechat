package com.varsel.firechat.view.signedIn.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.varsel.firechat.R
import com.varsel.firechat.databinding.RecentSearchItemBinding
import com.varsel.firechat.model.User.User
import com.varsel.firechat.utils.UserUtils
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.view.signedIn.fragments.AddFriendsFragment

class RecentSearchAdapter(
    val activity: SignedinActivity,
    val fragment: AddFriendsFragment,
    val parentClickListener: (user: User)-> Unit
): RecyclerView.Adapter<RecentSearchAdapter.RecentSearchItem>() {
    var recentSearches = listOf<String>()

    class RecentSearchItem(binding: RecentSearchItemBinding) : RecyclerView.ViewHolder(binding.root){
        val parent = binding.parent
        val name = binding.name
        val occupation = binding.occupation
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentSearchItem {
        var binding = RecentSearchItemBinding.inflate(fragment.layoutInflater, parent, false)

        return RecentSearchItem(binding)
    }

    override fun onBindViewHolder(holder: RecentSearchItem, position: Int) {
        val item = recentSearches[position]

        UserUtils.getUser(item, activity) {
            holder.name.text = it.name
            holder.occupation.text = it.occupation
            holder.parent.setOnClickListener { it2 ->
                parentClickListener(it)
            }
        }

    }

    override fun getItemCount(): Int {
        return recentSearches.count()
    }
}