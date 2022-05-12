package com.lib.util

import android.animation.ValueAnimator
import android.graphics.Point
import android.graphics.Rect
import android.util.Size
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import kotlin.math.roundToInt

object AnimationDuration{
    const val DEFAULT = 300L
    const val SHORT = 200L
    const val LONG = 500L
}


fun View.animateAlpha(targetValue:Float, isAutoVisible:Boolean = true, duration:Long = AnimationDuration.DEFAULT){
    if(alpha == targetValue && isAutoVisible){
        visibility = if( targetValue == 0f )  View.GONE else View.VISIBLE
        return
    }
    val ani = AlphaAnimation(alpha, targetValue)
    ani.duration = duration
    if(alpha < targetValue) alpha = targetValue
    ani.setAnimationListener( object: Animation.AnimationListener{
        override fun onAnimationEnd(animation: Animation?) {
            if( targetValue == 0f  && isAutoVisible) {
                visibility = View.GONE
            }
            if(alpha > targetValue) alpha = targetValue
        }
        override fun onAnimationStart(animation: Animation?) {
            if(isAutoVisible) visibility = View.VISIBLE
        }
        override fun onAnimationRepeat(animation: Animation?) {}
    })
    startAnimation(ani)
}

fun ViewGroup.setLayoutX(targetValue:Int, isReverse:Boolean = false):Int {
    val layout = layoutParams as ViewGroup.MarginLayoutParams
    val delta:Int
    if( isReverse) {
        delta = targetValue - layout.rightMargin
        layout.rightMargin = targetValue
    } else {
        delta = -(targetValue - layout.leftMargin)
        layout.leftMargin = targetValue
    }
    layoutParams = layout
    return delta
}

fun ViewGroup.setSize(targetValue:Size) {
    val layout = layoutParams as ViewGroup.LayoutParams
    if(targetValue.width != -1) layout.width = targetValue.width
    if(targetValue.height != -1) layout.height = targetValue.height
    layoutParams = layout
}

fun ViewGroup.animateX(targetValue:Int, isReverse:Boolean = false, duration:Long = AnimationDuration.DEFAULT):TranslateAnimation {
    val layout = layoutParams as ViewGroup.MarginLayoutParams
    val delta:Int = setLayoutX(targetValue, isReverse)
    val ani = TranslateAnimation(delta.toFloat(), 0f, 0f , 0f)
    ani.duration = duration
    return ani
}


fun ViewGroup.setLayoutY(targetValue:Int, isReverse:Boolean = false):Int {
    val layout = layoutParams as ViewGroup.MarginLayoutParams
    val delta:Int
    if( isReverse) {
        delta = targetValue - layout.bottomMargin
        layout.bottomMargin = targetValue
    } else {
        delta =  -(targetValue - layout.topMargin)
        layout.topMargin = targetValue
    }
    layoutParams = layout
    return delta
}

fun ViewGroup.animateY(targetValue:Int, isReverse:Boolean = false, duration:Long = AnimationDuration.DEFAULT):TranslateAnimation {
    val layout = layoutParams as ViewGroup.MarginLayoutParams
    val delta:Int = setLayoutY(targetValue, isReverse)
    val ani = TranslateAnimation(0f , 0f, delta.toFloat(), 0f)
    ani.duration = duration
    return ani
}

fun ViewGroup.setSize(targetValue:Int, isReverse:Boolean = false, duration:Long = AnimationDuration.DEFAULT):TranslateAnimation {
    val layout = layoutParams as ViewGroup.MarginLayoutParams
    val delta:Int = setLayoutY(targetValue, isReverse)
    val ani = TranslateAnimation(0f , 0f, delta.toFloat(), 0f)
    ani.duration = duration
    return ani
}

