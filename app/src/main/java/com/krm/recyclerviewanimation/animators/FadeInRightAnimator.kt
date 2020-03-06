package com.krm.recyclerviewanimation.animators

import android.view.animation.Interpolator
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView

class FadeInRightAnimator : BaseItemAnimator {
    constructor()
    constructor(interpolator: Interpolator) {
        mInterpolator = interpolator
    }

    override fun animateRemoveImpl(holder: RecyclerView.ViewHolder?) {
        ViewCompat.animate(holder!!.itemView)
            .translationX(holder.itemView.rootView.width * .25f)
            .alpha(0f)
            .setDuration(removeDuration)
            .setInterpolator(mInterpolator)
            .setListener(DefaultRemoveVpaListener(holder))
            .setStartDelay(getRemoveDelay(holder))
            .start()
    }

    override fun preAnimateAddImpl(holder: RecyclerView.ViewHolder?) {
        ViewCompat.setTranslationX(holder!!.itemView, holder.itemView.rootView.width * .25f)
        ViewCompat.setAlpha(holder.itemView, 0f)
    }

    override fun animateAddImpl(holder: RecyclerView.ViewHolder?) {
        ViewCompat.animate(holder!!.itemView)
            .translationX(0f)
            .alpha(1f)
            .setDuration(addDuration)
            .setInterpolator(mInterpolator)
            .setListener(DefaultAddVpaListener(holder))
            .setStartDelay(getAddDelay(holder))
            .start()
    }
}