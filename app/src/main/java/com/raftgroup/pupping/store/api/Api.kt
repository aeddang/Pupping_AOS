package com.raftgroup.pupping.store.api

import androidx.lifecycle.LifecycleOwner
import com.google.gson.annotations.SerializedName
import com.raftgroup.pupping.store.api.ApiAction
import com.skeleton.module.network.ErrorType
import okhttp3.Interceptor
import java.io.IOException
import java.util.ArrayList
import kotlin.jvm.Throws

data class ApiResponse<T> (
    @SerializedName("contents") val contents: T? = null,
    @SerializedName("items") val items: List<T>? = null,
    @SerializedName("kind") val kind: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("code") val code: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("error") val error: String? = null
)


class ApiInterceptor : Interceptor {
    var accesstoken: String = ""
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val original = chain.request()
        val request = original.newBuilder()
        if (accesstoken.isNotEmpty()) {
            request.header("Authorization", "Bearer $accesstoken")
        } else {
            request.header("Authorization", "")
        }
        return chain.proceed(request.build())
    }
}

data class ApiQ(val id:String,  val type: ApiType,
    var query:HashMap<String,String>? = null,
    var body:HashMap<String, Any>? = null,
    var action: ApiAction? = null,
    var contentID:String = "",
    var isOptional:Boolean = false,
    var isLock:Boolean = false,
    var requestData:Any? = null
)

data class ApiSuccess<T>(
    val type:T, var data:Any?,
    val id: String? = null, val isOptional:Boolean = false,
    var contentID:String = "",
    val requestData:Any? = null
)
data class ApiError<T>(
    val type:T , val errorType:ErrorType ,
    val code:String?, val msg:String? = null,
    val id: String? = null,  val isOptional:Boolean = false
)
data class ApiGroup<T>(
    val type:T, var group: ArrayList<ApiSuccess<T>>,
    var complete:Int,
    var params: ArrayList<Map<String, Any?>?>? = null,
    val isSerial:Boolean = false,
    val owner: LifecycleOwner? = null)
{
    var process:Int = 0 ;private set
    fun finish():Boolean{
        complete --
        process ++
        return complete <= 0
    }
}



