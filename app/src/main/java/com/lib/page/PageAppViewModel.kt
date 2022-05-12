package com.lib.page

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData


enum class PageEventType{
    Init,IntroCompleted,
    AddPopup, RemovePopup, ChangePage,
    AddedPopup, RemovedPopup,
    WillChangePage, ShowKeyboard, HideKeyboard,
    Event
}

enum class PageStatus{
    Free, Busy
}

enum class PageNetworkStatus{
    Available, Lost, Undefined
}

data class PageEvent(val type:PageEventType, val id: String = "", var data:Any? = null, val eventType:String? = null)

class PageAppViewModel {
    val event = MutableLiveData<PageEvent?>()
    val networkStatus = MutableLiveData<PageNetworkStatus>()
    val status = MutableLiveData<PageStatus>()

    init {
        status.value = PageStatus.Free
        networkStatus.value = PageNetworkStatus.Undefined
    }

    fun onDestroyView(owner: LifecycleOwner, pageObject: PageObject?=null) {
        event.removeObservers(owner)
        status.removeObservers(owner)
        networkStatus.removeObservers(owner)
    }
}