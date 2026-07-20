package com.electrohub.schoolmanagementapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        findViewById<View>(R.id.main)?.let { applyBounceToAllClickables(it) }

        setupNavigation()
        setupScrollAnimations()

        findViewById<android.widget.Button>(R.id.logoutButton).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupScrollAnimations() {
        val scrollView = findViewById<NestedScrollView>(R.id.profileScrollView)
        val contentLayout = findViewById<LinearLayout>(R.id.profileContentLayout)
        
        contentLayout?.layoutAnimation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_slide_up)

        scrollView?.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, _, _, _ ->
            val centerY = v.height / 2
            for (i in 0 until (contentLayout?.childCount ?: 0)) {
                val child = contentLayout?.getChildAt(i) ?: continue
                val childRect = android.graphics.Rect()
                child.getGlobalVisibleRect(childRect)
                
                val viewCenterY = (childRect.top + childRect.bottom) / 2
                val distFromCenter = (centerY - viewCenterY).toFloat()
                val scale = 1f - (Math.abs(distFromCenter) / v.height) * 0.1f
                val rotation = (distFromCenter / v.height) * 5f
                
                if (childRect.intersect(android.graphics.Rect(0, 0, v.width, v.height))) {
                    child.scaleX = Math.max(0.9f, scale)
                    child.scaleY = Math.max(0.9f, scale)
                    child.rotationX = rotation
                }
            }
        })
    }

    private fun setupNavigation() {
        findViewById<LinearLayout>(R.id.navItemHome).setOnClickListener {
            startActivity(Intent(this, StudentDashboard::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navItemSearch).setOnClickListener {
            startActivity(Intent(this, AcademicActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navItemNotify).setOnClickListener {
            startActivity(Intent(this, ScheduleActivity::class.java))
            finish()
        }
        // Profile is current activity
    }
}