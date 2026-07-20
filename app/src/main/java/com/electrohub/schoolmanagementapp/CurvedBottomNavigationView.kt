package com.electrohub.schoolmanagementapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class CurvedBottomNavigationView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val path = Path()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.nav_dark)
    }

    private var selectedItemX = 0f
    private val curveRadius = 110f // Size of the "dip"

    fun setCurvePosition(x: Float) {
        selectedItemX = x
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        path.reset()
        path.moveTo(0f, 0f)

        // Left part of the bar
        path.lineTo(selectedItemX - curveRadius, 0f)

        // The concave curve (the dip)
        path.cubicTo(
            selectedItemX - curveRadius / 2, 0f,
            selectedItemX - curveRadius / 2, curveRadius / 1.5f,
            selectedItemX, curveRadius / 1.5f
        )
        path.cubicTo(
            selectedItemX + curveRadius / 2, curveRadius / 1.5f,
            selectedItemX + curveRadius / 2, 0f,
            selectedItemX + curveRadius, 0f
        )

        // Right part of the bar
        path.lineTo(w, 0f)
        path.lineTo(w, h)
        path.lineTo(0f, h)
        path.close()

        canvas.drawPath(path, paint)
    }
}