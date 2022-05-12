package com.raftgroup.pupping.scene.page

import android.os.Bundle
import android.view.View
import com.raftgroup.pupping.R
import com.lib.page.*
import com.lib.util.Log
import dagger.hilt.android.AndroidEntryPoint

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PageSplash : PageFragment(){
    private val appTag = javaClass.simpleName
    @Inject lateinit var pagePresenter: PagePresenter

    override fun getLayoutResID(): Int = R.layout.page_splash

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //viewModel = ViewModelProvider(this, viewModelFactory).get(BasePageViewModel::class.java)
        //pageViewModel = viewModel
        Log.d(appTag, "onCreate")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
        isStart = true
        scope.launch {
            delay(1500)
            pagePresenter.pageInit()
        }
    }

    private var isStart = false
    override fun onResume() {
        super.onResume()
        if (!isStart) return
        pagePresenter.pageInit()
    }
}