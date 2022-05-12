package com.raftgroup.pupping.store.api.rest

import com.raftgroup.pupping.store.api.ApiResponse
import com.skeleton.module.network.NetworkAdapter

class ApiAdapter(getData: ()->ApiResponse<*>? ) : NetworkAdapter<ApiResponse<*>>(null, getData) {

    fun withRespondId(id:String): ApiAdapter {
        responseId = id
        return this
    }

    override fun onReceive(response: ApiResponse<*>?) {
        response ?: return super.onReceive(response)
        if ( response.error == null ) {
            super.onReceive(response)
        }else{
            val code = response.code ?: ""
            onApiError(code , response.error)
        }
    }
}




