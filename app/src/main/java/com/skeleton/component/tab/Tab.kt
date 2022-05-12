package com.skeleton.component.tab

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.annotation.CallSuper
import com.lib.page.PageComponent


abstract class Tab<T> : PageComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)
    private val appTag = javaClass.simpleName

    private var selectListener: SelectListener<T>? = null
    interface SelectListener <T> {
        fun onSelected(view: Tab<T>, id:T, idx:Int)
    }
    fun setOnSelectListener( listener:SelectListener<T>? ){ selectListener = listener }

    lateinit var tab:Array<View>
    abstract fun getTabMenu(): Array<View>
    protected lateinit var data:Array<T>
    abstract fun getIDData(): Array<T>
    private var isReady = false
    var selectedIdx:Int = -1 ; private set
    var selectedTab:View? = null
        private set(value) {
            if(selectedIdx != -1) onWillChangeSelected(selectedIdx)
            selectedTab?.isSelected = false
            field = value
            value?.let {
                it.isSelected = true
                selectedIdx = it.tag as? Int ?: 0
                onChangeSelected(selectedIdx)
            }
    }

    fun setSelect(id:T) {
        if(!isReady) return
        val idx = data.indexOf(id)
        setSelectIndex(idx)
    }

    fun setSelectIndex(idx:Int) {
        if(!isReady) return
        if(idx < 0) return
        if(idx >= tab.size) return
        selectedTab = tab[idx]
    }
    protected open fun onWillChangeSelected(idx:Int){}
    protected open fun onChangeSelected(idx:Int){}

    @CallSuper
    override fun init(context: Context) {
        super.init(context)
    }

    protected open fun createTab(){
        data = getIDData()
        tab = getTabMenu()
        tab.forEachIndexed { index, view ->
            view.tag = index
            view.setOnClickListener {
                selectedTab = view
                selectListener?.onSelected(this, data[selectedIdx], selectedIdx)
            }
        }
        isReady = true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isReady = false
        selectListener = null
    }


}

