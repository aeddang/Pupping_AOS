package com.lib.view.adapter

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

abstract class BaseViewPagerAdapter<V:View,T>: PagerAdapter() {

    var pages: Array<T>? = null; private set

    abstract fun getPageView(container: ViewGroup, position: Int):V

    fun setData(data:Array<T>?):PagerAdapter {
        pages = data
        notifyDataSetChanged()
        return this
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
    override fun instantiateItem(container: ViewGroup, position: Int):V {
        val v = getPageView(container, position)
        container.addView(v)
        return v
    }

    override fun isViewFromObject(view: View, key: Any): Boolean {
        return view == key
    }

    override fun getCount(): Int {
        return pages?.size ?: 0
    }
}