package com.varsel.firechat.view.signedIn.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.varsel.firechat.R
import com.varsel.firechat.model.PublicPost.PublicPost
import com.varsel.firechat.model.PublicPost.PublicPostType
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.view.signedIn.SignedinActivity

class PublicPostAdapter(
    val activity: SignedinActivity,
    val postClickListener: (publicPost: PublicPost) -> Unit
): ListAdapter<PublicPost, RecyclerView.ViewHolder>(PublicPostDiffCallback()) {
    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.findViewById<ImageView>(R.id.public_post_image_view)
    }

    class VideoViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    }

    class AudioViewHolder(itemView: View): RecyclerView.ViewHolder (itemView){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == PublicPostType.VIDEO){
            // TODO: Change view holder type to audio
            val view = LayoutInflater.from(parent.context).inflate(R.layout.public_post_type_image, parent, false)
            return ImageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.public_post_type_image, parent, false)
            return ImageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item: PublicPost = getItem(position)

        if(holder.javaClass == AudioViewHolder::class.java){
            // TODO: Add audio support
        } else {
            // IMAGE
            val viewHolder = holder as ImageViewHolder

            val bitmap = ImageUtils.base64ToBitmap(item.image)

            viewHolder.imageView.setImageBitmap(bitmap)

            viewHolder.imageView.setOnClickListener {
                postClickListener(item)
            }
        }
    }

    private fun shouldBeLarge(position: Int): Boolean{

        if(position == 0 || position % 5 == 0){
            return true
        } else {
            return false
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)

        if(item.type == PublicPostType.AUDIO){
            return PublicPostType.AUDIO
        } else if(item.type == PublicPostType.VIDEO){
            return PublicPostType.VIDEO
        } else {
            return PublicPostType.IMAGE
        }
    }


}

class PublicPostDiffCallback(): DiffUtil.ItemCallback<PublicPost>() {
    override fun areItemsTheSame(oldItem: PublicPost, newItem: PublicPost): Boolean = oldItem.postId == newItem.postId

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: PublicPost, newItem: PublicPost): Boolean = oldItem == newItem

}