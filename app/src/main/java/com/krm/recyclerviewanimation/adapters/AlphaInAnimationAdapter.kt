package com.krm.recyclerviewanimation.adapters

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import com.krm.recyclerviewanimation.MainAdapter

class AlphaInAnimationAdapter private constructor(
    adapter: MainAdapter,
    private val mFrom: Float
) : AnimationAdapter(adapter) {

    constructor(adapter: MainAdapter) : this(
        adapter,
        DEFAULT_ALPHA_FROM
    )

    override fun getAnimators(view: View?): Array<Animator> {
        return arrayOf(ObjectAnimator.ofFloat(view, "alpha", mFrom, 1f))
    }

    companion object {
        private const val DEFAULT_ALPHA_FROM = 0f
    }

}