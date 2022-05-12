package com.raftgroup.pupping.store.preference

import android.content.Context
import com.lib.module.CachedPreference
import com.raftgroup.pupping.BuildConfig
import com.raftgroup.pupping.store.TopicCategory

class StoragePreference(context: Context) : CachedPreference(context, PreferenceName.SETTING + BuildConfig.BUILD_TYPE) {
    companion object {
        private const val VS = "1.000"
        private const val initate = "initate" + VS
        private const val retryPushToken = "retryPushToken" + VS
        private const val loginType = "loginType" + VS
        private const val loginToken = "loginToken" + VS
        private const val loginId = "loginId" + VS

        private const val authToken = "authToken" + VS
        private const val deviceModel = "deviceModel" + VS

        private const val TOPIC = "topic_"
    }
    var initate:Boolean
        get(){ return get(StoragePreference.initate, false) as Boolean }
        set(value:Boolean){ put(StoragePreference.initate, value) }

    var retryPushToken:String
        get(){ return get(StoragePreference.retryPushToken, "") as String }
        set(value:String){ put(StoragePreference.retryPushToken, value) }

    var loginType:String
        get(){ return get(StoragePreference.loginType, "") as String }
        set(value:String){ put(StoragePreference.loginType, value) }

    var loginToken:String
        get(){ return get(StoragePreference.loginToken, "") as String }
        set(value:String){ put(StoragePreference.loginToken, value) }

    var loginId:String
        get(){ return get(StoragePreference.loginId, "") as String }
        set(value:String){ put(StoragePreference.loginId, value) }

    var authToken:String
        get(){ return get(StoragePreference.authToken, "") as String }
        set(value:String){ put(StoragePreference.authToken, value) }

    var deviceModel:String
        get(){ return get(StoragePreference.deviceModel, "") as String }
        set(value:String){ put(StoragePreference.deviceModel, value) }


    fun subscribeTopic(topic:TopicCategory, isSubscribe:Boolean){
        put(TOPIC +topic.name, isSubscribe)
    }
    fun isSubscribe(topic:TopicCategory):Boolean{
        return get(TOPIC +topic.name, false) as Boolean
    }


}