package com.lib.model

import java.util.*

data class InfinityPaginationData<T>(val pageSize:Int = 10) {

    var currentPage = 0
    var isPageable = false
    var data = ArrayList<T>()

    fun reset() {
        currentPage = 0
        isPageable = false
        data = ArrayList()
    }

    fun add(item: T) {
        data.add(item)
    }

    fun addAll(addData:Array<T>): Int {
        data.addAll(addData)
        isPageable = addData.size >= pageSize
        return data.size
    }

    fun next(): Int {
        currentPage ++
        return currentPage
    }
}
