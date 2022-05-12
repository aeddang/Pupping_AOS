package com.skeleton.component.indicator
/*
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.CallSuper

import com.lib.page.PageComponent
import com.lib.page.PageUI
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.UiCircleIndicatorBinding


class CircleIndicator: PageComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)
    private val appTag = javaClass.simpleName
    private lateinit var binding: UiCircleIndicatorBinding
    @CallSuper
    override fun init(context: Context) {
        super.init(context)
        binding = UiCircleIndicatorBinding.inflate(LayoutInflater.from(context), this, true)
    }

    private var items:ArrayList<CircleItem> = arrayListOf()
    var count: Int = 0
        set(value) {
            field = value
            binding.indicatorBody.removeAllViews()
            items.clear()
            for (idx in 0 until value){
                val item = CircleItem(context)
                items.add(item)
                binding.indicatorBody.addView(item)
            }
        }

    var selectIdx:Int = 0
        set(value) {
            if (field in 0 until count) items[field].isSelected = false
            field = value
            if (value in 0 until count) items[value].isSelected = true
        }

    inner class CircleItem: PageUI {
        constructor(context: Context) : super(context)
        constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
        override fun getLayoutResID(): Int = R.layout.ui_circle_button
    }
}
*/
