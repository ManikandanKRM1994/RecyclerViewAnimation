package com.krm.recyclerviewanimation.adapters

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import com.krm.recyclerviewanimation.MainAdapter

class SlideInLeftAnimationAdapter(adapter: MainAdapter) :
    AnimationAdapter(adapter) {
    override fun getAnimators(view: View?): Array<Animator> {
        return arrayOf(
            ObjectAnimator.ofFloat(
                view,
                "translationX",
                -view!!.rootView.width.toFloat(),
                0f
            )
        )
    }
}