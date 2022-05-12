package com.raftgroup.pupping.store

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.jaredrummler.android.device.DeviceName
import com.lib.page.PagePresenter
import com.lib.util.AppUtil
import com.lib.util.DataLog
import com.raftgroup.pupping.R
import com.raftgroup.pupping.scene.page.viewmodel.ActivityModel
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import com.raftgroup.pupping.store.api.*
import com.raftgroup.pupping.store.database.DataBaseManager
import com.raftgroup.pupping.store.mission.MissionManager
import com.raftgroup.pupping.store.preference.StoragePreference
import com.raftgroup.pupping.store.provider.DataProvider
import com.raftgroup.pupping.store.provider.manager.AccountManager
import com.skeleton.component.dialog.Alert
import com.skeleton.module.Repository
import com.skeleton.module.network.ErrorType
import com.skeleton.sns.SnsManager
import com.skeleton.sns.SnsUser
import com.skeleton.sns.SnsUserInfo

enum class RepositoryStatus{
    Initate, Ready
}

enum class RepositoryEvent{
    LoginUpdate
}

class PageRepository (ctx: Context,
                      val storage: StoragePreference,
                      val dataBaseManager: DataBaseManager,
                      val dataProvider: DataProvider,
                      val apiManager: ApiManager,
                      val pageModel: ActivityModel,
                      val pageProvider: FragmentProvider,
                      val pagePresenter: PagePresenter,
                      val shareManager:ShareManager,
                      val snsManager: SnsManager,
                      val topic:Topic,
                      val missionManager: MissionManager,
                      private val interceptor: ApiInterceptor


) : Repository(ctx){
    companion object {
        var deviceID:String = "" ; private set
    }
    private val appTag = "Repository"
    val status = MutableLiveData<RepositoryStatus>(RepositoryStatus.Initate)
    val event = MutableLiveData<RepositoryEvent?>(null)
    private val accountManager = AccountManager(dataProvider.user)

    fun clearEvent(){
        dataProvider.clearEvent()
    }

    @SuppressLint("HardwareIds")
    override fun setDefaultLifecycleOwner(owner: LifecycleOwner) {
        deviceID =  Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID)
        snsManager.setDefaultLifecycleOwner(owner)
        accountManager.setDefaultLifecycleOwner(owner)
        missionManager.setDefaultLifecycleOwner(owner)
        apiManager.setAccountManager(accountManager)
        dataProvider.request.observe(owner, Observer{apiQ: ApiQ?->
            apiQ?.let {
                apiManager.load(it)
                if(!it.isOptional) {
                    if (it.isLock) {
                        pagePresenter.loading(true)
                    } else {
                        pagePresenter.loading(false)
                    }
                }
                dataProvider.request.value = null
            }

        })

        apiManager.event.observe(owner, Observer{ evt ->
            evt?.let {
                when (it){
                    ApiEvent.Join -> {
                        DataLog.d("apiManager initate", appTag)
                        loginCompleted()
                        dataProvider.user.snsUser?.let { snsUser->
                            apiManager.initateApi(snsUser)
                        }
                    }
                    ApiEvent.Initate -> {
                        DataLog.d("apiManager initate", appTag)
                        loginCompleted()
                    }
                    ApiEvent.Error -> {
                        DataLog.d("apiManager error", appTag)
                        clearLogin()
                    }
                }
                apiManager.event.value = null
            }
        })

        apiManager.result.observe(owner, Observer{res: ApiSuccess<ApiType>? ->
            res?.let {
                dataProvider.result.value = it
                if (!res.isOptional) pagePresenter.loaded()
                apiManager.result.value = null
                dataProvider.result.postValue(null) //value = null
            }
        })
        apiManager.error.observe(owner, Observer{err: ApiError<ApiType>?->
            err?.let {
                dataProvider.error.value = it
                apiManager.error.value = null
                dataProvider.error.postValue(null)
                if (!it.isOptional) {
                    pagePresenter.loaded()
                    val msg =
                        if ( it.errorType != ErrorType.API ) ctx.getString(R.string.alertApiErrorServer)
                        else it.msg
                    val builder = Alert.Builder(pagePresenter.activity)
                    builder.setTitle(R.string.alertApi)
                    builder.setText(msg ?:  ctx.getString(R.string.alertApiErrorServer)).show()
                }
            }

        })
        setupSetting()
        autoSnsLogin()
        status.value = RepositoryStatus.Ready
    }

    override fun disposeDefaultLifecycleOwner(owner: LifecycleOwner) {
        snsManager.disposeDefaultLifecycleOwner(owner)
        dataProvider.removeObserve(owner)
        accountManager.disposeDefaultLifecycleOwner(owner)
        missionManager.disposeDefaultLifecycleOwner(owner)
        pagePresenter.observable.onDestroyView(owner)

    }

    override fun disposeLifecycleOwner(owner: LifecycleOwner){
        snsManager.disposeLifecycleOwner(owner)
        accountManager.disposeLifecycleOwner(owner)
        missionManager.disposeLifecycleOwner(owner)
        dataProvider.removeObserve(owner)
        pagePresenter.observable.onDestroyView(owner)
    }

    private fun setupSetting(){
        if (!storage.initate) {
            storage.initate = true
            SystemEnvironment.firstLaunch = true
        }
        SystemEnvironment.systemVersion = AppUtil.getAppVersion(ctx)
        SystemEnvironment.isTablet = AppUtil.isBigsizeDevice(ctx)
        if (storage.deviceModel.isEmpty()) {
            DeviceName.with(ctx).request { info, _ ->
                //val manufacturer = info.manufacturer // "Samsung"
                //val name = info.marketName // "Galaxy S8+"
                //val model = info.model // "SM-G955W"
                //val codename = info.codename // "dream2qltecan"
                val deviceName = info.name // "Galaxy S8+"
                storage.deviceModel = deviceName
                SystemEnvironment.model = deviceName
            }
        } else {
            SystemEnvironment.model = storage.deviceModel
        }

        dataProvider.user.registUser(
            storage.loginId,
            storage.loginToken,
            storage.loginType)
    }


    fun registerSnsLogin(user:SnsUser, info:SnsUserInfo?) {
        DataLog.d("registerSnsLogin $user", appTag)
        storage.loginId = user.snsID
        storage.loginToken = user.snsToken
        storage.loginType = user.snsType.apiCode()
        dataProvider.user.registUser(user)
        pagePresenter.loading(true)
        apiManager.joinAuth(user, info)
    }
    fun clearLogin() {
        DataLog.d("clearLogin", appTag)
        storage.loginId = ""
        storage.loginToken = ""
        storage.loginType = ""
        storage.authToken = ""
        apiManager.clearApi()
        dataProvider.user.clearUser()
        snsManager.requestAllLogOut()
        event.value = RepositoryEvent.LoginUpdate
        pagePresenter.loaded()

    }

    private fun autoSnsLogin() {
        val user = dataProvider.user.snsUser
        val token = storage.authToken

        DataLog.d("$user",appTag)
        DataLog.d("token " + (token ?: ""),appTag)
        if ( user != null && token.isNotEmpty() ) {
            apiManager.initateApi(token, user)
        } else {
            clearLogin()
        }

    }

    private fun loginCompleted() {
        DataLog.d("loginCompleted ${interceptor.accesstoken}", appTag)
        pagePresenter.loaded()
        storage.authToken = interceptor.accesstoken
        event.value = RepositoryEvent.LoginUpdate
        updateMyData()
    }
    fun updateMyData(isForce:Boolean = false){
        dataProvider.user.snsUser?.let { user->
            if (dataProvider.user.currentProfile.type.value == null || isForce) {
                val userQ = ApiQ(appTag, ApiType.GetUser, isOptional = true, requestData = user)
                apiManager.load(userQ)
            }
            if (dataProvider.user.pets.value?.isEmpty() != false || isForce) {
                val petQ = ApiQ(appTag, ApiType.GetPets, isOptional = true, requestData = user)
                apiManager.load(petQ)
            }
        }
    }

    val isLogin: Boolean get() {
        val token = storage.authToken
        DataLog.d("isLogin token $token", appTag)
        DataLog.d("isLogin token ${token.isNotEmpty()}", appTag)
        return token.isNotEmpty()
    }

}

