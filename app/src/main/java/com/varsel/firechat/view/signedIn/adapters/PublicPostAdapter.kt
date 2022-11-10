package com.varsel.firechat.view.signedIn.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.varsel.firechat.R
import com.varsel.firechat.model.PublicPost.PublicPost
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.view.signedIn.SignedinActivity

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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.public_post_type_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val item: String = publicPostStrings[position]

        if(shape == PublicPostAdapterShapes.SQUARE){
            handleBindImage(holder.image_square, item)
        } else if(shape == PublicPostAdapterShapes.RECTANCLE){
            handleBindImage(holder.image_rectangle, item)
        } else if(shape == PublicPostAdapterShapes.RECTANGLE_SMALL){
            handleBindImage(holder.image_rectangle_small, item)

        }

    }

    fun handleBindImage(image: ImageView, item: String){
        // TODO: Add optional download
        // check if position is anywhere between 0 and 4
            // if: Call the determine code directly
            // else: Check if it is in DB, then call the deternime code on item press
        activity.determinePublicPostFetchMethod_fullObject(item) {
            if(it != null){
                val decoded = ImageUtils.base64ToBitmap(it.image)

                image.visibility = View.VISIBLE

                image.setImageBitmap(decoded)

                image.setOnClickListener { it2 ->
                    postClickListener(it)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return publicPostStrings.size
    }
}