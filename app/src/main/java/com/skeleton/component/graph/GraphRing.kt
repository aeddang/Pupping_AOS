package com.skeleton.component.graph


import android.content.Context
import android.util.AttributeSet
import com.lib.view.animate.AnimationUtil


class GraphRing: GraphCircle {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)
    private val appTag = javaClass.simpleName

    init {
        this.isRing = true
        this.duration = AnimationUtil.ANIMATION_DURATION
        this.type = Type.Ring
    }


}