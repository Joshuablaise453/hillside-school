package com.electrohub.schoolmanagementapp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import com.google.firebase.auth.FirebaseAuth

class StudentDashboard : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var navCard: View
    private lateinit var navSelectionPill: View
    private lateinit var navItemHome: LinearLayout
    private lateinit var navItemSearch: LinearLayout
    private lateinit var navItemNotify: LinearLayout
    private lateinit var navItemProfile: LinearLayout

    private lateinit var navContentHome: View
    private lateinit var navContentSearch: View
    private lateinit var navContentNotify: View
    private lateinit var navContentProfile: View

    private var currentSelectedIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_student_dashboard)

        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Handle window insets
        navCard = findViewById(R.id.navCard)
        findViewById<View>(R.id.main)?.let { applyBounceToAllClickables(it) }
        ViewCompat.setOnApplyWindowInsetsListener(navCard) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val lp = v.layoutParams as ViewGroup.MarginLayoutParams
            lp.bottomMargin = 16 + systemBars.bottom
            v.layoutParams = lp
            insets
        }

        setupModernNav()
        setupScrollAnimations()

        // Update user name from intent
        val userName = intent.getStringExtra("USER_NAME")
        if (!userName.isNullOrEmpty()) {
            findViewById<TextView>(R.id.userNameText).text = userName
        }
    }

    private fun setupScrollAnimations() {
        val scrollView = findViewById<NestedScrollView>(R.id.dashboardScrollView)
        val contentLayout = findViewById<LinearLayout>(R.id.dashboardContentLayout)
        
        // Initial animation for the whole layout
        contentLayout?.layoutAnimation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_slide_up)
        
        scrollView?.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            val centerY = v.height / 2
            
            for (i in 0 until (contentLayout?.childCount ?: 0)) {
                val child = contentLayout?.getChildAt(i) ?: continue
                val childRect = android.graphics.Rect()
                child.getGlobalVisibleRect(childRect)
                
                // Add a subtle parallax or scale effect as items scroll
                val viewCenterY = (childRect.top + childRect.bottom) / 2
                val distFromCenter = (centerY - viewCenterY).toFloat()
                val scale = 1f - (Math.abs(distFromCenter) / v.height) * 0.15f
                val rotation = (distFromCenter / v.height) * 5f
                
                if (childRect.intersect(android.graphics.Rect(0, 0, v.width, v.height))) {
                    child.scaleX = Math.max(0.85f, scale)
                    child.scaleY = Math.max(0.85f, scale)
                    child.rotationX = rotation
                }
            }
        })
    }

    private fun setupModernNav() {
        navSelectionPill = findViewById(R.id.navSelectionPill)
        navItemHome = findViewById(R.id.navItemHome)
        navItemSearch = findViewById(R.id.navItemSearch)
        navItemNotify = findViewById(R.id.navItemNotify)
        navItemProfile = findViewById(R.id.navItemProfile)

        navContentHome = findViewById(R.id.navContentHome)
        navContentSearch = findViewById(R.id.navContentSearch)
        navContentNotify = findViewById(R.id.navContentNotify)
        navContentProfile = findViewById(R.id.navContentProfile)

        val items = listOf(navItemHome, navItemSearch, navItemNotify, navItemProfile)

        items.forEachIndexed { index, linearLayout ->
            linearLayout.setOnClickListener {
                if (currentSelectedIndex != index) {
                    selectNavItem(index)
                    when(index) {
                        1 -> startActivity(Intent(this, AcademicActivity::class.java))
                        2 -> startActivity(Intent(this, ScheduleActivity::class.java))
                        3 -> startActivity(Intent(this, ProfileActivity::class.java))
                    }
                }
            }
        }

        // Default selection
        navCard.post {
            selectNavItem(0, animate = false)
        }
    }

    private fun selectNavItem(index: Int, animate: Boolean = true) {
        val items = listOf(navItemHome, navItemSearch, navItemNotify, navItemProfile)
        val contents = listOf(navContentHome, navContentSearch, navContentNotify, navContentProfile)
        val navIcons = listOf<ImageView>(
            findViewById(R.id.navIconHome),
            findViewById(R.id.navIconSearch),
            findViewById(R.id.navIconNotify),
            findViewById(R.id.navIconProfile)
        )
        val navTexts = listOf<TextView>(
            findViewById(R.id.navTextHome),
            findViewById(R.id.navTextSearch),
            findViewById(R.id.navTextNotify),
            findViewById(R.id.navTextProfile)
        )

        currentSelectedIndex = index

        // 1. Update tints and colors
        items.forEachIndexed { i, _ ->
            val icon = navIcons[i]
            val text = navTexts[i]
            if (i == index) {
                text.setTextColor(Color.parseColor("#0D2B23")) // text_dark_green
                icon.imageTintList = ColorStateList.valueOf(Color.parseColor("#0D2B23"))
            } else {
                text.setTextColor(Color.parseColor("#5B6B5D")) // text_muted_green
                icon.imageTintList = ColorStateList.valueOf(Color.parseColor("#5B6B5D"))
            }
        }

        // 2. Animate the pill background
        val selectedItem = items[index]
        val selectedContent = contents[index]

        selectedItem.post {
            // Need to measure the content width AFTER text visibility changed
            selectedContent.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            val targetWidth = selectedContent.measuredWidth
            val targetX = selectedItem.x + (selectedItem.width - targetWidth) / 2

            if (!animate) {
                val lp = navSelectionPill.layoutParams as FrameLayout.LayoutParams
                lp.width = targetWidth
                navSelectionPill.layoutParams = lp
                navSelectionPill.x = targetX
                navSelectionPill.visibility = View.VISIBLE
                return@post
            }

            // Animate width
            val widthAnimator = ValueAnimator.ofInt(navSelectionPill.width, targetWidth)
            widthAnimator.addUpdateListener { animator ->
                val lp = navSelectionPill.layoutParams as FrameLayout.LayoutParams
                lp.width = animator.animatedValue as Int
                navSelectionPill.layoutParams = lp
            }

            // Animate translationX
            val xAnimator = ObjectAnimator.ofFloat(navSelectionPill, "x", targetX)

            val animatorSet = AnimatorSet()
            animatorSet.playTogether(widthAnimator, xAnimator)
            animatorSet.duration = 300
            animatorSet.start()

            // navSelectionPill.visibility = View.VISIBLE // Disabled for now as per new design
        }
    }
}