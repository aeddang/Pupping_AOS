package com.lib.thread

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AppExecutors(val diskIO: ExecutorService = Executors.newSingleThreadExecutor(),
//                   val deviceIO: AppDeviceExecutor = AppDeviceExecutor(),
                   val mainThread: Executor = MainThreadExecutor()
) {

    private class MainThreadExecutor: Executor {
        private val mainThreadHandler = Handler(Looper.getMainLooper())

        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }
    }
}