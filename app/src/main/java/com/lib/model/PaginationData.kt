package com.lib.model

import java.util.ArrayList

data class PaginationData<T>(val initPage:Int = 0, val isLoop:Boolean = false) {

    var currentPage = initPage
    var totalCount:Int = 0
    var data = ArrayList<T>()
        set (newData) {
            currentPage = initPage
            field = newData
            totalCount = newData.size
        }

    fun prev(): Boolean {
        var changePage = currentPage - 1
        if(changePage < 0) {
            if(isLoop) {
                changePage = totalCount - 1
            } else {
                return false
            }
        }
        currentPage = changePage
        return true
    }

    fun next(): Boolean {
        var changePage = currentPage + 1
        if(changePage >= totalCount) {
            if(isLoop) {
                changePage = 0
            } else {
                return false
            }
        }
        currentPage = changePage
        return true
    }
}