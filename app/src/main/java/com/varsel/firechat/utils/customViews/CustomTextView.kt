package com.varsel.firechat.utils.customViews

import android.content.Context
import android.util.AttributeSet
import android.view.View.MeasureSpec.*
import androidx.appcompat.widget.AppCompatTextView
import kotlin.math.ceil

class TightTextView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val lineCount = layout.lineCount
        if (lineCount > 1 && getMode(widthMeasureSpec) != EXACTLY) {
            val textWidth = (0 until lineCount).maxOf(layout::getLineWidth)
            val padding = compoundPaddingLeft + compoundPaddingRight
            val w = ceil(textWidth).toInt() + padding

            if (w < measuredWidth) {
                val newWidthMeasureSpec = makeMeasureSpec(w, AT_MOST)
                super.onMeasure(newWidthMeasureSpec, heightMeasureSpec)
            }
        }
    }
}