package com.krm.recyclerviewanimation.animators

import android.view.animation.OvershootInterpolator
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView

class OvershootInLeftAnimator : BaseItemAnimator {
    private val mTension: Float

    constructor() {
        mTension = 2.0f
    }

    constructor(mTension: Float) {
        this.mTension = mTension
    }

    override fun animateRemoveImpl(holder: RecyclerView.ViewHolder?) {
        ViewCompat.animate(holder!!.itemView)
            .translationX(-holder.itemView.rootView.width.toFloat())
            .setDuration(removeDuration)
            .setListener(DefaultRemoveVpaListener(holder))
            .setStartDelay(getRemoveDelay(holder))
            .start()
    }

    override fun preAnimateAddImpl(holder: RecyclerView.ViewHolder?) {
        ViewCompat.setTranslationX(
            holder!!.itemView,
            -holder.itemView.rootView.width.toFloat()
        )
    }

    override fun animateAddImpl(holder: RecyclerView.ViewHolder?) {
        ViewCompat.animate(holder!!.itemView)
            .translationX(0f)
            .setDuration(addDuration)
            .setListener(DefaultAddVpaListener(holder))
            .setInterpolator(OvershootInterpolator(mTension))
            .setStartDelay(getAddDelay(holder))
            .start()
    }
}