package uz.gita.kvartarena.utils

import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation

fun View.getCollapseAnimation(): Animation {
    val initialHeight = measuredHeight
    val animation: Animation = object : Animation() {
        override fun applyTransformation(
            interpolatedTime: Float,
            t: Transformation?
        ) {
            if (interpolatedTime == 1f) {
                visibility = View.GONE
            } else {
                layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                requestLayout()
            }
        }

        override fun willChangeBounds(): Boolean {
            return true
        }

        override fun cancel() {
            super.cancel()
            visibility = View.GONE
        }
    }

    animation.duration = 200
    return animation
}

fun View.getExpandAnimation(): Animation {
    measure(
        View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.AT_MOST)
    )
    val targetHeight = measuredHeight

    layoutParams.height = 1
    visibility = View.VISIBLE
    val animation: Animation = object : Animation() {
        override fun applyTransformation(
            interpolatedTime: Float,
            transformation: Transformation?
        ) {
            layoutParams.height =
                if (interpolatedTime == 1f) ViewGroup.LayoutParams.WRAP_CONTENT else (targetHeight * interpolatedTime).toInt()
            requestLayout()
        }

        override fun willChangeBounds(): Boolean {
            return true
        }

        override fun cancel() {
            super.cancel()
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }

    animation.duration = 200
    return animation
}