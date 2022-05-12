package com.lib.model

import android.net.Uri
import com.lib.page.PageObject
import com.lib.util.Log
import org.json.JSONException
import org.json.JSONObject

data class IwillGo(
    var pageID: String="",
    var pageIDX: Int = 9999,
    var param: HashMap<String, Any?>? = null,
    var isPopup: Boolean = true
) {
    private var TAG = javaClass.simpleName
    val page: PageObject?
    get() {
        if( pageID == "" ) return null
        val pageObject = PageObject(pageID, pageIDX)
        pageObject.params = param
        pageObject.isPopup = isPopup
        return pageObject
    }

    companion object {
        private const val PAGE_KEY = "pageID"
        private const val PAGE_IDX_KEY = "pageIDX"
        private const val PARAMS_KEY = "params"
        private const val IS_POPUP_KEY = "isPopup"
    }

    fun stringfy(): String {
        val json = JSONObject()
        try {
            json.put(PAGE_KEY, pageID)
            json.put(PAGE_IDX_KEY, pageIDX)
            param?.let { json.put(PARAMS_KEY , it) }
            json.put(IS_POPUP_KEY, isPopup)
        } catch (e: JSONException) {
            Log.d(TAG, "json stringfy error $e")
        }
        return json.toString()
    }

    fun parse(jsonString: String): IwillGo {
        val json = JSONObject(jsonString)
        json.keys().forEach { key ->
            when (key) {
                PAGE_KEY -> pageID = json.getString(key)
                PAGE_IDX_KEY -> pageIDX = json.getInt(key)
                IS_POPUP_KEY -> isPopup = json.getBoolean(key)
                PARAMS_KEY ->{
                    try {
                        val params = HashMap<String, Any?>()
                        json.getJSONObject(key).let { jsonParams->
                            jsonParams.keys().forEach {
                                params[it] = jsonParams.getString(it)
                            }
                        }
                        param = params
                    } catch (e: JSONException) {
                        Log.d(TAG, "json parse error $e")
                    }
                }
                else -> {}
            }
        }

        return this
    }

    fun parse(data:Map<String, String>): IwillGo {
        data[PAGE_KEY]?.let{ pageID = it }
        data[PAGE_IDX_KEY]?.let{ pageIDX = it.toInt() }
        data[IS_POPUP_KEY]?.let{ isPopup = it.toBoolean() }
        data[PARAMS_KEY]?.let{jsonString->
            try {
                val params = HashMap<String, Any?>()
                JSONObject(jsonString).let { jsonParams->
                    jsonParams.keys().forEach {
                        params[it] = jsonParams.getString(it)
                    }
                }
                param = params
            } catch (e: JSONException) {
                Log.d(TAG, "json parse error $e")
            }
        }
        return this
    }

    fun qurry(): String {
        pageID ?: return ""
        val qurry = "$PAGE_KEY=$pageID&$PAGE_IDX_KEY=$pageIDX&$IS_POPUP_KEY=$isPopup"
        param ?: return qurry
        return param!!.keys.fold(qurry) { sum, key -> "$sum&$key=${param!![key]}" }
    }

    fun parseQurry(qurry: String): IwillGo {
        val fullPath = "https://kkk.com?$qurry"
        val uri = Uri.parse(fullPath)
        val params = HashMap<String, Any?>()
        uri.queryParameterNames.forEach { key ->
            val value = uri.getQueryParameter(key)
            value?.let { v ->
                when (key) {
                    PAGE_KEY -> pageID = v
                    PAGE_IDX_KEY -> pageIDX = v.toInt()
                    IS_POPUP_KEY -> isPopup = v.toBoolean()
                    else -> params[key] = v
                }
            }
        }
        param = params
        return this
    }
}

object WhereverYouCanGo {
    fun parseQurryIwillGo(qurry: String): IwillGo {
        return IwillGo().parseQurry(qurry)
    }
    fun parseJsonIwillGo(jsonString: String): IwillGo {
        return IwillGo().parse(jsonString)
    }
    fun parseJsonIwillGo(data:Map<String, String>): IwillGo {
        return IwillGo().parse(data)
    }

    fun stringfyQurryIwillGo(
        pageID: String,
        pageIDX: Int = 999,
        param: HashMap<String, Any?>? = null,
        isPopup: Boolean = false
    ): String {
        return IwillGo(pageID, pageIDX, param, isPopup).qurry()
    }

    fun stringfyIwillGo(
        pageID: String,
        pageIDX: Int = 999,
        param: HashMap<String, Any?>? = null,
        isPopup: Boolean = false
    ): String {
        return IwillGo(pageID, pageIDX, param, isPopup).stringfy()
    }

}