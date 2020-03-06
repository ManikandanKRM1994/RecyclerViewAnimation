package com.krm.recyclerviewanimation.adapters

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import com.krm.recyclerviewanimation.MainAdapter

class ScaleInAnimationAdapter private constructor(
    adapter: MainAdapter,
    private val mFrom: Float
) : AnimationAdapter(adapter) {

    constructor(adapter: MainAdapter) : this(
        adapter,
        DEFAULT_SCALE_FROM
    )

    override fun getAnimators(view: View?): Array<Animator> {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", mFrom, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", mFrom, 1f)
        return arrayOf(scaleX, scaleY)
    }

    companion object {
        private const val DEFAULT_SCALE_FROM = .5f
    }

}