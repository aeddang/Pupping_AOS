package com.raftgroup.pupping.store

import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.lib.page.PagePresenter
import com.raftgroup.pupping.store.preference.StoragePreference

enum class TopicCategory{
    All
}

class Topic(
        val pagePresenter: PagePresenter,
        private val settingPreference: StoragePreference
){

    private var appTag = javaClass.simpleName

    fun subscribe(cate: TopicCategory) {
        val prev = settingPreference.isSubscribe(cate)
        if (prev) return
        settingPreference.subscribeTopic(cate, true)
        Firebase.messaging.subscribeToTopic(cate.name)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.d(appTag, "subscribeToTopic $cate")
                }else{
                    Log.d(appTag, "subscribeToTopic error $cate")
                    settingPreference.subscribeTopic(cate, false)
                }
            }
    }

    fun unsubscribe(cate: TopicCategory) {
        val prev = settingPreference.isSubscribe(cate)
        if (!prev) return
        settingPreference.subscribeTopic(cate, false)
        Firebase.messaging.unsubscribeFromTopic(cate.name)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.d(appTag, "unsubscribeToTopic $cate")
                }else{
                    Log.d(appTag, "unsubscribeToTopic error $cate")
                    settingPreference.subscribeTopic(cate, true)
                }
            }
    }
}