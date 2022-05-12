package com.skeleton.sns

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.lib.util.DataLog
import com.lib.util.Log
import org.json.JSONObject

class FaceBookManager : Sns, FacebookCallback<LoginResult>{

    private val appTag = javaClass.simpleName
    val respond = MutableLiveData<SnsResponds?>()
    val error = MutableLiveData<SnsError?>()
    val type = SnsType.Fb

    var callbackManager: CallbackManager? = null; private set
    private var accessTokenTracker:AccessTokenTracker? = null
    private var profileTracker:ProfileTracker? = null
    private var accessToken:AccessToken? = null

    init {
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager,this)

        accessTokenTracker = object : AccessTokenTracker() {
            override fun onCurrentAccessTokenChanged(oldAccessToken: AccessToken?, currentAccessToken: AccessToken?) {
                accessTokenTracker?.stopTracking()
                currentAccessToken?.let { token ->
                    if (!token.isExpired) respond.value = SnsResponds(SnsEvent.GetToken, type, token.token)
                    else respond.value = SnsResponds(SnsEvent.InvalidToken, type)
                }
            }
        }

        profileTracker = object : ProfileTracker() {
            override fun onCurrentProfileChanged(oldProfile: Profile?, currentProfile: Profile?) {
                profileTracker?.stopTracking()
                currentProfile?.let {
                    val profile = SnsUserInfo(
                        it.name,
                        it.getProfilePictureUri(150, 150).path)
                    respond.value = SnsResponds(SnsEvent.GetProfile, type, profile)
                    /*
                    if (accessToken != null){
                        GraphRequest.newMeRequest(accessToken){ obj: JSONObject?, response: GraphResponse? ->
                            obj?.let { DataLog.d(obj, appTag) }
                            response?.let { DataLog.d(response, appTag) }
                            respond.value = SnsResponds(SnsEvent.GetProfile, type, profile)
                        }.executeAsync()

                    } else {
                        respond.value = SnsResponds(SnsEvent.GetProfile, type, profile)
                    }*/
                    return
                }
                error.value = SnsError(SnsEvent.GetProfile, type)
            }
        }
    }

    //FacebookCallback
    override fun onSuccess(loginResult: LoginResult) {
        loginResult.accessToken
        val user = SnsUser(
            type,
            loginResult.accessToken.userId,
            loginResult.accessToken.token
        )
        accessToken = loginResult.accessToken
        respond.value = SnsResponds(SnsEvent.Login, type, user)
    }
    //FacebookCallback
    override fun onCancel() {

    }
    //FacebookCallback
    override fun onError(exception: FacebookException) {
        Log.d(appTag, "onError ${exception.message}")
        error.value = SnsError(SnsEvent.Login, type, exception)
    }

    override fun destroy(){
        accessToken = null
        callbackManager = null
        accessTokenTracker?.stopTracking()
        profileTracker?.stopTracking()
        accessTokenTracker = null
        profileTracker = null
    }

    override fun getAccessTokenInfo() {
        accessTokenTracker?.startTracking()
    }

    override fun getUserInfo() {
        profileTracker?.startTracking()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return callbackManager?.onActivityResult(requestCode, resultCode, data) ?: false
    }

    override fun requestUnlink() {
        Log.e(appTag, "Not supported")
    }
    override fun requestLogin(fragment: Fragment) {
        Log.d(appTag, "requestLogin")
        LoginManager.getInstance().logIn(fragment, listOf("public_profile", "email"))
    }

    override fun requestLogOut() {
        Log.d(appTag, "requestLogOut")
        LoginManager.getInstance().logOut()
        respond.value = SnsResponds(SnsEvent.Logout, type)
        accessToken = null
    }

}