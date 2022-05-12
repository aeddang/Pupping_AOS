package com.skeleton.component.thread

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AppExecutors(val diskIO: ExecutorService = Executors.newSingleThreadExecutor(),
                   val mainThread: Executor = MainThreadExecutor()
) {

    private class MainThreadExecutor: Executor {
        private val mainThreadHandler = Handler(Looper.getMainLooper())

        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }
    }
}