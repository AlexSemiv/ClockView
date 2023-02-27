package com.example.clockview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.clockview.Utils.dip
import kotlin.math.min

class ClockView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet,
    defStyle: Int = 0
) : View(context, attributeSet, defStyle) {

    private val boarderAndDotOffset = dip(8).toFloat()
    private val dotAndNumberOffset = dip(12).toFloat()

    private val boarderWidth = dip(8)
    private val boarderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(
            context,
            R.color.clock_view_boarder
        )
        style = Paint.Style.STROKE
        strokeWidth = boarderWidth.toFloat()
    }

    private val dialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(
            context,
            R.color.clock_view_dial
        )
        style = Paint.Style.FILL
    }

    private val numberPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.clock_view_number)
        style = Paint.Style.FILL
        textSize = 72f
    }
    private val numberRect = Rect()

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.clock_view_dot)
        style = Paint.Style.FILL
    }

    private val minuteDotRadius = dip(2).toFloat()
    private val hourDotRadius = dip(4).toFloat()

    // hands..

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMeasureMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthMeasureSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMeasureMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightMeasureSize = MeasureSpec.getSize(heightMeasureSpec)

        val preferWidth = dip(250) + boarderWidth
        val calculateWidth = when (widthMeasureMode) {
            MeasureSpec.EXACTLY -> widthMeasureSize
            MeasureSpec.AT_MOST -> min(preferWidth, widthMeasureSize)
            else -> preferWidth
        }

        val preferHeight = dip(250) + boarderWidth
        val calculateHeight = when (heightMeasureMode) {
            MeasureSpec.EXACTLY -> heightMeasureSize
            MeasureSpec.AT_MOST -> min(preferHeight, heightMeasureSize)
            else -> preferHeight
        }

        // часы по идее должны быть всегда "кругом", а не "овалом"
        val finalSize = min(calculateWidth, calculateHeight)
        setMeasuredDimension(finalSize, finalSize)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawBoarder(canvas)
        drawDial(canvas)
        drawDotsAndNumbers(canvas)
        // drawHands(canvas)
    }

    private fun drawBoarder(canvas: Canvas) {
        canvas.save()
        val cx = measuredWidth / 2f
        val cy = measuredHeight / 2f
        val radius = cx - boarderWidth / 2f
        canvas.drawCircle(
            cx,
            cy,
            radius,
            boarderPaint
        )
        canvas.restore()
    }

    private fun drawDial(canvas: Canvas) {
        canvas.save()
        val cx = measuredWidth / 2f
        val cy = measuredHeight / 2f
        val radius = cx - boarderWidth
        canvas.drawCircle(
            cx,
            cy,
            radius,
            dialPaint
        )
        canvas.restore()
    }

    private fun drawDotsAndNumbers(canvas: Canvas) {
        canvas.save()
        val dx = measuredWidth / 2f
        val dy = measuredHeight / 2f
        canvas.translate(
            dx,
            dy
        )
        for (i in 1..60) {
            canvas.rotate(6f, 0f, 0f)
            if (i % 5 == 0) {
                val dotCx = 0f
                val dotCy = -dy + boarderWidth + boarderAndDotOffset
                canvas.drawCircle(
                    dotCx,
                    dotCy,
                    hourDotRadius,
                    dotPaint
                )
                drawNumber(canvas, i, dotCx, dotCy)
            } else {
                canvas.drawCircle(
                    0f,
                    -dy + boarderWidth + boarderAndDotOffset,
                    minuteDotRadius,
                    dotPaint
                )
            }
        }
        canvas.restore()
    }

    private fun drawHands(canvas: Canvas) {
        // todo
    }

    private fun drawNumber(canvas: Canvas, i: Int, dotCx: Float, dotCy: Float) {
        canvas.save()
        val number = (i / 5).toString()
        numberPaint.getTextBounds(number, 0, number.length, numberRect)
        canvas.translate(
            dotCx,
            dotCy + dotAndNumberOffset + numberRect.height() / 2
        )
        canvas.rotate(-6f * i)
        canvas.drawText(
            number,
            numberRect.width() / -2f,
            numberRect.height() / 2f,
            numberPaint
        )
        canvas.restore()
    }
}