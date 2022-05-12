package com.lib.view.adapter

import android.annotation.SuppressLint
import android.os.Handler
import android.view.View
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import com.lib.model.InfinityPaginationData
import com.lib.util.DataLog
import com.lib.util.Log

interface ListItem<T>{
    fun setData(data:T, idx:Int = -1)
}


abstract class BaseAdapter<T>(private val isViewMore:Boolean = false, pageSize:Int = -1) : RecyclerView.Adapter<BaseAdapter.ViewHolder>() {
    class ViewHolder(val item: View) : RecyclerView.ViewHolder(item)
    interface Delegate {
        fun onBottom(page:Int, size:Int){}
        fun onTop(page:Int, size:Int){}
        fun onReflash(){}
    }
    private var delegate: Delegate? = null
    fun setOnAdapterListener(delegate:Delegate){
        this.delegate = delegate
    }

    private var viewEndHandler: Handler = Handler()
    private var viewEndRunnable: Runnable = Runnable {
        onViewEnd(paginationData.currentPage, paginationData.pageSize)
        delegate?.onBottom(paginationData.currentPage, paginationData.pageSize)
    }

    private var viewStartHandler: Handler = Handler()
    private var viewStartRunnable: Runnable = Runnable {
        onViewStart(paginationData.currentPage, paginationData.pageSize)
        delegate?.onTop(0, paginationData.pageSize)
    }
    var total = 0; private set
    private val appTag = javaClass.simpleName
    private var isBusyBottom = false
    private var isBusyTop = false
    private var isInit = false
    val paginationData:InfinityPaginationData<T> = InfinityPaginationData(pageSize)
    val isEmpty:Boolean
        get() {
            Log.d(appTag, "paginationData ${paginationData.data.size}")
            return paginationData.data.isEmpty()
        }

    @CallSuper
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        delegate = null
        viewEndHandler.removeCallbacks(viewEndRunnable)
        viewStartHandler.removeCallbacks(viewStartRunnable)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(data:T) {
        paginationData.add(data)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun reset(): RecyclerView.Adapter<ViewHolder> {
        isInit = true
        paginationData.reset()
        notifyDataSetChanged()
        return this
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setDataArray(data:Array<T>): RecyclerView.Adapter<ViewHolder> {
        isInit = true
        paginationData.reset()
        paginationData.addAll(data)
        notifyDataSetChanged()
        DataLog.d("setDataArray ${paginationData.data.size}", appTag)
        return this
    }

    fun addDataArray(data:Array<T>) {
        if (!isInit){
            setDataArray(data)
            return
        }
        val idx = paginationData.data.size
        paginationData.addAll(data)
        notifyItemRangeInserted(idx, data.size)
    }

    fun insertData(data:T, idx:Int = -1) {
        val position = if (idx == -1) paginationData.data.size else idx
        if(position == -1 || position > total) return
        paginationData.data.add(position, data)
        notifyItemInserted(position)
    }

    fun insertDataArray(data:Array<T>, idx:Int = -1) {
        val position = if (idx == -1) paginationData.data.size else idx
        if(position == -1 || position > total) return
        paginationData.data.addAll(position, data.toList())
        notifyItemRangeInserted(position, data.size)
    }

    fun updateData(data:T, idx:Int) {
        if(idx == -1 || idx >= total) return
        paginationData.data[idx] = data
        notifyItemChanged(idx)
    }

    fun removeData(data:T) {
        val position = paginationData.data.indexOf(data)
        removeData(position)
    }

    fun removeData(idx:Int) {
        if(idx == -1 || idx >= total) return
        paginationData.data.removeAt(idx)
        notifyItemRemoved(idx)
    }
    @SuppressLint("NotifyDataSetChanged")
    fun removeAllData() {
        paginationData.reset()
        notifyDataSetChanged()
    }

    fun getData(position: Int) = paginationData.data[position]
    fun getDatas() = paginationData.data

    open protected fun onViewStart(page:Int, size:Int){}
    open protected fun onViewEnd(page:Int, size:Int){}
    @CallSuper
    open fun onBottomComplete(dataArray:Array<T>) {
        addDataArray(dataArray)
        isBusyBottom = false
    }

    @CallSuper
    open fun onTopComplete() {
        isBusyTop  = false
    }

    override fun getItemCount():Int {
        total = paginationData.data.size
        return total
    }

    open fun onBindData(item:View? ,data:T) {}

    @CallSuper
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item =  holder.item
        val citem = item  as? ListItem<T>?
        val data = paginationData.data[position]
        citem?.setData(data, position)
        onBindData(item , data)
        DataLog.d("position $position", appTag)
        DataLog.d("total $total", appTag)
        if(position == total-1 && isViewMore && paginationData.isPageable && !isBusyBottom) {
            isBusyBottom = true
            paginationData.next()
            viewEndHandler.post(viewEndRunnable)

        }else if(position == 0 && !isBusyTop) {
            isBusyTop = true
            viewStartHandler.post(viewStartRunnable)
        }
    }


}