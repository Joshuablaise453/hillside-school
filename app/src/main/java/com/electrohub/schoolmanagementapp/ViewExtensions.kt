package com.electrohub.schoolmanagementapp

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.cardview.widget.CardView

@SuppressLint("ClickableViewAccessibility")
fun View.addClickBounce() {
    this.setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                v.animate().scaleX(0.94f).scaleY(0.94f).setDuration(100).start()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }
        }
        false
    }
}

fun applyBounceToAllClickables(view: View) {
    if (view is Button || view is CardView || view is EditText || view.isClickable) {
        view.addClickBounce()
    }

    if (view is ViewGroup) {
        for (i in 0 until view.childCount) {
            applyBounceToAllClickables(view.getChildAt(i))
        }
    }
}