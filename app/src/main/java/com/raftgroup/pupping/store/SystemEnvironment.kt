package com.raftgroup.pupping.store

import com.lib.util.AppUtil

class SystemEnvironment {
    companion object {
        var model:String = ""
        var systemVersion:String = ""
        var firstLaunch :Boolean = false
        var isTablet = false
    }
}