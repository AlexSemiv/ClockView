package com.example.clockview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.os.Parcelable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnRepeat
import androidx.core.content.ContextCompat
import com.example.clockview.Utils.dip
import java.util.*
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class ClockView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet,
    defStyle: Int = 0
) : View(context, attributeSet, defStyle) {

    private var calendar = Calendar.getInstance()
    private val secondAngle: Float
        get() = calendar[Calendar.SECOND] * 360 / 60f
    private val minuteAngle: Float
        get() = (calendar[Calendar.MINUTE] + calendar[Calendar.SECOND] / 60f) * 360 / 60f
    private val hourAngle: Float
        get() = (calendar[Calendar.HOUR] + calendar[Calendar.MINUTE] / 60f) * 360 / 12f

    private var lastSecondAngle = secondAngle
    private var secondProgress = 0f
    private val oneSecondAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        val timeMillis = 1000L
        interpolator = LinearInterpolator()
        duration = timeMillis
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener { animator ->
            val secondAngle = secondAngle
            val value = animator.animatedValue as Float
            secondProgress = value
            if (lastSecondAngle == secondAngle) {
                invalidate()
            } else {
                lastSecondAngle = secondAngle
            }
        }
        doOnRepeat {
            calendar.add(Calendar.MILLISECOND, timeMillis.toInt())
        }
        doOnCancel {
            secondProgress = 0f
            invalidate()
        }
    }

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

    private val handPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.clock_view_hand)
        style = Paint.Style.FILL
    }

    private val secondHandPath = Path()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMeasureMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthMeasureSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMeasureMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightMeasureSize = MeasureSpec.getSize(heightMeasureSpec)

        val preferWidth = dip(250) + boarderWidth
        val finalWidth = when (widthMeasureMode) {
            MeasureSpec.EXACTLY -> widthMeasureSize
            MeasureSpec.AT_MOST -> min(preferWidth, widthMeasureSize)
            else -> preferWidth
        }

        val preferHeight = dip(250) + boarderWidth
        val finalHeight = when (heightMeasureMode) {
            MeasureSpec.EXACTLY -> heightMeasureSize
            MeasureSpec.AT_MOST -> min(preferHeight, heightMeasureSize)
            else -> preferHeight
        }

        setMeasuredDimension(finalWidth, finalHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawBoarder(canvas)
        drawDial(canvas)
        val cx = measuredWidth / 2f
        val cy = measuredHeight / 2f
        drawDotsAndNumbers(canvas, cx, cy)
        drawHands(canvas, cx, cy)
    }

    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        if (isVisible) {
            calendar = Calendar.getInstance()
            val timeMillis = calendar[Calendar.MILLISECOND]
            secondProgress = timeMillis / 1000f
            oneSecondAnimator.start()
        } else {
            if (oneSecondAnimator.isRunning) oneSecondAnimator.cancel()
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        return Bundle(2).apply {
            putFloat(LAST_SECOND_ANGLE, lastSecondAngle)
            putParcelable(SUPER_STATE, super.onSaveInstanceState())
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val superState = if (state is Bundle) {
            lastSecondAngle = state.getFloat(LAST_SECOND_ANGLE)
            state.getParcelable(SUPER_STATE)
        } else {
            state
        }
        super.onRestoreInstanceState(superState)
    }

    private fun drawBoarder(canvas: Canvas) {
        canvas.save()
        val boarderWidthPart = boarderWidth / 2f
        canvas.drawOval(
            boarderWidthPart,
            boarderWidthPart,
            measuredWidth.toFloat() - boarderWidthPart,
            measuredHeight.toFloat() - boarderWidthPart,
            boarderPaint
        )
        canvas.restore()
    }

    private fun drawDial(canvas: Canvas) {
        canvas.save()
        canvas.drawOval(
            boarderWidth.toFloat(),
            boarderWidth.toFloat(),
            measuredWidth.toFloat() - boarderWidth,
            measuredHeight.toFloat() - boarderWidth,
            dialPaint
        )
        canvas.restore()
    }

    private fun drawDotsAndNumbers(canvas: Canvas, cx: Float, cy: Float) {
        canvas.save()
        canvas.translate(cx, cy)
        for (i in 1..60) {
            // по идее здесь должен быть поворот не на 6 градусов,
            // а на градус, полученный при вычислении равных сегментов эллипса,
            // для того чтобы равномерно распределять числа по циферблату
            // но это трудно сделать без сложных вычислений
            canvas.rotate(6f)
            val radius = calculateRadius(6f * i, cx, cy)
            val dotCx = 0f
            val dotCy = -radius + boarderWidth + boarderAndDotOffset
            if (i % 5 == 0) {
                drawDot(canvas, hourDotRadius, dotCx, dotCy)
                val numberCx = dotCx
                val numberCy = dotCy + dotAndNumberOffset + numberRect.height() / 2
                drawNumber(canvas, i, numberCx, numberCy)
            } else {
                drawDot(canvas, minuteDotRadius, dotCx, dotCy)
            }
        }
        canvas.restore()
    }

    private fun drawDot(canvas: Canvas, radius: Float, dotCx: Float, dotCy: Float) {
        canvas.save()
        canvas.drawCircle(
            dotCx,
            dotCy,
            radius,
            dotPaint
        )
        canvas.restore()
    }

    private fun drawNumber(canvas: Canvas, index: Int, numberCx: Float, numberCy: Float) {
        canvas.save()
        val number = (index / 5).toString()
        numberPaint.getTextBounds(number, 0, number.length, numberRect)
        canvas.translate(numberCx, numberCy)
        canvas.rotate(-6f * index)
        canvas.drawText(
            number,
            numberRect.width() / -2f,
            numberRect.height() / 2f,
            numberPaint
        )
        canvas.restore()
    }

    private fun drawHands(canvas: Canvas, cx: Float, cy: Float) {
        canvas.save()
        canvas.translate(cx, cy)
        drawHourHand(canvas, cx, cy)
        drawMinuteHand(canvas, cx, cy)
        drawSecondHand(canvas, cx, cy)
        canvas.restore()
    }

    private fun drawHourHand(canvas: Canvas, cx: Float, cy: Float) {
        canvas.save()
        val hourAngle = hourAngle
        val hourHandRadius = calculateRadius(hourAngle, cx, cy)
        val centerToNumberOffset = hourHandRadius - boarderWidth - boarderAndDotOffset - hourDotRadius - dotAndNumberOffset
        canvas.rotate(hourAngle)
        val hourHandWidth = hourDotRadius * 1.5f
        canvas.drawRect(
            -hourHandWidth / 2f,
            -centerToNumberOffset / 1.75f,
            hourHandWidth / 2f,
            centerToNumberOffset / 10f,
            handPaint
        )
        canvas.restore()
    }

    private fun drawMinuteHand(canvas: Canvas, cx: Float, cy: Float) {
        canvas.save()
        val minuteAngle = minuteAngle
        val minuteHandRadius = calculateRadius(minuteAngle, cx, cy)
        val centerToNumberOffset = minuteHandRadius - boarderWidth - boarderAndDotOffset - hourDotRadius - dotAndNumberOffset
        canvas.rotate(minuteAngle)
        val minuteHandWidth = minuteDotRadius * 1.5f
        canvas.drawRect(
            -minuteHandWidth / 2f,
            -centerToNumberOffset / 1.5f,
            minuteHandWidth / 2f,
            centerToNumberOffset / 10f,
            handPaint
        )
        canvas.restore()
    }

    private fun drawSecondHand(canvas: Canvas, cx: Float, cy: Float) {
        canvas.save()
        val secondAngle = secondAngle + 6f * secondProgress
        val secondHandRadius = calculateRadius(secondAngle, cx, cy)
        val centerToNumberOffset = secondHandRadius - boarderWidth - boarderAndDotOffset - hourDotRadius - dotAndNumberOffset
        canvas.rotate(secondAngle)
        val secondHandBottomWidth = minuteDotRadius * 1.5f
        secondHandPath.reset()
        secondHandPath.moveTo(
            -secondHandBottomWidth / 2f,
            centerToNumberOffset / 10f
        )
        secondHandPath.lineTo(
            -secondHandBottomWidth / 6f,
            -centerToNumberOffset / 1.25f
        )
        secondHandPath.lineTo(
            secondHandBottomWidth / 6f,
            -centerToNumberOffset / 1.25f
        )
        secondHandPath.lineTo(
            secondHandBottomWidth / 2f,
            centerToNumberOffset / 10f
        )
        secondHandPath.close()
        canvas.drawPath(
            secondHandPath,
            handPaint
        )
        canvas.restore()
    }

    /**
     * https://ru.onlinemschool.com/math/formula/ellipse
     */
    private fun ellipseRadius(
        bigAxisAlpha: Float,
        bigSemiAxis: Float,
        smallSemiAxis: Float
    ): Float {
        val alpha = Math.toRadians(bigAxisAlpha.toDouble()).toFloat()
        return (bigSemiAxis * smallSemiAxis) /
                sqrt(bigSemiAxis.pow(2) * sin(alpha).pow(2) + smallSemiAxis.pow(2) * cos(alpha).pow(2))
    }

    private fun calculateRadius(
        rotateAngle: Float,
        firstSemiAxis: Float,
        secondSemiAxis: Float
    ): Float {
        return if (firstSemiAxis > secondSemiAxis) {
            val bigAxisAlpha = (90 - rotateAngle) % 180
            ellipseRadius(bigAxisAlpha, firstSemiAxis, secondSemiAxis)
        } else {
            val bigAxisAlpha = rotateAngle % 180
            ellipseRadius(bigAxisAlpha, secondSemiAxis, firstSemiAxis)
        }
    }

    companion object {
        private const val LAST_SECOND_ANGLE = "LAST_SECOND_ANGLE"
        private const val SUPER_STATE = "SUPER_STATE"
    }
}