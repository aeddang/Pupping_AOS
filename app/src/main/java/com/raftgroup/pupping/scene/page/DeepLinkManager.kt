package com.raftgroup.pupping.scene.page

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

import com.raftgroup.pupping.scene.page.viewmodel.PageID
import com.raftgroup.pupping.store.PageRepository
import com.lib.model.IwillGo
import com.lib.model.WhereverYouCanGo
import com.lib.page.PageLifecycleUser
import com.lib.util.Log
import com.lib.util.showCustomToast
import com.skeleton.module.firebase.FirebaseDynamicLink
import com.raftgroup.pupping.store.Shareable


class DeepLinkManager(val activity: Activity, val repo: PageRepository) : FirebaseDynamicLink.Delegate, PageLifecycleUser {
    private var appTag = javaClass.simpleName
    private var dynamicLink: FirebaseDynamicLink = FirebaseDynamicLink(activity)

    init {
        dynamicLink.setOnDynamicLinkListener(this)
    }
    override fun setDefaultLifecycleOwner(owner: LifecycleOwner){
        repo.shareManager.share.observe(owner, Observer{shareable: Shareable?->
            shareable ?: return@Observer
            sendSns(
                String(),
                shareable.image,
                shareable.text,
                shareable.pageID.value,
                shareable.pageID.position,
                shareable.params,
                shareable.isPopup
                    )

            repo.shareManager.share.value = null
        })
    }

    override fun disposeDefaultLifecycleOwner(owner: LifecycleOwner){
        repo.shareManager.share.removeObservers(owner)
    }

    fun changeActivityIntent(intent: Intent) {
        dynamicLink.initDynamicLink(intent)
    }

    override fun onDetactedDeepLink(deepLink: Uri) {
        Log.d("DeepLinkManager", "onDetactedDeepLink()  come:  ${deepLink}")
        deepLink.query?.let {
            goQueryPage(it)
        }
    }

    override fun onCreateDynamicLinkComplete(dynamicLink: String) {
        repo.pagePresenter.loaded()
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.putExtra(Intent.EXTRA_TEXT, dynamicLink)
        shareIntent.type = "text/plain"
        activity.startActivity(Intent.createChooser(shareIntent, "share"))
    }

    override fun onCreateDynamicLinkError() {
        repo.pagePresenter.loaded()
        Toast(activity).showCustomToast("DynamicLinkError", activity)
    }

    fun sendSns(
        title: String?,
        image: String?,
        desc: String?,
        pageID: String = PageID.Home.value,
        pageIDX: Int = 9999,
        param: HashMap<String, Any?>? = null,
        isPopup: Boolean = false
    ) {
        val queryString = WhereverYouCanGo.stringfyQurryIwillGo(pageID, pageIDX, param, isPopup)
        dynamicLink.requestDynamicLink(title, image, desc, queryString)
    }

    fun goQueryPage(query: String?) {
        query ?: return
        val iwillGo = WhereverYouCanGo.parseQurryIwillGo(query)
        goPage(iwillGo)
    }


    fun goPage(iwillGo: IwillGo) {
        Log.d(appTag, "goPage $iwillGo")
        val pageObj = iwillGo.page
        pageObj ?: return
        if( repo.pageModel.isHomePage(pageObj.pageID) ) pageObj.isPopup = false
        repo.pagePresenter.changePage(pageObj)
    }

}

