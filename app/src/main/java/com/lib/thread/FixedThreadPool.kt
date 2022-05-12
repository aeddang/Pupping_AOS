package com.lib.thread
import java.util.concurrent.Executors

class FixedThreadPool(max:Int) : ThreadExecutor {
    private val executorService = Executors.newFixedThreadPool(max)
    override fun execute(command: Runnable) {
        executorService.submit(command)
    }

    override fun shutdown( isSafe:Boolean ) {
        if( isSafe ) executorService.shutdown() else executorService.shutdownNow()
    }
}