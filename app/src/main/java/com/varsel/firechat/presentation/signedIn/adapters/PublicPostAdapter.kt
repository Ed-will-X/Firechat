package com.varsel.firechat.presentation.signedIn.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.varsel.firechat.R
import com.varsel.firechat.data.local.PublicPost.PublicPost
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.utils.PostUtils
import com.varsel.firechat.presentation.signedIn.SignedinActivity

/*
*   Set width and height to null to use the default
* */

class PublicPostAdapterShapes {
    companion object {
        val SQUARE = 0
        val RECTANCLE = 1
        val RECTANGLE_SMALL = 2
    }
}

class PublicPostAdapter(
    val activity: SignedinActivity,
    val shape: Int,
    val postClickListener: (publicPost: PublicPost) -> Unit
): RecyclerView.Adapter<PublicPostAdapter.ImageViewHolder>() {

    var publicPostStrings = listOf<String>()

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image_square = itemView.findViewById<ImageView>(R.id.image_square)
        val image_rectangle = itemView.findViewById<ImageView>(R.id.image_rectangle)
        val image_rectangle_small = itemView.findViewById<ImageView>(R.id.image_rectangle_small)

        val image_rectangle_small_parent = itemView.findViewById<MaterialCardView>(R.id.image_rectangle_small_parent)
        val image_rectangle_parent = itemView.findViewById<MaterialCardView>(R.id.image_rectangle_parent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.public_post_type_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val item: String = publicPostStrings[position]

        if(shape == PublicPostAdapterShapes.SQUARE){
            handleBindImage(holder.image_square, null, item, position)
        } else if(shape == PublicPostAdapterShapes.RECTANCLE){
            handleBindImage(holder.image_rectangle, holder.image_rectangle_parent, item, position)
        } else if(shape == PublicPostAdapterShapes.RECTANGLE_SMALL){
            handleBindImage(holder.image_rectangle_small, holder.image_rectangle_small_parent, item, position)
        }

    }

    fun handleBindImage(image: ImageView, parent: MaterialCardView?, item: String, position: Int){
        var hasBeenClicked = false
        parent?.visibility = View.VISIBLE

        // check if item-to-load is not the full image
        if(shape != PublicPostAdapterShapes.RECTANCLE){
            // if: Call the determine code directly
            activity.determinePublicPostFetchMethod_fullObject(item) {
                if(it != null){
                    setImage(it, image, parent)
                }
            }
        } else {
            // else: Check if it is in DB, then call the determine code on item press
            PostUtils.check_if_post_in_room(item, activity) {
                // check if in room
                if(it != null && parent != null){
                    // if: Bind it directly
                    setImage(it, image, parent)
                } else {
                    // else: Set on click listener that downloads it from firebase
                    parent?.setOnClickListener {
                        if(!hasBeenClicked){
                            hasBeenClicked = true

                            // TODO: Get and set image
                            activity.fetchPublicPost_fullObject(item) {
                                if(it != null){
                                    setImage(it, image, parent)
                                }
                            }

                        }
                    }

                }
            }

        }
    }

    fun setImage(post: PublicPost, image: ImageView, parent: MaterialCardView?){
        if(post.image != null){
            val decoded = ImageUtils.base64ToBitmap(post.image!!)

            image.visibility = View.VISIBLE

            if(parent != null){
                parent.visibility = View.VISIBLE
            }

            image.setImageBitmap(decoded)

            image.setOnClickListener { it2 ->
                postClickListener(post)
            }
        }
    }

    override fun getItemCount(): Int {
        return publicPostStrings.size
    }
}