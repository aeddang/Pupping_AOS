package com.skeleton.module.network
import com.lib.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

abstract class NetworkFlow<T>(var responseId:String?, val apis: Array<T?>)  {
    companion object{
        const val TAG = "NetworkAdapter"
    }
    interface ApiInterface<T>{
        fun onReceive(response: T?, id:String?) {}
        fun onFail(type: ErrorType, code:String, id:String?) {}
        fun onApiError(type: ErrorType, code:String, msg: String?, id:String?) {}
        fun onFinish(responseAll:ArrayList<T?>?,id:String?){}
    }
    protected var responseAll = ArrayList<T?>()
    protected var apiInterface: ApiInterface<T>? = null
    protected var job: Job? = null
    fun onSuccess(
        success: (ArrayList<T?>?, id:String?) -> Unit,
        error: (type: ErrorType, code:String,  msg: String?, id:String?) -> Unit
    ) {
        onStart(
            object : ApiInterface<T> {
                override fun onReceive(response: T?, id:String?) {  }
                override fun onFail(type: ErrorType, code:String, id:String?) {  error(type, code, null, id) }
                override fun onApiError(type: ErrorType, code:String, msg: String?, id:String?) {  error(type, code, msg, id) }
                override fun onFinish(responseAll:ArrayList<T?>?,id:String?) { success(responseAll, id) }
            })
    }

    fun onSuccess(
        success: (T?, id:String?) -> Unit,
        apiError: (type: ErrorType, code:String, msg: String?,id:String?) -> Unit,
        fail: (type: ErrorType, code:String,id:String?) -> Unit,
        finished: ((responseAll:ArrayList<T?>?,id:String?) -> Unit?)? = null
    ) {
        onStart(
            object : ApiInterface<T> {
                override fun onReceive(response: T?, id:String?) { success(response, id) }
                override fun onFail(type: ErrorType, code:String, id:String?) {  fail(type, code, id) }
                override fun onApiError(type: ErrorType, code:String, msg: String?, id:String?) {  apiError(type, code, msg, id) }
                override fun onFinish(responseAll:ArrayList<T?>?, id:String?) { if ( finished != null)  finished(responseAll, id) }
        })
    }


    private fun onStart(apiInterface: ApiInterface<T>?) {
        this.apiInterface = apiInterface

        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                flow {
                    apis.forEach { emit(it) }
                }
                .map { onReceive(it) }
                .flowOn(Dispatchers.Main).collectLatest {
                      onFinish()
                }

            } catch (e: CancellationException) {
                Log.d(TAG, e)
                onCancel()

            } catch (e: HttpException) {
                Log.e(NetworkAdapter.TAG, e)
                apiInterface?.onFail(ErrorType.HTTP, e.code().toString(), responseId)
                onFinish()

            } catch (e: UnknownHostException) {
                Log.e(NetworkAdapter.TAG, e)
                apiInterface?.onFail(ErrorType.HOST, HttpStatusCode.HOST, responseId)
                onFinish()

            } catch (e: SocketTimeoutException){
                Log.e(NetworkAdapter.TAG, e)
                apiInterface?.onFail(ErrorType.TIME_OUT, HttpStatusCode.TIME_OUT, responseId)
                onFinish()
            } catch (e: Exception) {
                Log.d(NetworkAdapter.TAG, e)
                apiInterface?.onFail(ErrorType.EXCEPTION, HttpStatusCode.EXCEPTION, responseId)
                onFinish()
            }

        }
    }

    protected open fun onReceive(response: T?)  {
        apiInterface?.onReceive(response, responseId)
        responseAll.add(response)
    }
    protected open fun onFail(type:ErrorType, code:String)  {
        responseAll.add(null)
        apiInterface?.onFail(type, code, responseId)
    }
    protected fun onApiError(code:String, msg:String? = null)  {
        responseAll.add(null)
        apiInterface?.onApiError(ErrorType.API,code,msg, responseId)
    }

    protected fun onFinish()  {
        job?.cancel()
        job = null
        apiInterface?.onFinish(responseAll, responseId)
    }

    protected fun onCancel()  {
        job?.cancel()
        job = null
    }

    protected fun retry() {
        onStart(apiInterface)
    }
}