package com.krm.recyclerviewanimation.animators

import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorListener
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.krm.recyclerviewanimation.animators.holder.AnimateViewHolder
import com.krm.recyclerviewanimation.internal.ViewHelper
import java.util.*
import kotlin.math.abs
import kotlin.math.max

abstract class BaseItemAnimator : SimpleItemAnimator() {
    private val mPendingRemovals =
        ArrayList<RecyclerView.ViewHolder>()
    private val mPendingAdditions =
        ArrayList<RecyclerView.ViewHolder>()
    private val mPendingMoves =
        ArrayList<MoveInfo>()
    private val mPendingChanges =
        ArrayList<ChangeInfo>()
    private val mAdditionsList =
        ArrayList<ArrayList<RecyclerView.ViewHolder>>()
    private val mMovesList =
        ArrayList<ArrayList<MoveInfo>>()
    private val mChangesList =
        ArrayList<ArrayList<ChangeInfo>>()
    private val mAddAnimations =
        ArrayList<RecyclerView.ViewHolder?>()
    private val mMoveAnimations =
        ArrayList<RecyclerView.ViewHolder?>()
    private val mRemoveAnimations =
        ArrayList<RecyclerView.ViewHolder?>()
    private val mChangeAnimations =
        ArrayList<RecyclerView.ViewHolder?>()

    @JvmField
    protected var mInterpolator: Interpolator = DecelerateInterpolator()

    private class MoveInfo(
        var holder: RecyclerView.ViewHolder,
        var fromX: Int,
        var fromY: Int,
        var toX: Int,
        var toY: Int
    )

    private class ChangeInfo private constructor(
        oldHolder: RecyclerView.ViewHolder,
        newHolder: RecyclerView.ViewHolder
    ) {
        var oldHolder: RecyclerView.ViewHolder? = oldHolder
        var newHolder: RecyclerView.ViewHolder? = newHolder
        var fromX = 0
        var fromY = 0
        var toX = 0
        var toY = 0

        constructor(
            oldHolder: RecyclerView.ViewHolder,
            newHolder: RecyclerView.ViewHolder,
            fromX: Int,
            fromY: Int,
            toX: Int,
            toY: Int
        ) : this(oldHolder, newHolder) {
            this.fromX = fromX
            this.fromY = fromY
            this.toX = toX
            this.toY = toY
        }

        override fun toString(): String {
            return "ChangeInfo{" +
                    "oldHolder=" + oldHolder +
                    ", newHolder=" + newHolder +
                    ", fromX=" + fromX +
                    ", fromY=" + fromY +
                    ", toX=" + toX +
                    ", toY=" + toY +
                    '}'
        }

    }

    fun setInterpolator(mInterpolator: Interpolator) {
        this.mInterpolator = mInterpolator
    }

    override fun runPendingAnimations() {
        val removalsPending = mPendingRemovals.isNotEmpty()
        val movesPending = mPendingMoves.isNotEmpty()
        val changesPending = mPendingChanges.isNotEmpty()
        val additionsPending = mPendingAdditions.isNotEmpty()
        if (!removalsPending && !movesPending && !additionsPending && !changesPending) {
            // nothing to animate
            return
        }
        // First, remove stuff
        for (holder in mPendingRemovals) {
            doAnimateRemove(holder)
        }
        mPendingRemovals.clear()
        // Next, move stuff
        if (movesPending) {
            val moves =
                ArrayList(
                    mPendingMoves
                )
            mMovesList.add(moves)
            mPendingMoves.clear()
            val mover = Runnable label@{
                val removed = mMovesList.remove(moves)
                if (!removed) {
                    // already canceled
                    return@label
                }
                for (moveInfo in moves) {
                    animateMoveImpl(
                        moveInfo.holder, moveInfo.fromX, moveInfo.fromY, moveInfo.toX,
                        moveInfo.toY
                    )
                }
                moves.clear()
            }
            if (removalsPending) {
                val view = moves[0].holder.itemView
                ViewCompat.postOnAnimationDelayed(view, mover, removeDuration)
            } else {
                mover.run()
            }
        }
        // Next, change stuff, to run in parallel with move animations
        if (changesPending) {
            val changes =
                ArrayList(
                    mPendingChanges
                )
            mChangesList.add(changes)
            mPendingChanges.clear()
            val changer = Runnable label@{
                val removed = mChangesList.remove(changes)
                if (!removed) {
                    // already canceled
                    return@label
                }
                for (change in changes) {
                    animateChangeImpl(change)
                }
                changes.clear()
            }
            if (removalsPending) {
                val holder = changes[0].oldHolder
                ViewCompat.postOnAnimationDelayed(holder!!.itemView, changer, removeDuration)
            } else {
                changer.run()
            }
        }
        // Next, add stuff
        if (additionsPending) {
            val additions =
                ArrayList(mPendingAdditions)
            mAdditionsList.add(additions)
            mPendingAdditions.clear()
            val adder = Runnable label@{
                val removed = mAdditionsList.remove(additions)
                if (!removed) {
                    // already canceled
                    return@label
                }
                for (holder in additions) {
                    doAnimateAdd(holder)
                }
                additions.clear()
            }
            if (removalsPending || movesPending || changesPending) {
                val removeDuration = if (removalsPending) removeDuration else 0
                val moveDuration = if (movesPending) moveDuration else 0
                val changeDuration = if (changesPending) changeDuration else 0
                val totalDelay =
                    removeDuration + max(moveDuration, changeDuration)
                val view = additions[0].itemView
                ViewCompat.postOnAnimationDelayed(view, adder, totalDelay)
            } else {
                adder.run()
            }
        }
    }

