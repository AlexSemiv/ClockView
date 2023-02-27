package com.example.clockview

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View

class ClockView(
    context: Context,
    attributeSet: AttributeSet,
    defStyle: Int = 0
) : View(context, attributeSet, defStyle) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }
}