package com.lib.view.adapter

import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper


abstract class SingleAdapter<T>(isViewMore:Boolean = false, pageSize:Int = -1): BaseAdapter<T>(isViewMore, pageSize) {
    abstract fun getListCell(parent: ViewGroup): ListItem<T>
    @CallSuper
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(getListCell(parent) as View)
    }
}