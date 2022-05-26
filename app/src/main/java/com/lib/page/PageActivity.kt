package com.lib.page
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.*
import android.net.ConnectivityManager.*
import android.os.Build
import android.os.Bundle
import android.transition.ChangeBounds
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.lib.util.Log
import com.lib.util.showCustomToast
import com.raftgroup.pupping.R
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.system.exitProcess


abstract class PageActivity : AppCompatActivity(), Page, PageRequestPermission, PageDelegate{
    private val appTag = javaClass.simpleName
    abstract fun getPageActivityPresenter(): PagePresenter
    abstract fun getPageActivityModel(): PageModel
    abstract fun getPageViewProvider(): PageProvider
    protected lateinit var activityModel : PageModel
    protected lateinit var viewProvider : PageProvider
    @IdRes abstract fun getPageAreaId(): Int
    @IdRes protected open fun getPageAreaTopId(): Int  = getPageAreaId()
    protected lateinit var pageArea: ViewGroup

    protected val historys = Stack<PageObject>  ()
    protected val popups = ArrayList<PageObject>()

    private var currentRequestPermissions = HashMap<Int, PageRequestPermission>()
    val pageAppViewModel = PageAppViewModel()

    val currentPage: PageObject?
        get(){
            return activityModel.currentPageObject
        }
    val currentTopPage: PageObject?
        get(){
            val closeablePopups = popups.filter { !it.isBottom  }
            return if( closeablePopups.isEmpty() ) currentPage else closeablePopups.last()
        }
    val lastPage: PageObject?
        get(){
            return if( popups.isEmpty() ) currentPage else popups.last()
        }
    val prevPage: PageObject?
        get(){
            return if( historys.isEmpty() ) null else historys.last()
        }
    val hasLayerPopup: Boolean
        get() = popups.find { it.isTop } != null


