package com.varsel.firechat.domain.use_case._util.animation

import android.animation.ObjectAnimator
import android.view.View
import android.widget.ImageView

sealed class Direction() {
    class Forward(): Direction()
    class Reverse(): Direction()
}

class Rotate90UseCase {
    operator fun invoke(icon: ImageView, direction: Direction){
        val animator: ObjectAnimator
        when(direction) {
            is Direction.Forward -> {
                animator = ObjectAnimator.ofFloat(icon, View.ROTATION, 90f)
                animator.duration = 300
                animator.start()
            }
            is Direction.Reverse -> {
                animator = ObjectAnimator.ofFloat(icon, View.ROTATION, 0f)
                animator.duration = 300
                animator.start()
            }
        }
    }
}