fun ViewGroup.animatePostion(targetValue:Point, isReverse:Boolean = false, duration:Long = AnimationDuration.DEFAULT):ValueAnimator {
    val layout = layoutParams as ViewGroup.MarginLayoutParams
    val startX =  if( isReverse ) layout.rightMargin else layout.leftMargin
    val startY = if( isReverse ) layout.bottomMargin else layout.topMargin
    val deltaX:Int
    val deltaY:Int
    if( isReverse) {
        deltaX = targetValue.x - layout.rightMargin
        layout.rightMargin = targetValue.x
    } else {
        deltaX = targetValue.x - layout.leftMargin
        layout.leftMargin = targetValue.x
    }
    if( isReverse) {
        deltaY = targetValue.y - layout.bottomMargin
        layout.bottomMargin = targetValue.y
    } else {
        deltaY = targetValue.y - layout.topMargin
        layout.topMargin = targetValue.y
    }
    val ani = ValueAnimator.ofFloat(0f, 1f)
    ani.addUpdateListener {
        val ratio = it.animatedValue as Float
        val posX = startX + ( ratio * deltaX ).roundToInt()
        val posY = startY + ( ratio * deltaY ).roundToInt()

        if( isReverse) layout.rightMargin = posX
        else layout.leftMargin = posX
        if( isReverse) layout.bottomMargin = posY
        else layout.topMargin = posY
        this.layoutParams = layout
    }
    ani.duration = duration
    return ani
}

fun ViewGroup.animateWidth(targetValue:Int, duration:Long = AnimationDuration.DEFAULT):ValueAnimator {
    val layout = layoutParams as ViewGroup.MarginLayoutParams
    val ani = ValueAnimator.ofInt(layout.width, targetValue)
    ani.addUpdateListener {
        layout.width = it.animatedValue as Int
        this.layoutParams = layout
    }
    ani.duration = duration
    return ani
}

fun ViewGroup.animateHeight(targetValue:Int, duration:Long = AnimationDuration.DEFAULT):ValueAnimator {
    val layout = layoutParams as ViewGroup.MarginLayoutParams
    val ani = ValueAnimator.ofInt(layout.height, targetValue)
    ani.addUpdateListener {
        layout.height = it.animatedValue as Int
        this.layoutParams = layout
    }
    ani.duration = duration
    return ani
}

fun ViewGroup.animateSize(targetValue:Size, duration:Long = AnimationDuration.DEFAULT):ValueAnimator {
    val layout = layoutParams as ViewGroup.MarginLayoutParams
    val startW = layout.width
    val startH = layout.height
    val deltaW = targetValue.width - layout.width
    val deltaH = targetValue.height - layout.height

    val ani = ValueAnimator.ofFloat(0f, 1f)
    ani.addUpdateListener {
        val ratio = it.animatedValue as Float
        if(targetValue.width != -1)  layout.width = startW + ( ratio * deltaW ).roundToInt()
        if(targetValue.height != -1) layout.height = startH + ( ratio * deltaH ).roundToInt()
        this.layoutParams = layout
    }
    ani.duration = duration
    return ani
}

fun ViewGroup.animateFrame(targetValue:Rect, isReverseX:Boolean = false, isReverseY:Boolean = false, duration:Long = AnimationDuration.DEFAULT):ValueAnimator {
    val layout = layoutParams as ViewGroup.MarginLayoutParams
    val startW = layout.width
    val startH = layout.height
    val deltaW = targetValue.right - layout.width
    val deltaH = targetValue.bottom - layout.height
    val startX =  if( isReverseX ) layout.rightMargin else layout.leftMargin
    val startY = if( isReverseY ) layout.bottomMargin else layout.topMargin
    val deltaX:Int
    val deltaY:Int
    if( isReverseX) {
        deltaX = targetValue.left - layout.rightMargin
        layout.rightMargin = targetValue.left
    } else {
        deltaX = targetValue.left - layout.leftMargin
        layout.leftMargin = targetValue.left
    }
    if( isReverseY) {
        deltaY = targetValue.top - layout.bottomMargin
        layout.bottomMargin = targetValue.top
    } else {
        deltaY = targetValue.top - layout.topMargin
        layout.topMargin = targetValue.top
    }

    val ani = ValueAnimator.ofFloat(0f, 1f)
    ani.addUpdateListener {
        val ratio = it.animatedValue as Float
        if(targetValue.right != -1) layout.width = startW + (ratio * deltaW).roundToInt()
        if(targetValue.bottom != -1) layout.height = startH + ( ratio * deltaH ).roundToInt()
        val posX = startX + ( ratio * deltaX ).roundToInt()
        val posY = startY + ( ratio * deltaY ).roundToInt()
        if( isReverseX) layout.rightMargin = posX
        else layout.leftMargin = posX
        if( isReverseY) layout.bottomMargin = posY
        else layout.topMargin = posY
        this.layoutParams = layout
    }
    ani.duration = duration
    return ani
}


