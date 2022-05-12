package com.raftgroup.pupping
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import com.google.android.libraries.places.api.Places
import com.lib.page.*
import com.lib.util.AppUtil
import com.lib.util.Log
import com.lib.util.PageLog
import com.lib.util.animateAlpha
import com.raftgroup.pupping.databinding.ActivityMainBinding
import com.raftgroup.pupping.scene.page.viewmodel.ActivityModel
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import com.raftgroup.pupping.scene.page.viewmodel.PageID
import com.raftgroup.pupping.store.PageRepository
import com.raftgroup.pupping.store.RepositoryEvent
import com.raftgroup.pupping.store.RepositoryStatus
import com.raftgroup.pupping.store.SystemEnvironment
import com.skeleton.sns.SnsManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint class MainActivity : PageActivity() {
    @Inject lateinit var repository: PageRepository
    @Inject lateinit var pageProvider: FragmentProvider
    @Inject lateinit var pageModel: ActivityModel
    @Inject lateinit var pagePresenter: PagePresenter
    @Inject lateinit var snsManager: SnsManager
    override fun getPageActivityPresenter(): PagePresenter = pagePresenter
    override fun getPageActivityModel() = pageModel
    override fun getPageViewProvider() = pageProvider
    override fun getPageAreaId() = R.id.area
    override fun getPageAreaTopId() = R.id.areaTop
    override fun getLayoutResID() = R.layout.activity_main
    private val appTag = javaClass.simpleName
    private val scope = PageCoroutineScope()
    private lateinit var binding: ActivityMainBinding
    private var isInit = false

    override fun onViewBinding(): View {
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
    }
    override fun onCreatedView() {
        super.onCreatedView()
        loaded()
        scope.createJob()
        AppUtil.getApplicationSignature(this)
        Places.initialize(applicationContext, "AIzaSyA-N_d8B57oZQ283byMk1veLPnAyBdABao")
        pageInit() // pagePresenter.pageInit() 가능
    }

    var isInitStart = true
    override fun pageInit() {
        super.pageInit()
        binding.bottomTab.lifecycleOwner = this
        PageLog.d("pageInit ",  appTag)
        repository.status.observe(this, Observer{ status ->
            when (status) {
                RepositoryStatus.Ready -> onStoreInit()
                else -> {}
            }
        })
        repository.event.observe(this, Observer{ evt ->
            when (evt) {
                RepositoryEvent.LoginUpdate -> onPageInit()
                else -> {}
            }
        })
        repository.setDefaultLifecycleOwner(this)
    }

    private fun onStoreInit(){
        PageLog.d("onStoreInit",  appTag)
        if ( SystemEnvironment.firstLaunch ) {
            PageLog.d("onStoreIni firstLaunch",  appTag)
            pageStart(pageProvider.getPageObject(PageID.Intro))
            return
        }
    }

    private fun onPageInit(){
        PageLog.d("onPageInit",  appTag)
        loaded()
        if(!repository.isLogin){
            isInit = false

            if ( pagePresenter.currentPage?.pageID != PageID.Login.value ) {
                if (SystemEnvironment.firstLaunch) {
                    PageLog.d("onPageInit go intro",  appTag)
                    pageStart(pageProvider.getPageObject(PageID.Intro))
                } else {
                    PageLog.d("onPageInit go login",  appTag)
                    pageStart(pageProvider.getPageObject(PageID.Login))
                }

            }
            return
        }
        isInit = true
        PageLog.d("onPageInit page start",  appTag)
        pageStart(pageProvider.getPageObject(PageID.My))

        /* push 이동 체크
        if !appObserverMove(self.appObserver.page) {
            self.pagePresenter.changePage(
                PageProvider.getPageObject(.explore)
            )
        }

        if self.appObserver.apns != nil  {
            self.appSceneObserver.event = .debug("apns exist")
            self.appSceneObserver.alert = .recivedApns
                    return
        }
        */
    }

    override fun isChangePageAble(pageObject: PageObject): Boolean {
        return super.isChangePageAble(pageObject)
    }

    override fun onWillChangePage(prevPage: PageObject?, nextPage: PageObject?) {
        super.onWillChangePage(prevPage, nextPage)
        nextPage ?: return
        if ( this.pageModel.useBottomTabPage(nextPage.pageID) ) binding.bottomTab.viewTab()
        else binding.bottomTab.hideTab()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        //if (currentPage != null) { deepLinkManager.changeActivityIntent(intent) }
    }


    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.destoryJob()
        repository.disposeDefaultLifecycleOwner(this)
        repository.disposeLifecycleOwner(this)
        //deepLinkManager.disposeDefaultLifecycleOwner(this)
        //deepLinkManager.disposeLifecycleOwner(this)
    }

    override fun loading(isRock: Boolean) {
        super.loading(isRock)
        if( isRock ){
            binding.loadingSpinnerLock.isLoading = true
            binding.dimed.animateAlpha(1f)
        }else{
            binding.loadingSpinner.isLoading = true
            binding.dimed.animateAlpha(0f)
        }
        Log.d(appTag, "loading")
    }

    override fun loaded() {
        super.loaded()
        Log.d(appTag, "loaded")
        binding.loadingSpinnerLock.isLoading = false
        binding.loadingSpinner.isLoading = false
        binding.dimed.animateAlpha(0f)
        Log.d(appTag, "loaded")

    }

    override fun getPageIn(pageID:String,isBack: Boolean): Int =
        if (pageModel.isHomePage(pageID)) android.R.anim.fade_in
        else {
            if (isBack) R.anim.slide_in_left
            else R.anim.slide_in_right
        }

    override fun getPageOut(pageID:String,isBack: Boolean): Int =
        if (pageModel.isHomePage(pageID)) android.R.anim.fade_out
        else {
            if (isBack) R.anim.slide_out_right
            else R.anim.slide_out_left
        }
    override fun getPopupIn(pageID:String): Int  =
        when {
            this.pageModel.isFadeInPage(pageID) -> android.R.anim.fade_in
            this.pageModel.isVerticalPage(pageID) -> R.anim.slide_in_down
            else -> R.anim.slide_in_right
        }

    override fun getPopupOut(pageID:String): Int =
        when {
            this.pageModel.isFadeInPage(pageID) -> android.R.anim.fade_out
            this.pageModel.isVerticalPage(pageID) -> R.anim.slide_out_down
            else -> R.anim.slide_out_right
        }

}

