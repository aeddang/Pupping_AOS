package com.raftgroup.pupping.store.provider

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.raftgroup.pupping.store.api.ApiError
import com.raftgroup.pupping.store.api.ApiQ
import com.raftgroup.pupping.store.api.ApiSuccess
import com.raftgroup.pupping.store.api.ApiType
import com.raftgroup.pupping.store.provider.model.User


class DataProvider {
    val user = User()
    val request = MutableLiveData<ApiQ?>()
    val result = MutableLiveData<ApiSuccess<ApiType>?>()
    val error = MutableLiveData<ApiError<ApiType>?>()

    fun requestData(q:ApiQ?){
        request.value = q
    }

    fun removeObserve(owner: LifecycleOwner){
        request.removeObservers(owner)
        result.removeObservers(owner)
        error.removeObservers(owner)
    }

    fun clearEvent(){
        request.value = null
        result.value = null
        error.value = null
    }
}