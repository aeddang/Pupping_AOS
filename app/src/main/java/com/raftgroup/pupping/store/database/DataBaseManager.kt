package com.raftgroup.pupping.store.database

import android.content.Context
import com.raftgroup.pupping.store.database.ContentDatabase
import com.raftgroup.pupping.store.preference.StoragePreference


class DataBaseManager (ctx:Context, val settingPreference: StoragePreference){
    val contentDatabase = ContentDatabase(ctx)
}