package com.raftgroup.pupping.scene.page.viewmodel

data class PageError<T>(val type:T, val code:String?, val msg:String? = null, val id: String? = null)


object PageAcvityEvent {
    const val bottomTabView = "bottomTabView"
    const val bottomTabHide = "bottomTabHide"
}

object PageParam {
    const val requestCode = "requestCode"
    const val title = "title"
    const val image = "image"
    const val type = "type"
    const val id = "id"
    const val subId = "subId"
    const val idx = "idx"
    const val data = "data"
    const val datas = "datas"
}