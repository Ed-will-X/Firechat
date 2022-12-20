package com.varsel.firechat.utils

import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.varsel.firechat.data.local.PublicPost.PublicPostEntity
import com.varsel.firechat.presentation.signedIn.SignedinActivity

class PostUtils {
    companion object {
        fun sortPublicPosts_reversed(posts: List<String>): List<String> {
            val sorted = posts.sortedBy {
                extractTimestamp_from_id(it)
            }.reversed().toList()

            return sorted
        }

        fun sortPublicPosts(posts: List<String>): List<String> {
            val sorted = posts.sortedBy {
                extractTimestamp_from_id(it)
            }.toList()

            return sorted
        }

        fun extractTimestamp_from_id(id: String): Long{
            if(id.substring(0, 1).equals(":")){
                return id.substringAfter(":").substringBefore("-").toLong()
            } else {
                return 0L
            }
        }

        fun extractTimestamp_inList(IDs: List<String>): List<Long> {
            val extracted = mutableListOf<Long>()

            for (i in IDs){
                extracted.add(extractTimestamp_from_id(i))
            }

            return extracted
        }

        fun extractType(string: String): Int {
            if(string.substring(string.length - 2, string.length -1) == ":"){
                return string.substring(string.length - 1, string.length).toInt()
            } else {
                return 0
            }
        }

        fun check_if_post_in_room(id: String, activity: SignedinActivity, postCallback: (post: PublicPostEntity?) -> Unit){
            activity.checkIfPostInDb(id) {
                if(it != null){
                    postCallback(it)
                } else {
                    postCallback(null)
                }
            }
        }

        fun setPostImage(post: PublicPostEntity, image: ImageView, viewParent: MaterialCardView, activity: SignedinActivity){
            if(post.image != null && post.image?.isNotEmpty() == true){
                val bitmap = ImageUtils.base64ToBitmap(post.image!!)
                Glide.with(activity).load(bitmap).dontAnimate().into(image)
                viewParent.visibility = View.VISIBLE

//                image.setImageBitmap(bitmap)
            }
        }
    }
}