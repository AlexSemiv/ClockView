package com.example.clockview

import android.content.Context
import android.view.View

object Utils {
    fun View.dip(value: Int): Int = context.dip(value)
    fun View.sp(value: Int): Int = context.sp(value)
    fun Context.dip(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    fun Context.sp(value: Int): Int = (value * resources.displayMetrics.scaledDensity).toInt()
}