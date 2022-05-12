package com.raftgroup.pupping.scene.page.viewmodel
import com.lib.page.PageProvider
import com.lib.page.PageObject
import com.lib.page.PageViewFragment
import com.raftgroup.pupping.scene.page.explore.PageExplore
import com.raftgroup.pupping.scene.page.home.PageHome
import com.raftgroup.pupping.scene.page.intro.PageIntro
import com.raftgroup.pupping.scene.page.login.PageLogin
import com.raftgroup.pupping.scene.page.my.PageHealthModify
import com.raftgroup.pupping.scene.page.my.PageMy
import com.raftgroup.pupping.scene.page.my.PageProfileModify
import com.raftgroup.pupping.scene.page.my.PageProfileRegist
import com.raftgroup.pupping.scene.page.popup.PageHistory
import com.raftgroup.pupping.scene.page.popup.PagePicture
import com.raftgroup.pupping.scene.page.popup.PagePictureList
import com.raftgroup.pupping.scene.page.popup.PageProfile
import com.raftgroup.pupping.scene.page.report.PageReport


class FragmentProvider : PageProvider{
    fun getPageObject(pageID: PageID) :PageObject {
        val obj = PageObject(pageID.value, pageID.position)
        when(pageID){
            //PageID.HOME -> obj.addParam( PageParam.data, PageHomeData().raftGroupData()
            else -> {}
        }
        return obj
    }
    fun getPageTitle(pageID: PageID) :String{
        return when(pageID){
            else -> "Page Title"
        }
    }

    override fun getPageView(pageObject: PageObject): PageViewFragment {
        return when(pageObject.pageID){
            PageID.Intro.value -> PageIntro()
            PageID.Login.value -> PageLogin()
            PageID.Home.value ->  PageHome()
            PageID.Explore.value ->  PageExplore()
            PageID.My.value ->  PageMy()
            PageID.ProfileRegist.value -> PageProfileRegist()
            PageID.ProfileModify.value -> PageProfileModify()
            PageID.HealthModify.value -> PageHealthModify()
            PageID.PictureList.value -> PagePictureList()
            PageID.Picture.value -> PagePicture()
            PageID.Profile.value -> PageProfile()
            PageID.History.value -> PageHistory()
            PageID.Report.value -> PageReport()
            else -> PageIntro()
        }
    }
}

enum class PageID(val value: String, val position: Int = 9999){
    Intro("Intro", 1),
    Login("Login", 2),
    Home("Home", 100),
    Explore("Explore", 300),
    My("My", 400),
    ProfileRegist("ProfileRegist", 999),
    ProfileModify("ProfileModify", 999),
    HealthModify("HealthModify", 999),
    PictureList("PictureList", 999),
    Picture("Picture", 999),
    Profile("Profile", 999),
    History("History", 999),
    Report("Report", 999)
}


