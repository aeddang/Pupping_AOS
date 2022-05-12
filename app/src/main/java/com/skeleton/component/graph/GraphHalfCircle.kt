package com.skeleton.component.graph


import android.content.Context
import android.util.AttributeSet
import com.lib.view.animate.AnimationUtil


class GraphHalfCircle: GraphCircle {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)
    private val appTag = javaClass.simpleName

    init {
        this.duration = AnimationUtil.ANIMATION_DURATION
        this.type = Type.HalfCircle
        this.startDegree = -180.0f
        this.totalDegree = 180.0
    }

    override fun onStart() {
        super.onStart()
        centerY = height.toFloat()
        val marginY = (height.toFloat() - size.height)
        rectF.set(rectF.left, marginY, rectF.right, size.height.toFloat()*2.0f)
    }

}