package com.lib.page
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import androidx.lifecycle.LifecycleOwner
import com.lib.util.Log
import com.raftgroup.pupping.R

abstract class PageComponent : FrameLayout, Page, PageView, PageViewCoroutine{
    constructor(context: Context): super(context) { init(context) }
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) { init(context) }
    protected val scope = PageCoroutineScope()
    protected open fun init(context: Context) {
        getLayoutResID()?.let { LayoutInflater.from(context).inflate(it, this, true) }
    }

    override fun getLayoutResID(): Int? = null
    override var lifecycleOwner: LifecycleOwner? = null
        set(value) {
            field = value
            pageChileren?.forEach { it.lifecycleOwner = value }
           // Log.d("PageComponent", "lifecycleOwner $lifecycleOwner")
            value ?: return
            onLifecycleOwner(value)
        }
    override val hasBackPressAction: Boolean
        get(){
            val f = pageChileren?.find { it.hasBackPressAction }
            f ?: return false
            return true
        }

    protected open fun onLifecycleOwner(owner: LifecycleOwner){}

    @CallSuper
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        scope.createJob()
        onCoroutineScope()
    }

    @CallSuper
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        scope.destoryJob()
    }

    @CallSuper
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        pageChileren?.forEach { it.onActivityResult(requestCode, resultCode, data) }
    }

    @CallSuper
    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
        pageChileren?.forEach { it.onTransactionCompleted() }
    }

    @CallSuper
    override fun onCategoryChanged(pageObject: PageObject?) {
        super.onCategoryChanged(pageObject)
        pageChileren?.forEach { it.onCategoryChanged(pageObject) }
    }

    @CallSuper
    override fun onPageAdded(pageObject: PageObject) {
        super.onPageAdded(pageObject)
        pageChileren?.forEach { it.onPageAdded(pageObject) }
    }

    @CallSuper
    override fun onPageViewModel(vm: PageViewModel):PageView {
        super.onPageViewModel(vm)
        pageChileren?.forEach { it.onPageViewModel(vm) }
        return this
    }

    @CallSuper
    override fun onPageParams(params: Map<String, Any?>):PageView {
        super.onPageParams(params)
        pageChileren?.forEach { it.onPageParams(params) }
        return this
    }

    @CallSuper
    override fun onPageEvent(pageObject: PageObject?, type: String, data: Any?) {
        super.onPageEvent(pageObject, type, data)
        pageChileren?.forEach { it.onPageEvent(pageObject, type, data) }
    }

    @CallSuper
    override fun onPageReload() {
        super.onPageReload()
        pageChileren?.forEach { it.onPageReload() }
    }

    @CallSuper
    override fun onPageRemoved(pageObject: PageObject) {
        super.onPageRemoved(pageObject)
        pageChileren?.forEach { it.onPageRemoved(pageObject) }
    }

    @CallSuper
    override fun onPagePause() {
        pageChileren?.forEach { it.onPagePause() }
    }

    @CallSuper
    override fun onPageResume() {
        pageChileren?.forEach { it.onPageResume() }
    }
}