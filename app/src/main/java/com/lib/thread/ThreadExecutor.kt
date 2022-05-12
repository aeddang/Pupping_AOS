package com.lib.thread
import java.util.concurrent.Executor

interface ThreadExecutor : Executor{
    fun shutdown( isSafe:Boolean = false )
}