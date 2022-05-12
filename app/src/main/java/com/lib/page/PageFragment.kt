package com.lib.page

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class PageFragment: Fragment(), Page, PageViewFragment, PageViewCoroutine,  ViewTreeObserver.OnGlobalLayoutListener{
    protected var delegate:PageDelegate? = null
    protected val scope = PageCoroutineScope()
    override var lifecycleOwner: LifecycleOwner? = null
    override val pageFragment: Fragment get() = this
    override val hasBackPressAction: Boolean
        get(){
            val f = pageChileren?.find { it.hasBackPressAction }
            f ?: return false
            return true
        }

    final override var pageObject: PageObject? = null
        set(value) {
            if(field == value) return
            field = value
            field?.let {f->
                f.params?.let { onPageParams(it) }
            }
        }
    final override var pageViewModel: PageViewModel?  = null
        set(value) {
            if(field == value) return
            field = value
            value ?: return
            onPageViewModel(value)
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        pageChileren?.forEach { it.onActivityResult(requestCode, resultCode, data) }
    }

    @CallSuper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        getLayoutResID()?.let { return inflater.inflate( it , container, false) }
        return onViewBinding()
    }

    override fun getLayoutResID(): Int? = null
    open fun onViewBinding():View? = null

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()

    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.viewTreeObserver?.addOnGlobalLayoutListener (this )
        if(pageObject?.isPopup == true ) delegate?.onAddedPage(pageObject!!)
        pageChileren?.forEach { child ->
            pageObject?.params?.let{ child.onPageParams(it) }
            child.lifecycleOwner = this

        }
        scope.createJob()
        onCoroutineScope()
        pageViewModel?.onCreateView(this, pageObject)
        scope.launch {
            delay(transactionTime)
            onTransactionCompleted()
        }
    }

    @CallSuper
    override fun onGlobalLayout(){
        view?.viewTreeObserver?.removeOnGlobalLayoutListener( this )
        pageChileren?.forEach { child ->
            child.onGlobalLayout()
        }
    }

    @CallSuper
    override fun setOnPageDelegate(delegate: PageDelegate) {
        this.delegate = delegate
    }

    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        pageViewModel?.onDestroyView(this, pageObject)
        if( pageObject?.isPopup == true ) delegate?.onRemovedPage(pageObject!!)
        scope.destoryJob()
        pageChileren?.clear()
        pageViewModel = null
        delegate = null
        pageObject = null

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


    override fun onPause() {
        super.onPause()
        pageChileren?.forEach { it.onPagePause() }
    }

    override fun onResume() {
        super.onResume()
        pageChileren?.forEach { it.onPageResume() }
    }
}