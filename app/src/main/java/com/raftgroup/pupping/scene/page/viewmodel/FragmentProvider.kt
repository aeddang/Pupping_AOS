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
import com.raftgroup.pupping.scene.page.popup.*
import com.raftgroup.pupping.scene.page.report.PageReport
import com.raftgroup.pupping.scene.page.walk.PageWalk
import com.raftgroup.pupping.scene.page.walk.model.PlayWalkType


class FragmentProvider : PageProvider{
    fun getPageObject(pageID: PageID) :PageObject {
        val obj = PageObject(pageID.value, pageID.position)
        when(pageID){
            PageID.Walk, PageID.Mission -> obj.isTop = true
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
            PageID.User.value -> PageUser()
            PageID.Walk.value -> PageWalk(PlayWalkType.Walk)
            PageID.Mission.value -> PageWalk(PlayWalkType.Mission)
            else -> PageIntro()
        }
    }
}

enum class PageID(val value: String, val position: Int = 9999){
    Intro("Intro", 1),
    Login("Login", 2),
    Home("Home", 200),
    Explore("Explore", 300),
    My("My", 400),
    ProfileRegist("ProfileRegist", 999),
    ProfileModify("ProfileModify", 999),
    HealthModify("HealthModify", 999),
    PictureList("PictureList", 999),
    Picture("Picture", 999),
    Profile("Profile", 999),
    History("History", 999),
    Report("Report", 999),
    User("User", 999),
    Walk("Walk", 101),
    Mission("Mission", 999)
}


