package com.lib.page
import android.content.Intent
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.skeleton.module.Repository
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.coroutines.CoroutineContext

interface Page {
    @LayoutRes
    fun getLayoutResID(): Int?
}

data class PageObject(val pageID:String = "",
                      var pageIDX:Int = 0){
    var params:HashMap<String, Any?>? = null
    var isPopup = false ; internal set
    val key:String = UUID.randomUUID().toString()
    val fragmentID:String get() { return "$pageID$pageIDX"}
    var isTop = false
    var isLayer = false
    var isBottom = false
    fun addParam(key:String, value:Any?):PageObject{
        value ?: return this
        if (params == null) {
            params  = HashMap<String, Any?>()
        }
        params!![key] = value
        return this
    }

    fun getParamValue(key:String):Any?{
        if (params == null) return null
        return params!![key]
    }
}

interface PagePresenter {
    var isFullScreen:Boolean
    var hasLayerPopup:Boolean
    var systemBarColor:Int
    var appTheme:Int
    var activity:PageActivity
    val currentPage:PageObject?
    val currentTopPage:PageObject?
    val lastPage:PageObject?
    val prevPage:PageObject?
    val observable:PageAppViewModel
    fun getPageFragment(pageObject:PageObject?): PageView?
    fun goHome(idx:Int = 0): PagePresenter
    fun goBack(pageObject:PageObject?=null): PagePresenter
    fun clearPageHistory(pageObject:PageObject?=null): PagePresenter
    fun closePopup(key:String?,isAni:Boolean = true): PagePresenter
    fun closePopupId(id:String?,isAni:Boolean = true): PagePresenter
    fun closePopup(pageObject:PageObject,isAni:Boolean = true): PagePresenter
    fun closeAllPopup(isAni:Boolean = true): PagePresenter
    fun closeAllPopup(exception:String,  isAni:Boolean = true): PagePresenter
    fun closeAllPopup(exceptions:List<String>,  isAni:Boolean = true): PagePresenter
    fun openPopup(pageObject:PageObject, sharedElement: View? = null, transitionName:String? = null): PagePresenter
    fun pageInit(): PagePresenter
    fun pageStart(pageObject:PageObject): PagePresenter
    fun changePage(pageObject:PageObject, sharedElement: View? = null, transitionName:String? = null): PagePresenter
    fun hasPermissions( permissions: Array<out String> ): Pair< Boolean, List<Boolean>>?
    fun requestPermission( permissions: Array<out String>, requester:PageRequestPermission )
    fun loading(isRock:Boolean = false): PagePresenter
    fun loaded(): PagePresenter
    fun finishApp()
    fun superBackPressAction()
}

interface PageModel {
    var isPageInit:Boolean
    var currentPageObject:PageObject?
    fun getHome(idx:Int = 0):PageObject
    @StringRes
    fun getPageExitMessage(): Int
    fun isHomePage( page:PageObject ): Boolean
    fun isHistoryPage( page:PageObject ): Boolean = true
    fun isFullScreenPage( page:PageObject ): Boolean = false
    fun isBackStackPage( page:PageObject ): Boolean = true
    fun isChangedCategory(prevPage:PageObject?, nextPage:PageObject?):Boolean = false
    fun getPageOrientation( page:PageObject ): Int
    fun getCloseExceptions(): List<String> = listOf()
}

interface PageProvider {
    fun getPageView( pageObject:PageObject ): PageViewFragment
}

interface PageDelegate {
    fun onAddedPage(pageObject:PageObject)
    fun onRemovedPage(pageObject:PageObject)
    fun onBottomPage(pageObject:PageObject)
    fun onTopPage(pageObject:PageObject)
    fun onEvent(pageObject:PageObject, type:String, data:Any? = null)
}

interface PageLifecycleUser{
    fun setDefaultLifecycleOwner(owner: LifecycleOwner){}
    fun disposeDefaultLifecycleOwner(owner: LifecycleOwner){}
    fun disposeLifecycleOwner(owner: LifecycleOwner){}
}

interface PageView{
    var lifecycleOwner: LifecycleOwner?
    val transactionTime: Long get() = 500L
    val pageChileren:ArrayList<PageView>? get() = null
    val hasBackPressAction: Boolean get() = false
    fun setOnPageDelegate(delegate: PageDelegate){}
    fun onCategoryChanged(pageObject:PageObject?){}
    fun onPageEvent(pageObject:PageObject?, type:String, data:Any? = null){}
    fun onPageAdded(pageObject:PageObject){}
    fun onPageRemoved(pageObject:PageObject){}
    fun onPageReload(){}
    fun onPageParams(params:Map<String, Any?>):PageView {return this}
    fun onPageViewModel(vm:PageViewModel):PageView{return this}
    fun onGlobalLayout(){}
    fun onTransactionCompleted(){}
    fun onRemoveTransactionStart(){}
    fun onPagePause(){}
    fun onPageResume(){}
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {}
}
interface PageViewFragment : PageView{
    val pageFragment:Fragment
    var pageObject:PageObject?
    var pageViewModel:PageViewModel?

    fun onWillDestory(pageObject:PageObject?){}
}

interface PageViewModel {
    val repository:Repository
    val presenter:PagePresenter
    val observable:PageAppViewModel
    fun onCreateView(owner: LifecycleOwner, pageObject:PageObject?){}
    fun onDestroyView(owner: LifecycleOwner, pageObject:PageObject?){}
}

class PageCoroutineScope : CoroutineScope {

    lateinit var job: Job
    fun createJob(){
        job = SupervisorJob()
    }
    fun destoryJob(){
        cancelJob()
        cancel()
    }
    fun cancelJob(){
        job.cancel()
    }
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    val coroutineContextIO: CoroutineContext
        get() = job + Dispatchers.IO
}


interface PageViewCoroutine {
    fun onCoroutineScope(){}
}

interface PageRequestPermission {
    fun onRequestPermissionResult(resultAll:Boolean ,  permissions: List<Boolean>?){}
}




