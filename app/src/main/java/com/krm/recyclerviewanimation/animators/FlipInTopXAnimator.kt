package com.krm.recyclerviewanimation.animators

import android.view.animation.Interpolator
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView

class FlipInTopXAnimator : BaseItemAnimator {
    constructor() {}
    constructor(interpolator: Interpolator) {
        mInterpolator = interpolator
    }

    override fun animateRemoveImpl(holder: RecyclerView.ViewHolder?) {
        ViewCompat.animate(holder!!.itemView)
            .rotationX(90f)
            .setDuration(removeDuration)
            .setInterpolator(mInterpolator)
            .setListener(DefaultRemoveVpaListener(holder))
            .setStartDelay(getRemoveDelay(holder))
            .start()
    }

    override fun preAnimateAddImpl(holder: RecyclerView.ViewHolder?) {
        ViewCompat.setRotationX(holder!!.itemView, 90f)
    }

    override fun animateAddImpl(holder: RecyclerView.ViewHolder?) {
        ViewCompat.animate(holder!!.itemView)
            .rotationX(0f)
            .setDuration(addDuration)
            .setInterpolator(mInterpolator)
            .setListener(DefaultAddVpaListener(holder))
            .setStartDelay(getAddDelay(holder))
            .start()
    }
}