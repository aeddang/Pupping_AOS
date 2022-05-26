package com.skeleton.component.view
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.appcompat.content.res.AppCompatResources
import com.lib.page.PageUI
import com.raftgroup.pupping.R

open class RoundView : PageUI {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { initialize(context, attrs)}
    @SuppressLint("CustomViewStyleable", "Recycle")
    override fun initialize(context: Context, attrs: AttributeSet?) {
        super.initialize(context, attrs)
        setBgColor()
        setOutline(selected)
    }

    fun setOutlineColor(@ColorRes color:Int ){
        val stroke:Float? = if (isSelected) activeStroke ?: defaultStroke  else defaultStroke
        this.setCorners(radius,
            stroke,color,shadowX, shadowY
        )
    }
    fun setRadius(@DimenRes r:Int? ){
        if (r == null){
            radius = 0f
        } else {
            radius = context.resources.getDimension(r)
        }
        setOutline(selected)
    }
    override var selected:Boolean? = null
        set(value) {
            field = value
            setOutline(value)
        }

}