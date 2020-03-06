package com.krm.recyclerviewanimation.animators

import android.view.animation.Interpolator
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView

class ScaleInBottomAnimator : BaseItemAnimator {
    constructor() {}
    constructor(interpolator: Interpolator) {
        mInterpolator = interpolator
    }

    override fun preAnimateRemoveImpl(holder: RecyclerView.ViewHolder?) {
        //        ViewCompat.setPivotY(holder.itemView, holder.itemView.getHeight());
        holder!!.itemView.pivotY = holder.itemView.height.toFloat()
    }

    override fun animateRemoveImpl(holder: RecyclerView.ViewHolder?) {
        ViewCompat.animate(holder!!.itemView)
            .scaleX(0f)
            .scaleY(0f)
            .setDuration(removeDuration)
            .setInterpolator(mInterpolator)
            .setListener(DefaultRemoveVpaListener(holder))
            .setStartDelay(getRemoveDelay(holder))
            .start()
    }

    override fun preAnimateAddImpl(holder: RecyclerView.ViewHolder?) {
        //        ViewCompat.setPivotY(holder.itemView, holder.itemView.getHeight());
        holder!!.itemView.pivotY = holder.itemView.height.toFloat()
        ViewCompat.setScaleX(holder.itemView, 0f)
        ViewCompat.setScaleY(holder.itemView, 0f)
    }

    override fun animateAddImpl(holder: RecyclerView.ViewHolder?) {
        ViewCompat.animate(holder!!.itemView)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(addDuration)
            .setInterpolator(mInterpolator)
            .setListener(DefaultAddVpaListener(holder))
            .setStartDelay(getAddDelay(holder))
            .start()
    }
}