package com.lib.thread
import java.util.concurrent.Executors

class CachedThreadPool() : ThreadExecutor {
    private val executorService = Executors.newCachedThreadPool()

    override fun execute(command: Runnable) {
        executorService.submit(command)
    }

    override fun shutdown( isSafe:Boolean ) {
        if( isSafe ) executorService.shutdown() else executorService.shutdownNow()
    }
}