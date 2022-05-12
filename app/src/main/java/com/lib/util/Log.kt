package com.lib.util

import com.raftgroup.pupping.BuildConfig

object PageLog {
    private const val TAG = "Pupping Page : "
    fun i(objects: Any, tag: String = "") { Log.i("$TAG$tag", objects) }
    fun d(objects: Any, tag: String = "") { Log.d("$TAG$tag", objects) }
    fun w(objects: Any, tag: String = "") { Log.w("$TAG$tag", objects) }
    fun e(objects: Any, tag: String = "") { Log.e("$TAG$tag", objects) }
    fun v(objects: Any, tag: String = "") { Log.v("$TAG$tag", objects) }
}
object ComponentLog {
    private const val TAG = "Pupping Component : "
    fun i(objects: Any, tag: String = "") { Log.i("$TAG$tag", objects) }
    fun d(objects: Any, tag: String = "") { Log.d("$TAG$tag", objects) }
    fun w(objects: Any, tag: String = "") { Log.w("$TAG$tag", objects) }
    fun e(objects: Any, tag: String = "") { Log.e("$TAG$tag", objects) }
    fun v(objects: Any, tag: String = "") { Log.v("$TAG$tag", objects) }
}
object DataLog {
    private const val TAG = "Pupping Data : "
    fun i(objects: Any, tag: String = "") { Log.i("$TAG$tag", objects) }
    fun d(objects: Any, tag: String = "") { Log.d("$TAG$tag", objects) }
    fun w(objects: Any, tag: String = "") { Log.w("$TAG$tag", objects) }
    fun e(objects: Any, tag: String = "") { Log.e("$TAG$tag", objects) }
    fun v(objects: Any, tag: String = "") { Log.v("$TAG$tag", objects) }
}

object Log {
    var enable = 0
    fun i(tag: String, vararg objects: Any) {
        if (enable == 1) android.util.Log.i(tag, toString(*objects))
        else {
            when (BuildConfig.BUILD_TYPE) {
                "debug", "release_debug"-> android.util.Log.i( tag, toString(*objects))
            }
        }
    }

    fun d(tag: String, vararg objects: Any) {
        if (enable == 1) android.util.Log.d(tag, toString(*objects))
        else {
            when (BuildConfig.BUILD_TYPE) {
                "debug", "release_debug" -> android.util.Log.d( tag, toString(*objects))
            }
        }
    }

    fun w(tag: String, vararg objects: Any) {
        android.util.Log.w(tag, toString(*objects))
    }

    fun e(tag: String, vararg objects: Any) {
        android.util.Log.e(tag, toString(*objects))
    }

    fun v(tag: String, vararg objects: Any) {
        android.util.Log.v(tag, toString(*objects))
    }

    private fun toString(vararg objects: Any): String {
        val sb = StringBuilder()
        for (o in objects) {
            sb.append(o)
        }
        return sb.toString()
    }
}

