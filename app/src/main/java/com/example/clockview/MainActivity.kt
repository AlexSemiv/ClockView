package com.example.clockview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import com.example.clockview.Utils.dip

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<LinearLayout>(R.id.container)?.let { container ->
            container.addView(
                ClockView(this).apply {
                    id = R.id.clock1
                    setDialColor(
                        ContextCompat.getColor(
                            this@MainActivity,
                            R.color.clock_view_dial
                        )
                    )
                    setBoarderColor(
                        ContextCompat.getColor(
                            this@MainActivity,
                            R.color.clock_view_boarder
                        )
                    )
                    setPadding(dip(12))
                },
                0,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }
    }
}