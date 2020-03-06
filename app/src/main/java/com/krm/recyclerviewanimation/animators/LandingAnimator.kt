package com.krm.recyclerviewanimation.animators

import android.view.animation.Interpolator
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView

class LandingAnimator : BaseItemAnimator {
    constructor() {}
    constructor(interpolator: Interpolator) {
        mInterpolator = interpolator
    }

    override fun animateRemoveImpl(holder: RecyclerView.ViewHolder?) {
        ViewCompat.animate(holder!!.itemView)
            .alpha(0f)
            .scaleX(1.5f)
            .scaleY(1.5f)
            .setDuration(removeDuration)
            .setInterpolator(mInterpolator)
            .setListener(DefaultRemoveVpaListener(holder))
            .setStartDelay(getRemoveDelay(holder))
            .start()
    }

    override fun preAnimateAddImpl(holder: RecyclerView.ViewHolder?) {
        ViewCompat.setAlpha(holder!!.itemView, 0f)
        ViewCompat.setScaleX(holder.itemView, 1.5f)
        ViewCompat.setScaleY(holder.itemView, 1.5f)
    }

    override fun animateAddImpl(holder: RecyclerView.ViewHolder?) {
        ViewCompat.animate(holder!!.itemView)
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(addDuration)
            .setInterpolator(mInterpolator)
            .setListener(DefaultAddVpaListener(holder))
            .setStartDelay(getAddDelay(holder))
            .start()
    }
}