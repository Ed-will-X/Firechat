package com.varsel.firechat.domain.use_case._util.animation

import android.content.Context
import android.widget.ImageView

// TODO: Animate color change
class ChangeIconColorUseCase {
    operator fun invoke(vector: ImageView, color: Int, context: Context){
        vector.setColorFilter(context.resources.getColor(color), android.graphics.PorterDuff.Mode.MULTIPLY)
    }
}