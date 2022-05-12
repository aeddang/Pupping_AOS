package com.skeleton.component.webview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.webkit.*
import android.widget.Toast

@SuppressLint("SetJavaScriptEnabled")
open class CustomWebView : WebView {
    private val appTag = javaClass.simpleName
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    enum class LoadType {
        Html,
        Url
    }

    enum class Status {
        Loading,
        Loaded,
        Finish,
        Error
    }

    protected var webViewStatusListener:WebViewStatusListener? = null
    interface WebViewStatusListener {
        fun onStatusChanged(v: WebView, status:Status)
    }

    fun setOnWebViewStatusListener( listener:WebViewStatusListener? ) {
        webViewStatusListener = listener
    }

    protected var javascriptInterfaceListener:JavascriptInterfaceListener? = null
    interface JavascriptInterfaceListener {
        fun onJsAlert(v: WebView, url: String?, message: String?, result: JsResult?){}
        fun onJsConfirm(v: WebView, url: String?, message: String?, result: JsResult?){}
        fun onDeepLinkMessage(v: WebView, deepLink:String){}
    }

    fun setOnJavascriptInterfaceListener( listener:JavascriptInterfaceListener? ) {
        javascriptInterfaceListener = listener
    }

    init {
        settings.apply {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            loadWithOverviewMode = true
            builtInZoomControls = true
            displayZoomControls = false
            allowFileAccess = true
            allowContentAccess = true
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
            loadWithOverviewMode = true
            useWideViewPort = true
            @SuppressLint("ObsoleteSdkInt")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                webViewStatusListener?.onStatusChanged(this@CustomWebView, Status.Loaded)
            }


            override fun onPageFinished(view: WebView?, url: String?) {
                webViewStatusListener?.onStatusChanged(this@CustomWebView, Status.Finish)
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                webViewStatusListener?.onStatusChanged(this@CustomWebView, Status.Error)
            }

            @SuppressLint("DefaultLocale")
            override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
                webView.loadUrl(url)
                return true
            }
        }

        webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                javascriptInterfaceListener?.onJsAlert(this@CustomWebView, url, message, result)
                return super.onJsAlert(view, url, message, result)
            }

            override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                javascriptInterfaceListener?.onJsConfirm(this@CustomWebView, url, message, result)
                return super.onJsConfirm(view, url, message, result)
            }
        }
    }

    override fun destroy() {
        webViewStatusListener = null
        javascriptInterfaceListener = null
        try {
            stopLoading()
            clearCache(true)
            clearHistory()
        } catch ( e:Exception){

        }
        super.destroy()
    }

    fun load( article: String, loadType:LoadType = LoadType.Url) {
        webViewStatusListener?.onStatusChanged(this, Status.Loading)
        when (loadType ) { // url type 0이면 html 0이 아니면 http url 형식
            LoadType.Url -> loadUrl(article)
            LoadType.Html -> loadData(article, "text/html", "UTF-8")
        }
    }

    var webAppInterface:String?  = null
        set(value) {
            if(value == null && field!= null) removeJavascriptInterface(  field!! )
            else addJavascriptInterface(getWebAppInterface(), value!!)
            field = value
        }
    protected open fun getWebAppInterface():WebAppInterface = WebAppInterface(context)

    inner class WebAppInterface(private val mContext: Context) {
        /** Show a toast from the web page  */
        @JavascriptInterface
        fun showToast(toast: String) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show()
        }

        @JavascriptInterface
        fun callDeepLink(urlLink: String) {
            javascriptInterfaceListener?.onDeepLinkMessage(this@CustomWebView, urlLink)
        }

        @JavascriptInterface
        fun callBrowser(urlLink: String) { // 외부 웹 호출
            urlLink.let {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                mContext.startActivity(intent)
            }
        }
    }
}