package com.varsel.firechat.utils

import android.content.Context
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import java.time.Duration

class AnimationUtils {
    companion object {
        fun changeColor(vector: ImageView, color: Int, context: Context){
            vector.setColorFilter(context.resources.getColor(color), android.graphics.PorterDuff.Mode.MULTIPLY)
        }

        fun sharedZAxisExit(fragment: Fragment, length: Long){
            fragment.exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
                duration = length
            }

            fragment.reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
                duration = length
            }
        }

        fun sharedZAxisEntry(fragment: Fragment, length: Long){
            fragment.enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
                duration = length
            }

            fragment.returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
                duration = length
            }
        }

        fun materialFadeThroughEntry(fragment: Fragment, length: Long){
            fragment.enterTransition = MaterialFadeThrough().apply {
                duration = length
            }
        }

        fun materialFadeThroughExit(fragment: Fragment, length: Long){
            fragment.exitTransition = MaterialFadeThrough().apply {
                duration = length
            }
        }
    }
}