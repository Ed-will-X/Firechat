package com.varsel.firechat.utils

import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import java.time.Duration

// TODO: Delete class
class AnimationUtils {
    companion object {
        fun changeColor(vector: ImageView, color: Int, context: Context){
            vector.setColorFilter(context.resources.getColor(color), android.graphics.PorterDuff.Mode.MULTIPLY)
        }

        fun rotate90(icon: ImageView){
            val animator = ObjectAnimator.ofFloat(icon, View.ROTATION, 90f)
            animator.duration = 300
            animator.start()
        }

        fun rotate90_back(icon: ImageView){
            val animator = ObjectAnimator.ofFloat(icon, View.ROTATION, 0f)
            animator.duration = 300
            animator.start()
        }

        fun changeDialogDimAmount(dialog: BottomSheetDialog, amount: Float){
            if(dialog.window != null){
                dialog.window!!.setDimAmount(amount)
            }
        }
    }
}