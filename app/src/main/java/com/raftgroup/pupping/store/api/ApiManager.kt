package com.raftgroup.pupping.store.api

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lib.util.DataLog
import com.raftgroup.pupping.store.api.rest.ApiAdapter
import com.raftgroup.pupping.store.api.rest.UserAuth
import com.raftgroup.pupping.store.provider.manager.AccountManager
import com.skeleton.module.network.ErrorType
import com.skeleton.module.network.NetworkFactory
import com.skeleton.sns.SnsUser
import com.skeleton.sns.SnsUserInfo
import kotlinx.coroutines.runBlocking
enum class ApiStatus{
    Initate, Ready, Reflash, Error
}
enum class ApiEvent{
    Initate, Error, Join
}
class ApiManager(
        private val context: Context,
        private val networkFactory: NetworkFactory,
        private val interceptor: ApiInterceptor)
{
    private val appTag = javaClass.simpleName

    private var status = ApiStatus.Initate
    var event: MutableLiveData<ApiEvent?> = MutableLiveData<ApiEvent?>(null)
    val result = MutableLiveData<ApiSuccess<ApiType>?>()
    val error = MutableLiveData<ApiError<ApiType>?>()
    private var apiQs :ArrayList<ApiQ> = arrayListOf()
    private var apiBridge = ApiBridge(context, networkFactory, interceptor)

    private var snsUser:SnsUser? = null
    private var accountManager:AccountManager? = null
    fun setAccountManager(manager:AccountManager){
        accountManager = manager
    }

    fun initateApi(token:String, user:SnsUser){
        interceptor.accesstoken = token
        snsUser = user
        status = ApiStatus.Ready
        if (status != ApiStatus.Reflash) {
            event.value = ApiEvent.Initate
        }
        executeQ()
    }

    fun initateApi(user:SnsUser){
        snsUser = user
        executeQ()
    }

    private fun executeQ(){
        apiQs.forEach { load(it) }
        apiQs.clear()
    }
    fun clearApi(){
        interceptor.accesstoken = ""
        snsUser = null
        status = ApiStatus.Initate
    }


    fun load(apiQ: ApiQ){
        if (status != ApiStatus.Ready){
            apiQs.add(apiQ)
            return
        }
        when(apiQ.type){
            ApiType.AuthLogin -> {
                // loadQ 사용불가
                return }
            ApiType.AuthReflash -> {
                reflashToken()
                return
            }
            else -> {}
        }

        ApiAdapter { apiBridge.getUpdateUserProfile(apiQ, snsUser) }
            .onSuccess(
                { res->
                    //res?.let { DataLog.d(it,appTag) }
                    val data = res?.contents
                    val datas = res?.items
                    val success = ApiSuccess(apiQ.type, data ?: datas, apiQ.id, apiQ.isOptional, apiQ.contentID, apiQ.requestData)
                    if ( accountManager?.respondApi(success) != true ){
                        result.postValue(success)
                    }

                },
                { type , code , msg ->
                    val e = ApiError(apiQ.type, type, code, msg, apiQ.id, apiQ.isOptional)
                    DataLog.e(e,appTag)
                    if (code == ApiCode.invalidToken && apiQ.type != ApiType.AuthReflash) {
                        apiQs.add(apiQ)
                        reflashToken(e)
                    }else{
                        error.postValue(e)
                    }
                }
            )
    }
    fun joinAuth(user:SnsUser, info:SnsUserInfo?){

        status = ApiStatus.Reflash
        ApiAdapter { getJoinApi(user, info) }
            .onSuccess(
                { res->
                    res?.let { DataLog.d(it,appTag) }
                    val data = res?.contents as? UserAuth
                    if (data?.token == null){
                        val e = ApiError(ApiType.AuthLogin, ErrorType.API, code = ApiCode.notFound)
                        error.postValue(e)
                    }else{
                        val token = data.token!!
                        interceptor.accesstoken = token
                        status = ApiStatus.Ready
                        result.value = ApiSuccess(ApiType.AuthLogin, token ,"" )
                        event.value = ApiEvent.Join

                    }
                },
                { _ , _ , _ ->
                    val e = ApiError(ApiType.AuthLogin, ErrorType.API, code = ApiCode.notFound)
                    DataLog.e(e,appTag)
                    error.postValue(e)
                }
            )
    }
    private fun getJoinApi(user:SnsUser, info:SnsUserInfo?) = runBlocking {
        val params = java.util.HashMap<String, String>()
        params["providerType"] = user.snsType.apiCode()
        params["id"] = user.snsID
        params["password"] = user.snsToken
        info?.nickName?.let { params["name"] = it }
        info?.email?.let { params["email"] = it }
        apiBridge.auth.post( params = params )
    }

    fun reflashToken(originErr:ApiError<ApiType>? = null){
        if (status == ApiStatus.Reflash) return
        snsUser ?: return
        status = ApiStatus.Reflash
        ApiAdapter { getReflashApi(snsUser!!) }
            .onSuccess(
                { res->
                    val data = res?.contents as? UserAuth
                    if (data?.token == null){
                        val e = originErr ?: ApiError(ApiType.AuthLogin, ErrorType.API, code = ApiCode.notFound)
                        error.postValue(e)
                        status = ApiStatus.Initate
                    }else{
                        val token = data.token!!
                        interceptor.accesstoken = token
                        status = ApiStatus.Ready
                        result.value = ApiSuccess(ApiType.AuthReflash, token ,"" )
                        event.value = ApiEvent.Initate
                        executeQ()
                    }
                },
                { _ , _ , _ ->
                    val e = originErr ?: ApiError(ApiType.AuthLogin, ErrorType.API, code = ApiCode.notFound)
                    error.postValue(e)
                    status = ApiStatus.Ready
                }
            )
    }

    private fun getReflashApi(user:SnsUser) = runBlocking {
        val params = java.util.HashMap<String, String>()
        params["providerType"] = "Token"
        params["id"] = user.snsID
        params["password"] = interceptor.accesstoken
        interceptor.accesstoken = ""
        apiBridge.auth.reflash( params = params )
    }
}