    @Suppress("DEPRECATION")
    var isFullScreen:Boolean = false
        set(value) {
            if( value == field ) return
            field = value
            if(field){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.insetsController?.hide(WindowInsets.Type.systemBars())
                } else {
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN
                    )
                    window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
                }
            }else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.insetsController?.show(WindowInsets.Type.systemBars())
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_VISIBLE)
                }

            }
        }

    @ColorRes
    var systemBarColor:Int = -1
        set(value) {
            if( value == -1 ) return
            field = value
            val c = this.applicationContext.getColor(value)
            window.navigationBarColor = c
            window.statusBarColor = c
        }

    @StyleRes
    var appTheme:Int = -1
        set(value) {
            if( value == -1 ) return
            field = value
            application.setTheme(value)
        }

    open fun onViewBinding(): View?  = null
    @SuppressLint("MissingPermission")
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getLayoutResID()?.let { setContentView(it) }
        onViewBinding()?.let { setContentView(it) }
        setupActivityResult()
        onCreatedView()
        val builder = NetworkRequest.Builder()
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(builder.build(), object : NetworkCallback() {

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                pageAppViewModel.networkStatus.postValue(PageNetworkStatus.Available)
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                pageAppViewModel.networkStatus.postValue(PageNetworkStatus.Lost)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                pageAppViewModel.networkStatus.postValue(PageNetworkStatus.Lost)
            }
        })
        pageAppViewModel.networkStatus.value = if( connectivityManager.isDefaultNetworkActive ) PageNetworkStatus.Available else PageNetworkStatus.Lost

    }


    @CallSuper
    protected open fun onCreatedView(){
        pageArea = findViewById(getPageAreaId())
        activityModel = getPageActivityModel()
        viewProvider = getPageViewProvider()
        getPageActivityPresenter().activity = this
        var prevSize:Rect = Rect()
        pageArea.viewTreeObserver.addOnGlobalLayoutListener {
            val origin = window.decorView.rootView
            val r = Rect()
            pageArea.getWindowVisibleDisplayFrame(r)
            if (prevSize.bottom == r.bottom && prevSize.right == r.right) return@addOnGlobalLayoutListener
            prevSize = r
            val heightDiff: Int = origin.height - (r.bottom - r.top)
            Log.d(appTag, "heightDiff $heightDiff")
            pageAppViewModel.event.value =
                if (heightDiff > 300) {
                    PageEvent(PageEventType.ShowKeyboard)
                }
                else{
                    PageEvent(PageEventType.HideKeyboard)
                }
        }
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        popups.clear()
        historys.clear()
        activityModel.currentPageObject = null
        currentRequestPermissions.clear()
    }

    /*
     interface override
    */
    @CallSuper
    open fun getFragment(pageObject: PageObject?): Fragment?{
        pageObject ?: return null
        return supportFragmentManager.findFragmentByTag(pageObject.fragmentID)
    }

    @CallSuper
    open fun getPageFragment(pageObject: PageObject?): PageViewFragment?{
        pageObject ?: return null
        val fragment = supportFragmentManager.findFragmentByTag(pageObject.fragmentID)
        return fragment as? PageViewFragment
    }
    @CallSuper
    open fun clearPageHistory(pageObject: PageObject? = null){
        if(pageObject == null) {
            historys.clear()
            return
        }
        var peek:PageObject? = null
        do {
            if(peek != null) historys.pop()
            peek = try { historys.peek() }catch (e: EmptyStackException){ null }
        } while (pageObject != peek  && !historys.isEmpty())
    }
    @CallSuper
    open fun openPopup(pageObject: PageObject, sharedElement: View?, transitionName: String?) {
        onOpenPopup(pageObject, sharedElement, transitionName)
    }
    @CallSuper
    open fun closePopup(pageObject: PageObject, isAni: Boolean) {
        onClosePopup(pageObject, isAni)
    }
    @CallSuper
    open fun closePopup(key: String, isAni: Boolean) {
        onClosePopup(key, isAni)
    }
    @CallSuper
    open fun closeAllPopup(isAni: Boolean, exceptions:List<String> = listOf()){
        onCloseAllPopup(isAni, exceptions)
    }
    @CallSuper
    open fun pageInit() {
        activityModel.isPageInit = true

    }
    @CallSuper
    open fun pageStart(pageObject: PageObject){
        onPageChange(pageObject, true)
    }
    @CallSuper
    open fun pageChange(
        pageObject: PageObject,
        sharedElement: View? = null,
        transitionName: String? = null
    ){
        onPageChange(pageObject, false, sharedElement, transitionName)
    }
    @CallSuper
    open fun goHome(idx: Int = 0){
        pageChange(activityModel.getHome(idx))
    }
    @CallSuper
    open fun goBack(pageObject: PageObject? = null){
        if(pageObject != null) clearPageHistory(pageObject)
        onBackPressed()
    }
    @CallSuper
    open fun finishApp(){ super.finish() }
    open fun loading(isRock: Boolean = false){}
    open fun loaded(){}

    @Suppress("DEPRECATION")
    @CallSuper
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        currentPage?.let {
            this.getFragment(it)?.onActivityResult(requestCode, resultCode, data)
        }
        popups.forEach { this.getFragment(it)?.onActivityResult(requestCode, resultCode, data) }
    }
    private lateinit var startActivityForResult: ActivityResultLauncher<Intent>; private set
    private var activityRequstId:Int = -1
    private fun setupActivityResult(){
        startActivityForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            getPageFragment(activityModel.currentPageObject)?.onActivityResult(activityRequstId, result.resultCode, result.data)
            popups.forEach { getPageFragment(it)?.onActivityResult(activityRequstId, result.resultCode, result.data) }
            activityRequstId = -1
        }
    }
    fun registActivityResult(intent:Intent, id:Int = -1){
        activityRequstId = id
        startActivityForResult.launch(intent)
    }

    /*
    BackPressed
    */
    fun superBackPressAction(){
        super.onBackPressed()
    }
    @CallSuper
    override fun onBackPressed() {
        val closeablePopups = popups.filter { !it.isLayer }
        if(closeablePopups.isNotEmpty()){
            val last = closeablePopups.last()
            val lastPopup = supportFragmentManager.findFragmentByTag(last.fragmentID) as? PageView
            lastPopup?.let { if( it.hasBackPressAction ) return }
            popups.remove(last)
            onClosePopup(last)
            return
        }
        activityModel.currentPageObject ?: return
        val currentPage = getPageFragment(activityModel.currentPageObject)
        if( currentPage?.hasBackPressAction == true ) return
        if( activityModel.isHomePage(activityModel.currentPageObject!!) )  onExitAction()
        else onBackPressedAction()
    }

    private var finalExitActionTime:Long = 0L
    private fun resetBackPressedAction() { finalExitActionTime = 0L }
    protected open fun onExitAction() {

        super.onBackPressed()
        /*
        val cTime =  Date().time
        if( abs(cTime - finalExitActionTime) < 3000L ) { exitProcess(-1) }
        else {
            finalExitActionTime = cTime
            Toast(this).showCustomToast(activityModel.getPageExitMessage(), this)
        }*/
    }

    protected open fun onBackPressedAction() {
        if( historys.isEmpty()) {
            if( activityModel.currentPageObject == null) goHome()
            else onExitAction()
        }else {
            onPageChange(historys.pop()!!, false, null, null, true)
        }
    }

    /*
    Page Transaction
    */
    @CallSuper
    protected open fun onWillChangePage(prevPage: PageObject?, nextPage: PageObject?){
        nextPage ?: return
        isFullScreen = activityModel.isFullScreenPage(nextPage)
        val willChangeOrientation = activityModel.getPageOrientation(nextPage)
        if (willChangeOrientation != -1 && requestedOrientation != willChangeOrientation) requestedOrientation = willChangeOrientation

        pageAppViewModel.event.value = PageEvent(
            PageEventType.WillChangePage,
            nextPage.pageID,
            nextPage
        )
    }

    private fun getWillChangePageFragment(pageObject: PageObject, isPopup: Boolean): PageViewFragment {
        pageObject.isPopup = isPopup
        onWillChangePage(lastPage, pageObject)
        if( activityModel.isBackStackPage(pageObject) ) {
            val backStackFragment = supportFragmentManager.findFragmentByTag(pageObject.fragmentID) as? PageFragment
            backStackFragment?.let {
                backStackFragment.pageObject = pageObject
                return backStackFragment
            }
        }
        val newFragment = viewProvider.getPageView(pageObject)
        newFragment.pageObject = pageObject
        return newFragment
    }

    private fun getSharedTransitionName(sharedElement: android.view.View, transitionName: String):String{
        val name = ViewCompat.getTransitionName(sharedElement)
        if(name == null) ViewCompat.setTransitionName(sharedElement, transitionName)
        return transitionName
    }
    protected open fun getSharedChange():Any { return ChangeBounds() }


    private fun onPageChange(
        pageObject: PageObject,
        isStart: Boolean = false,
        sharedElement: android.view.View? = null,
        transitionName: String? = null,
        isBack: Boolean = false
    ) {
        if( !isChangePageAble(pageObject) ) return
        if( activityModel.currentPageObject?.pageID == pageObject.pageID ) {
            if(pageObject.params == null){
                getPageFragment(activityModel.currentPageObject)?.onPageReload()
                return
            }else {
                val currentValues = activityModel.currentPageObject?.params?.map { it.toString() }
                val values = pageObject.params?.map { it.toString() }
                if(currentValues == values){
                    getPageFragment(activityModel.currentPageObject)?.onPageReload()
                    return
                }
            }
        }
        onCloseAllPopup(false , activityModel.getCloseExceptions())
        resetBackPressedAction()
        val willChangePage = getWillChangePageFragment(pageObject, false)
        if(activityModel.isChangedCategory(currentPage, pageObject)) willChangePage.onCategoryChanged(
            currentPage
        )
        willChangePage.setOnPageDelegate(this)
        getPageFragment(currentPage)?.onWillDestory(pageObject)
        val motionPage = if(isBack) activityModel.currentPageObject ?: pageObject else pageObject
        try {
            val transaction = supportFragmentManager.beginTransaction()
            if (isStart) {
                transaction.setCustomAnimations(
                    getPageStart(),
                    getPageOut(pageObject.pageID, false)
                )
            } else {
                if (sharedElement == null) {
                    val currentPos = activityModel.currentPageObject?.pageIDX ?: 9999
                    val isReverse = currentPos > pageObject.pageIDX
                   // if(isBack) isReverse = !isReverse
                    transaction.setCustomAnimations(
                        getPageIn(motionPage.pageID, isReverse), getPageOut(
                            motionPage.pageID,
                            isReverse
                        )
                    )
                } else {
                    transaction.setReorderingAllowed(true)
                    transitionName?.let {
                        transaction.addSharedElement(
                            sharedElement,
                            getSharedTransitionName(sharedElement, it)
                        )
                    }
                    willChangePage.pageFragment.sharedElementEnterTransition = getSharedChange()
                }
            }
            activityModel.currentPageObject?.let {
                if (activityModel.isBackStackPage(it)) transaction.addToBackStack(it.fragmentID)
            }
            transaction.replace(getPageAreaId(), willChangePage.pageFragment, pageObject.fragmentID)
            transaction.commit()
        }catch (e: IllegalStateException){ }

        if( !isBack ) {
            activityModel.currentPageObject?.let {
                if( activityModel.isHistoryPage(it) ) historys.push(it)
            }
        }
        if(activityModel.currentPageObject == null) pageAppViewModel.event.value = PageEvent(
            PageEventType.Init,
            pageObject.pageID,
            pageObject.params
        )
        pageAppViewModel.event.value = PageEvent(
            PageEventType.ChangePage,
            pageObject.pageID,
            pageObject.params
        )
        activityModel.currentPageObject = pageObject
    }

    protected open fun isChangePageAble(pageObject: PageObject):Boolean = true

    private var finalAddedPopupID:String? = null
    private var finalOpenPopupTime:Long = 0L
    private fun onOpenPopup(
        pageObject: PageObject,
        sharedElement: android.view.View?,
        transitionName: String?
    ) {
        if( !isChangePageAble(pageObject) ) return
        val cTime =  Date().time
        if( finalAddedPopupID == pageObject.pageID && (abs(cTime - finalOpenPopupTime) < 500 ) ) return
        finalAddedPopupID = pageObject.pageID
        finalOpenPopupTime = cTime
        resetBackPressedAction()
        val popup = getWillChangePageFragment(pageObject, true)
        popup.setOnPageDelegate(this)
        try{

            val transaction = supportFragmentManager.beginTransaction()
            if(sharedElement == null) {
                transaction.setCustomAnimations(
                    getPopupIn(pageObject.pageID), getPopupOut(
                        pageObject.pageID
                    )
                )
            }else {
                transaction.setReorderingAllowed(true)
                transitionName?.let { transaction.addSharedElement(
                    sharedElement, getSharedTransitionName(
                        sharedElement,
                        it
                    )
                ) }
                popup.pageFragment.sharedElementEnterTransition = getSharedChange()
                getPageFragment(activityModel.currentPageObject)?.let { transaction.hide(it.pageFragment) }
            }
            val areaID = if (pageObject.isTop) getPageAreaTopId() else getPageAreaId()
            transaction.add(areaID, popup.pageFragment, pageObject.fragmentID)
            transaction.commit()
            if(sharedElement != null) {
                getPageFragment(activityModel.currentPageObject)?.let { supportFragmentManager.beginTransaction().show(
                    it.pageFragment
                ).commit()}
            }
        } catch (e: IllegalStateException){ }
        popups.add(pageObject)
        pageAppViewModel.event.value = PageEvent(
            PageEventType.AddPopup,
            pageObject.pageID,
            pageObject.params
        )

    }
    private fun onCloseAllPopup(isAni: Boolean = false, exceptions:List<String> = listOf()) {

        val remainPopup:ArrayList<PageObject> = arrayListOf()
        try {
            val transaction = supportFragmentManager.beginTransaction()
            popups.forEach { p ->
                val f = exceptions.find { p.pageID == it }
                if (f != null) {
                    remainPopup.add(p)
                    return@forEach
                }
                getPageFragment(p)?.let { f ->
                    if (activityModel.isBackStackPage(p)) transaction.addToBackStack(p.fragmentID)
                    if (isAni) transaction.setCustomAnimations(
                        getPopupIn(p.pageID),
                        getPopupOut(p.pageID)
                    )
                    transaction.remove(f.pageFragment)
                }
                pageAppViewModel.event.value = PageEvent(
                    PageEventType.RemovePopup,
                    p.pageID,
                    p.params
                )
            }
            transaction.commitNow()
        }catch (e: IllegalStateException){ }

        popups.clear()
        popups.addAll(remainPopup)
        if (remainPopup.filter{ !it.isBottom }.isEmpty()){
            onWillChangePage(null, activityModel.currentPageObject)
        }else{
            onWillChangePage(null, popups.filter{ !it.isBottom }.last())
        }



    }
    fun onClosePopupId(id: String, isAni: Boolean = true){
        val f = popups.find { it.pageID == id }
        f?.let {
            onClosePopup(it, isAni)
        }
    }
    private fun onClosePopup(key: String, isAni: Boolean = true){
        val f = popups.find { it.key == key }
        f?.let {
            onClosePopup(it, isAni)
        }
    }
    private fun onClosePopup(pageObject: PageObject, isAni: Boolean = true){
        popups.remove(pageObject)
        val fragment = supportFragmentManager.findFragmentByTag(pageObject.fragmentID)
        fragment ?: return
        val pageFragment = fragment as? PageFragment
        pageFragment?.let { it.onRemoveTransactionStart() }
        val nextPage = if(popups.filter { !it.isBottom }.isNotEmpty()) popups.last() else activityModel.currentPageObject
        onWillChangePage(null, nextPage)
        try{
            val transaction = supportFragmentManager.beginTransaction()
            if( activityModel.isBackStackPage(pageObject)) transaction.addToBackStack(pageObject.fragmentID)
            if(isAni) transaction.setCustomAnimations(
                getPopupIn(pageObject.pageID), getPopupOut(
                    pageObject.pageID
                )
            )
            transaction.remove(fragment).commit()

        } catch (e: IllegalStateException){
            Log.i(appTag, "onClosePopup ${e.message}")
        }
        pageAppViewModel.event.value = PageEvent(
            PageEventType.RemovePopup,
            pageObject.pageID,
            pageObject.params
        )
    }

    @CallSuper
    override fun onAddedPage(pageObject: PageObject){
        pageAppViewModel.event.value = PageEvent(
            PageEventType.AddedPopup,
            pageObject.pageID,
            pageObject.params
        )
        getPageFragment(activityModel.currentPageObject)?.onPageAdded(pageObject)
        popups.forEach { getPageFragment(it)?.onPageAdded(pageObject) }
    }

    @CallSuper
    override fun onRemovedPage(pageObject: PageObject){
        if(pageObject.isPopup) pageAppViewModel.event.value = PageEvent(
            PageEventType.RemovedPopup,
            pageObject.pageID,
            pageObject.params
        )
        getPageFragment(activityModel.currentPageObject)?.onPageRemoved(pageObject)
        popups.forEach { getPageFragment(it)?.onPageRemoved(pageObject) }
    }

    @CallSuper
    override fun onTopPage(pageObject: PageObject) {
        onWillChangePage(null, pageObject)
    }

    @CallSuper
    override fun onBottomPage(pageObject: PageObject) {
        val nextPage = if(popups.filter { !it.isBottom }.isNotEmpty()) popups.last() else activityModel.currentPageObject
        onWillChangePage(null, nextPage)
    }

    @CallSuper
    override fun onEvent(pageObject: PageObject, type: String, data: Any?){
        val eventType = PageEventType.Event
        pageAppViewModel.event.value = PageEvent(eventType, pageObject.pageID, data, type)
        getPageFragment(activityModel.currentPageObject)?.onPageEvent(pageObject, type, data)
        popups.forEach { getPageFragment(it)?.onPageEvent(pageObject, type, data) }
    }

    /*
    Animation
     */
    @AnimRes protected open fun getPageStart(): Int { return android.R.anim.fade_in }
    @AnimRes protected open fun getPageIn(pageID: String, isBack: Boolean): Int { return if(isBack) android.R.anim.fade_in else android.R.anim.fade_out}
    @AnimRes protected open fun getPageOut(pageID: String, isBack: Boolean): Int { return if(isBack) android.R.anim.fade_out else android.R.anim.fade_in }
    @AnimRes protected open fun getPopupIn(pageID: String): Int { return android.R.anim.fade_in }
    @AnimRes protected open fun getPopupOut(pageID: String): Int { return android.R.anim.fade_in }

    /*
    Permission
    */
    open fun hasPermissions(permissions: Array<out String>): Pair<Boolean, List<Boolean>>? {
        //if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return null
        val permissionResults = ArrayList<Boolean>()
        var resultAll = true
        for (permission in permissions) {
            val grant =  checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
            permissionResults.add(grant)
            if( !grant ) resultAll = false
        }
        return Pair(resultAll, permissionResults)
    }

    open fun requestPermission(permissions: Array<out String>, requester: PageRequestPermission)
    {
        val grantResult = currentRequestPermissions.size
        currentRequestPermissions[grantResult] = requester
        //if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { requestPermissionResult(grantResult, true); return }
        hasPermissions(permissions)?.let {
            if ( !it.first ) requestPermissions(permissions, grantResult) else requestPermissionResult(
                grantResult,
                true
            )
        }
    }
    private fun requestPermissionResult(
        requestCode: Int,
        resultAll: Boolean,
        permissions: List<Boolean>? = null
    )
    {
        currentRequestPermissions[requestCode]?.onRequestPermissionResult(resultAll, permissions)
        currentRequestPermissions.remove(requestCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    )
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        hasPermissions(permissions)?.let { requestPermissionResult(requestCode, it.first, it.second) }
    }



}