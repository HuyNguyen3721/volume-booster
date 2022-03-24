package com.ezstudio.volumebooster.test.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class CustomViewPager : ViewPager {
    var isPagingEnabled = true

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        try {
            return isPagingEnabled && super.onTouchEvent(event)
        } catch (ex: IllegalArgumentException) {
        }
        return false

    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        try {
            return isPagingEnabled && super.onInterceptTouchEvent(event)
        } catch (ex: IllegalArgumentException) {
        }
        return false
    }

}