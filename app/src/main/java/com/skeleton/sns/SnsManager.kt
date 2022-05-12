package com.skeleton.sns

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.lib.page.PageLifecycleUser
import com.lib.util.Log

class SnsManager(private val context: Context) : PageLifecycleUser {
    private val appTag = javaClass.simpleName

    val currentSnsType = MutableLiveData<SnsType?>()
    val user = MutableLiveData<SnsUser?>()
    val userInfo = MutableLiveData<SnsUserInfo?>()

    val respond = MutableLiveData<SnsResponds?>()
    val error = MutableLiveData<SnsError?>()

    private var currentManager:Sns? = null

    val fb:FaceBookManager = FaceBookManager()

    override fun setDefaultLifecycleOwner(owner: LifecycleOwner) {
        fb.respond.observe(owner, Observer{ res:SnsResponds? ->
            res ?: return@Observer
            onRespond(res)
        })
        fb.error.observe(owner, Observer{ err:SnsError? ->
            err ?: return@Observer
            onError(err)
        })

    }

    override fun disposeDefaultLifecycleOwner(owner: LifecycleOwner) {
        fb.respond.removeObservers(owner)
        fb.error.removeObservers(owner)
    }

    fun getManager(type:SnsType? = null) : Sns?{
        val cType = type ?: ( currentSnsType.value ?: return null )
        return when(cType) {
            SnsType.Fb -> fb
        }
    }

    private fun onRespond(res:SnsResponds){
        this.respond.value = res
        if (currentSnsType.value != null && res.type != currentSnsType.value) return
        when (res.event) {
            SnsEvent.Login -> {
                currentSnsType.value = res.type
                currentManager = getManager()
                user.value = res.data as? SnsUser
                Log.d(appTag, "login $user")
            }
            SnsEvent.Logout -> {
                Log.d(appTag,"logout ${currentSnsType.value}")
                currentSnsType.value = null
                user.value = null
                userInfo.value = null
                currentManager = null
            }
            SnsEvent.InvalidToken -> {
                Log.d(appTag,"invalidToken ${currentSnsType.value}")
                currentSnsType.value = null
                user.value = null
                userInfo.value = null
                currentManager = null
            }
            SnsEvent.GetProfile -> {
                userInfo.value = res.data as? SnsUserInfo
                Log.d(appTag,"getProfile $userInfo")
            }
            else->{}
        }
    }

    private fun onError(err:SnsError){
        this.error.value = err
        if (currentSnsType.value != null && err.type != currentSnsType.value) return
        if (err.event == SnsEvent.Login) {

        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        fb.onActivityResult(requestCode,resultCode, data)
        return false
    }

    fun requestLogin(type:SnsType) {
        getManager(type)?.requestLogin()
    }

    fun requestLogOut() {
        Log.d(appTag, "requestLogOut")
        currentManager?.requestLogOut()
    }
    fun requestAllLogOut() {
        fb.requestLogOut()
    }

    fun getAccessTokenInfo() {
        currentManager?.getAccessTokenInfo()
    }

    fun getUserInfo() {
        currentManager?.getUserInfo()
    }

    fun requestUnlink() {
        currentManager?.requestUnlink()
    }
}







