package com.lib.thread

import android.os.Handler
import android.os.HandlerThread
import com.lib.util.Log


class HandlerExecutor(val name:String): ThreadExecutor {

    private val appTag = "HandlerExecutor"

    private val backgroundThread = HandlerThread(name).also { it.start() }
    val backgroundHandler = Handler(backgroundThread.looper)

    override fun execute(command: Runnable) {
        backgroundHandler.post(command)
    }

    override fun shutdown( isSafe:Boolean ) {
        if( isSafe )  backgroundThread.quitSafely() else backgroundThread.quit()
        try {
            backgroundThread.join()
        } catch (e: InterruptedException) {
            Log.e(appTag, e.toString())
        }
    }
}