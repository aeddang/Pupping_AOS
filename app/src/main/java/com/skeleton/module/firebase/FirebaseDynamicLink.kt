package com.skeleton.module.firebase

import android.app.Activity
import android.content.Intent
import android.net.Uri

import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.lib.util.Log
import com.raftgroup.pupping.R


class FirebaseDynamicLink(val activity: Activity) {

    private var appTag = javaClass.simpleName
    private var delegate: Delegate? = null

    interface Delegate {
        fun onDetactedDeepLink(deepLink: Uri) {}
        fun onCreateDynamicLinkComplete(dynamicLink: String) {}
        fun onCreateDynamicLinkError() {}
    }

    fun setOnDynamicLinkListener(_delegate: Delegate?) {
        delegate = _delegate
    }

    init {
        initDynamicLink(activity.intent)
    }

    fun initDynamicLink(intent: Intent) {
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(activity) { pendingDynamicLinkData ->
                pendingDynamicLinkData?.let {
                    val deepLink = it.link
                    deepLink?.let { link -> delegate?.onDetactedDeepLink(link) }

                }
            }.addOnFailureListener(activity) { e ->
                Log.d(appTag, "getDynamicLink:onFailure", e)
            }
    }

    fun requestDynamicLink(title: String?, image: String?, desc: String?, qurryString: String?) {
        val longLink = "http://${activity.getString(R.string.fdl_domain)}/?$qurryString"
        val longLinkUri = Uri.parse(longLink)
        val prefix = "https://${activity.getString(R.string.fdl_link)}"
        Log.d(appTag, "longLinkUri ${longLinkUri.query}")
        Log.d(appTag, "prefix $prefix")
        val builder = DynamicLink.SocialMetaTagParameters.Builder()
        title?.let{ builder.setTitle(it) }
        image?.let{ builder.setImageUrl(Uri.parse(it)) }
        desc?.let{ builder.setDescription(it) }

        val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(longLinkUri)
            .setDomainUriPrefix(prefix)
            .setAndroidParameters(
                DynamicLink.AndroidParameters.Builder(activity.getString(R.string.app_id))
                    .build()
            )
            .setIosParameters(
                DynamicLink.IosParameters.Builder(activity.getString(R.string.ios_id))
                    //.setAppStoreId("123456789")
                    //.setMinimumVersion("1.0.0")
                    .build())

            .setSocialMetaTagParameters(
                builder.build()
            )
                /*
           .setGoogleAnalyticsParameters(
               DynamicLink.GoogleAnalyticsParameters.Builder()
                   .setSource("orkut")
                   .setMedium("social")
                   .setCampaign("example-promo")
                   .build())
           .setItunesConnectAnalyticsParameters(
               DynamicLink.ItunesConnectAnalyticsParameters.Builder()
                   .setProviderToken("123456")
                   .setCampaignToken("example-promo")
                   .build())
            */
            .buildShortDynamicLink()
            .addOnSuccessListener { result ->
                val shortLink = result.shortLink
                val flowchartLink = result.previewLink
                delegate?.onCreateDynamicLinkComplete("$prefix${shortLink?.path}")
            }.addOnFailureListener { error ->
                Log.d(appTag, "dynamicLink Error ${error.message}")
                delegate?.onCreateDynamicLinkError()
            }
    }
}

