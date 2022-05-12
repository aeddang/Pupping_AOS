package com.skeleton.module.network

enum class ErrorType{
    HOST,
    HTTP,
    CANCEL,
    TIME_OUT,
    EXCEPTION,
    API
}

object HttpStatusCode{
    const val HOST = "504"
    const val CANCEL = "499"
    const val EXCEPTION = "520"
    const val TIME_OUT = "504"
    const val REQUEST = "400"
    const val DATA = "417"
    const val PARSE = "424"
}