    protected open fun preAnimateRemoveImpl(holder: RecyclerView.ViewHolder?) {}
    protected open fun preAnimateAddImpl(holder: RecyclerView.ViewHolder?) {}
    protected abstract fun animateRemoveImpl(holder: RecyclerView.ViewHolder?)
    protected abstract fun animateAddImpl(holder: RecyclerView.ViewHolder?)
    private fun preAnimateRemove(holder: RecyclerView.ViewHolder) {
        ViewHelper.clear(holder.itemView)
        if (holder is AnimateViewHolder) {
            (holder as AnimateViewHolder).preAnimateRemoveImpl(holder)
        } else {
            preAnimateRemoveImpl(holder)
        }
    }

    private fun preAnimateAdd(holder: RecyclerView.ViewHolder) {
        ViewHelper.clear(holder.itemView)
        if (holder is AnimateViewHolder) {
            (holder as AnimateViewHolder).preAnimateAddImpl(holder)
        } else {
            preAnimateAddImpl(holder)
        }
    }

    private fun doAnimateRemove(holder: RecyclerView.ViewHolder) {
        if (holder is AnimateViewHolder) {
            (holder as AnimateViewHolder).animateRemoveImpl(
                holder,
                DefaultRemoveVpaListener(holder)
            )
        } else {
            animateRemoveImpl(holder)
        }
        mRemoveAnimations.add(holder)
    }

    private fun doAnimateAdd(holder: RecyclerView.ViewHolder) {
        if (holder is AnimateViewHolder) {
            (holder as AnimateViewHolder).animateAddImpl(holder, DefaultAddVpaListener(holder))
        } else {
            animateAddImpl(holder)
        }
        mAddAnimations.add(holder)
    }

