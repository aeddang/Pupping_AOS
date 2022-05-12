package com.raftgroup.pupping.scene.page.viewmodel

import android.content.pm.ActivityInfo
import com.lib.page.PageModel
import com.lib.page.PageObject
import com.raftgroup.pupping.R

class ActivityModel : PageModel{
    override var isPageInit: Boolean = false
    override var currentPageObject: PageObject? = null
    override fun getHome(idx: Int): PageObject = PageObject(PageID.Home.value, PageID.Home.position)
    override fun getPageExitMessage(): Int  = R.string.noticeAppExit

    private val homePages = arrayListOf<String>()
    override fun isHomePage(page: PageObject): Boolean {
        val f= homePages.indexOf(page.pageID)
        return f != -1
    }
    fun isHomePage(pageID: String): Boolean {
        val f= homePages.indexOf(pageID)
        return f != -1
    }

    override fun isBackStackPage(page:PageObject ):Boolean{
        return isHomePage(page)
    }

    override fun getPageOrientation(page: PageObject): Int {
        when(page.pageID){
            PageID.Picture.value -> return ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }
        return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private val disableHistoryPages = arrayOf(PageID.Intro, PageID.Login).map { it.value }
    override fun isHistoryPage(page: PageObject): Boolean {
        val f= disableHistoryPages.indexOf(page.pageID)
        return f == -1
    }

    private val fadeInPages = arrayOf(PageID.Intro, PageID.Picture).map { it.value }
    fun isFadeInPage(pageValue:String): Boolean {
        val f= fadeInPages.indexOf(pageValue)
        return f != -1
    }

    private val verticalPages = arrayListOf<String>()
    fun isVerticalPage(pageValue:String): Boolean {
        val f= verticalPages.indexOf(pageValue)
        return f != -1
    }

    private val useBottomTabPages = arrayOf(PageID.Explore, PageID.Home, PageID.My).map { it.value }
    fun useBottomTabPage(pageValue:String): Boolean {
        val f= useBottomTabPages.indexOf(pageValue)
        return f != -1
    }

}