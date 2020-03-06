package com.krm.recyclerviewanimation.internal

import android.view.View
import androidx.core.view.ViewCompat

object ViewHelper {
    fun clear(v: View) {
        ViewCompat.setAlpha(v, 1f)
        ViewCompat.setScaleY(v, 1f)
        ViewCompat.setScaleX(v, 1f)
        ViewCompat.setTranslationY(v, 0f)
        ViewCompat.setTranslationX(v, 0f)
        ViewCompat.setRotation(v, 0f)
        ViewCompat.setRotationY(v, 0f)
        ViewCompat.setRotationX(v, 0f)
        ViewCompat.setPivotY(v, v.measuredHeight / 2.toFloat())
        ViewCompat.setPivotX(v, v.measuredWidth / 2.toFloat())
        ViewCompat.animate(v).setInterpolator(null).startDelay = 0
    }
}