    override fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
        endAnimation(holder)
        preAnimateRemove(holder)
        mPendingRemovals.add(holder)
        return true
    }

    protected fun getRemoveDelay(holder: RecyclerView.ViewHolder): Long {
        return abs(holder.oldPosition * removeDuration / 4)
    }

    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        endAnimation(holder)
        preAnimateAdd(holder)
        mPendingAdditions.add(holder)
        return true
    }

    protected fun getAddDelay(holder: RecyclerView.ViewHolder): Long {
        return abs(holder.adapterPosition * addDuration / 4)
    }

    override fun animateMove(
        holder: RecyclerView.ViewHolder,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int
    ): Boolean {
        var mFromX = fromX
        var mFromY = fromY
        val view = holder.itemView
        mFromX += ViewCompat.getTranslationX(holder.itemView).toInt()
        mFromY += ViewCompat.getTranslationY(holder.itemView).toInt()
        endAnimation(holder)
        val deltaX = toX - mFromX
        val deltaY = toY - mFromY
        if (deltaX == 0 && deltaY == 0) {
            dispatchMoveFinished(holder)
            return false
        }
        if (deltaX != 0) {
            ViewCompat.setTranslationX(view, -deltaX.toFloat())
        }
        if (deltaY != 0) {
            ViewCompat.setTranslationY(view, -deltaY.toFloat())
        }
        mPendingMoves.add(
            MoveInfo(
                holder,
                mFromX,
                mFromY,
                toX,
                toY
            )
        )
        return true
    }

    private fun animateMoveImpl(
        holder: RecyclerView.ViewHolder,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int
    ) {
        val view = holder.itemView
        val deltaX = toX - fromX
        val deltaY = toY - fromY
        if (deltaX != 0) {
            ViewCompat.animate(view).translationX(0f)
        }
        if (deltaY != 0) {
            ViewCompat.animate(view).translationY(0f)
        }
        mMoveAnimations.add(holder)
        val animation = ViewCompat.animate(view)
        animation.setDuration(moveDuration).setListener(object : VpaListenerAdapter() {
            override fun onAnimationStart(view: View) {
                dispatchMoveStarting(holder)
            }

            override fun onAnimationCancel(view: View) {
                if (deltaX != 0) {
                    ViewCompat.setTranslationX(view, 0f)
                }
                if (deltaY != 0) {
                    ViewCompat.setTranslationY(view, 0f)
                }
            }

            override fun onAnimationEnd(view: View) {
                animation.setListener(null)
                dispatchMoveFinished(holder)
                mMoveAnimations.remove(holder)
                dispatchFinishedWhenDone()
            }
        }).start()
    }

    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder,
        newHolder: RecyclerView.ViewHolder,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int
    ): Boolean {
        if (oldHolder === newHolder) {
            // Don't know how to run change animations when the same view holder is re-used.
            // run a move animation to handle position changes.
            return animateMove(oldHolder, fromX, fromY, toX, toY)
        }
        val prevTranslationX = ViewCompat.getTranslationX(oldHolder.itemView)
        val prevTranslationY = ViewCompat.getTranslationY(oldHolder.itemView)
        val prevAlpha = ViewCompat.getAlpha(oldHolder.itemView)
        endAnimation(oldHolder)
        val deltaX = (toX - fromX - prevTranslationX).toInt()
        val deltaY = (toY - fromY - prevTranslationY).toInt()
        // recover prev translation state after ending animation
        ViewCompat.setTranslationX(oldHolder.itemView, prevTranslationX)
        ViewCompat.setTranslationY(oldHolder.itemView, prevTranslationY)
        ViewCompat.setAlpha(oldHolder.itemView, prevAlpha)
        // carry over translation values
        endAnimation(newHolder)
        ViewCompat.setTranslationX(newHolder.itemView, -deltaX.toFloat())
        ViewCompat.setTranslationY(newHolder.itemView, -deltaY.toFloat())
        ViewCompat.setAlpha(newHolder.itemView, 0f)
        mPendingChanges.add(
            ChangeInfo(
                oldHolder,
                newHolder,
                fromX,
                fromY,
                toX,
                toY
            )
        )
        return true
    }

    private fun animateChangeImpl(changeInfo: ChangeInfo) {
        val holder = changeInfo.oldHolder
        val view = holder?.itemView
        val newHolder = changeInfo.newHolder
        val newView = newHolder?.itemView
        if (view != null) {
            mChangeAnimations.add(changeInfo.oldHolder)
            val oldViewAnim =
                ViewCompat.animate(view).setDuration(changeDuration)
            oldViewAnim.translationX(changeInfo.toX - changeInfo.fromX.toFloat())
            oldViewAnim.translationY(changeInfo.toY - changeInfo.fromY.toFloat())
            oldViewAnim.alpha(0f).setListener(object : VpaListenerAdapter() {
                override fun onAnimationStart(view: View) {
                    dispatchChangeStarting(changeInfo.oldHolder, true)
                }

                override fun onAnimationEnd(view: View) {
                    oldViewAnim.setListener(null)
                    ViewCompat.setAlpha(view, 1f)
                    ViewCompat.setTranslationX(view, 0f)
                    ViewCompat.setTranslationY(view, 0f)
                    dispatchChangeFinished(changeInfo.oldHolder, true)
                    mChangeAnimations.remove(changeInfo.oldHolder)
                    dispatchFinishedWhenDone()
                }
            }).start()
        }
        if (newView != null) {
            mChangeAnimations.add(changeInfo.newHolder)
            val newViewAnimation = ViewCompat.animate(newView)
            newViewAnimation.translationX(0f).translationY(0f).setDuration(changeDuration)
                .alpha(1f).setListener(object : VpaListenerAdapter() {
                    override fun onAnimationStart(view: View) {
                        dispatchChangeStarting(changeInfo.newHolder, false)
                    }

                    override fun onAnimationEnd(view: View) {
                        newViewAnimation.setListener(null)
                        ViewCompat.setAlpha(newView, 1f)
                        ViewCompat.setTranslationX(newView, 0f)
                        ViewCompat.setTranslationY(newView, 0f)
                        dispatchChangeFinished(changeInfo.newHolder, false)
                        mChangeAnimations.remove(changeInfo.newHolder)
                        dispatchFinishedWhenDone()
                    }
                }).start()
        }
    }

    private fun endChangeAnimation(
        infoList: MutableList<ChangeInfo>,
        item: RecyclerView.ViewHolder
    ) {
        for (i in infoList.indices.reversed()) {
            val changeInfo =
                infoList[i]
            if (endChangeAnimationIfNecessary(changeInfo, item)) {
                if (changeInfo.oldHolder == null && changeInfo.newHolder == null) {
                    infoList.remove(changeInfo)
                }
            }
        }
    }

    private fun endChangeAnimationIfNecessary(changeInfo: ChangeInfo) {
        if (changeInfo.oldHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.oldHolder)
        }
        if (changeInfo.newHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.newHolder)
        }
    }

    private fun endChangeAnimationIfNecessary(
        changeInfo: ChangeInfo,
        item: RecyclerView.ViewHolder?
    ): Boolean {
        var oldItem = false
        when {
            changeInfo.newHolder === item -> {
                changeInfo.newHolder = null
            }
            changeInfo.oldHolder === item -> {
                changeInfo.oldHolder = null
                oldItem = true
            }
            else -> {
                return false
            }
        }
        ViewCompat.setAlpha(item!!.itemView, 1f)
        ViewCompat.setTranslationX(item.itemView, 0f)
        ViewCompat.setTranslationY(item.itemView, 0f)
        dispatchChangeFinished(item, oldItem)
        return true
    }

    override fun endAnimation(item: RecyclerView.ViewHolder) {
        val view = item.itemView
        // this will trigger end callback which should set properties to their target values.
        ViewCompat.animate(view).cancel()
        for (i in mPendingMoves.indices.reversed()) {
            val moveInfo =
                mPendingMoves[i]
            if (moveInfo.holder === item) {
                ViewCompat.setTranslationY(view, 0f)
                ViewCompat.setTranslationX(view, 0f)
                dispatchMoveFinished(item)
                mPendingMoves.removeAt(i)
            }
        }
        endChangeAnimation(mPendingChanges, item)
        if (mPendingRemovals.remove(item)) {
            ViewHelper.clear(item.itemView)
            dispatchRemoveFinished(item)
        }
        if (mPendingAdditions.remove(item)) {
            ViewHelper.clear(item.itemView)
            dispatchAddFinished(item)
        }
        for (i in mChangesList.indices.reversed()) {
            val changes =
                mChangesList[i]
            endChangeAnimation(changes, item)
            if (changes.isEmpty()) {
                mChangesList.removeAt(i)
            }
        }
        for (i in mMovesList.indices.reversed()) {
            val moves =
                mMovesList[i]
            for (j in moves.indices.reversed()) {
                val moveInfo =
                    moves[j]
                if (moveInfo.holder === item) {
                    ViewCompat.setTranslationY(view, 0f)
                    ViewCompat.setTranslationX(view, 0f)
                    dispatchMoveFinished(item)
                    moves.removeAt(j)
                    if (moves.isEmpty()) {
                        mMovesList.removeAt(i)
                    }
                    break
                }
            }
        }
        for (i in mAdditionsList.indices.reversed()) {
            val additions = mAdditionsList[i]
            if (additions.remove(item)) {
                ViewHelper.clear(item.itemView)
                dispatchAddFinished(item)
                if (additions.isEmpty()) {
                    mAdditionsList.removeAt(i)
                }
            }
        }

        // animations should be ended by the cancel above.
        check(!(mRemoveAnimations.remove(item) && DEBUG)) { "after animation is cancelled, item should not be in " + "mRemoveAnimations list" }
        check(!(mAddAnimations.remove(item) && DEBUG)) { "after animation is cancelled, item should not be in " + "mAddAnimations list" }
        check(!(mChangeAnimations.remove(item) && DEBUG)) { "after animation is cancelled, item should not be in " + "mChangeAnimations list" }
        check(!(mMoveAnimations.remove(item) && DEBUG)) { "after animation is cancelled, item should not be in " + "mMoveAnimations list" }
        dispatchFinishedWhenDone()
    }

    override fun isRunning(): Boolean {
        return mPendingAdditions.isNotEmpty() ||
                mPendingChanges.isNotEmpty() ||
                mPendingMoves.isNotEmpty() ||
                mPendingRemovals.isNotEmpty() ||
                mMoveAnimations.isNotEmpty() ||
                mRemoveAnimations.isNotEmpty() ||
                mAddAnimations.isNotEmpty() ||
                mChangeAnimations.isNotEmpty() ||
                mMovesList.isNotEmpty() ||
                mAdditionsList.isNotEmpty() ||
                mChangesList.isNotEmpty()
    }

    private fun dispatchFinishedWhenDone() {
        if (!isRunning) {
            dispatchAnimationsFinished()
        }
    }

    override fun endAnimations() {
        var count = mPendingMoves.size
        for (i in count - 1 downTo 0) {
            val item =
                mPendingMoves[i]
            val view = item.holder.itemView
            ViewCompat.setTranslationY(view, 0f)
            ViewCompat.setTranslationX(view, 0f)
            dispatchMoveFinished(item.holder)
            mPendingMoves.removeAt(i)
        }
        count = mPendingRemovals.size
        for (i in count - 1 downTo 0) {
            val item = mPendingRemovals[i]
            dispatchRemoveFinished(item)
            mPendingRemovals.removeAt(i)
        }
        count = mPendingAdditions.size
        for (i in count - 1 downTo 0) {
            val item = mPendingAdditions[i]
            ViewHelper.clear(item.itemView)
            dispatchAddFinished(item)
            mPendingAdditions.removeAt(i)
        }
        count = mPendingChanges.size
        for (i in count - 1 downTo 0) {
            endChangeAnimationIfNecessary(mPendingChanges[i])
        }
        mPendingChanges.clear()
        if (!isRunning) {
            return
        }
        var listCount = mMovesList.size
        for (i in listCount - 1 downTo 0) {
            val moves =
                mMovesList[i]
            count = moves.size
            for (j in count - 1 downTo 0) {
                val moveInfo =
                    moves[j]
                val item = moveInfo.holder
                val view = item.itemView
                ViewCompat.setTranslationY(view, 0f)
                ViewCompat.setTranslationX(view, 0f)
                dispatchMoveFinished(moveInfo.holder)
                moves.removeAt(j)
                if (moves.isEmpty()) {
                    mMovesList.remove(moves)
                }
            }
        }
        listCount = mAdditionsList.size
        for (i in listCount - 1 downTo 0) {
            val additions = mAdditionsList[i]
            count = additions.size
            for (j in count - 1 downTo 0) {
                val item = additions[j]
                val view = item.itemView
                ViewCompat.setAlpha(view, 1f)
                dispatchAddFinished(item)
                //this check prevent exception when removal already happened during finishing animation
                if (j < additions.size) {
                    additions.removeAt(j)
                }
                if (additions.isEmpty()) {
                    mAdditionsList.remove(additions)
                }
            }
        }
        listCount = mChangesList.size
        for (i in listCount - 1 downTo 0) {
            val changes =
                mChangesList[i]
            count = changes.size
            for (j in count - 1 downTo 0) {
                endChangeAnimationIfNecessary(changes[j])
                if (changes.isEmpty()) {
                    mChangesList.remove(changes)
                }
            }
        }
        cancelAll(mRemoveAnimations)
        cancelAll(mMoveAnimations)
        cancelAll(mAddAnimations)
        cancelAll(mChangeAnimations)
        dispatchAnimationsFinished()
    }

    private fun cancelAll(viewHolders: List<RecyclerView.ViewHolder?>) {
        for (i in viewHolders.indices.reversed()) {
            ViewCompat.animate(viewHolders[i]!!.itemView).cancel()
        }
    }

    internal open class VpaListenerAdapter : ViewPropertyAnimatorListener {
        override fun onAnimationStart(view: View) {}
        override fun onAnimationEnd(view: View) {}
        override fun onAnimationCancel(view: View) {}
    }

    internal inner class DefaultAddVpaListener(var mViewHolder: RecyclerView.ViewHolder) :
        VpaListenerAdapter() {
        override fun onAnimationStart(view: View) {
            dispatchAddStarting(mViewHolder)
        }

        override fun onAnimationCancel(view: View) {
            ViewHelper.clear(view)
        }

        override fun onAnimationEnd(view: View) {
            ViewHelper.clear(view)
            dispatchAddFinished(mViewHolder)
            mAddAnimations.remove(mViewHolder)
            dispatchFinishedWhenDone()
        }

    }

    internal inner class DefaultRemoveVpaListener(var mViewHolder: RecyclerView.ViewHolder) :
        VpaListenerAdapter() {
        override fun onAnimationStart(view: View) {
            dispatchRemoveStarting(mViewHolder)
        }

        override fun onAnimationCancel(view: View) {
            ViewHelper.clear(view)
        }

        override fun onAnimationEnd(view: View) {
            ViewHelper.clear(view)
            dispatchRemoveFinished(mViewHolder)
            mRemoveAnimations.remove(mViewHolder)
            dispatchFinishedWhenDone()
        }

    }

    companion object {
        private const val DEBUG = false
    }

    init {
        supportsChangeAnimations = false
    }
}