package com.lib.view.adapter

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.lib.page.PageFragment

abstract class BasePageStatePagerAdapter<T>(fragmentManager: FragmentManager): FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    lateinit var pages: Array<T>

    abstract fun getPageFragment(position: Int): PageFragment

    fun setData(data:Array<T>): FragmentStatePagerAdapter {
        pages = data
        notifyDataSetChanged()
        return this
    }

    override fun getItem(position: Int): PageFragment {
        return getPageFragment(position)
    }

    override fun getCount(): Int {
        return pages.size
    }
}