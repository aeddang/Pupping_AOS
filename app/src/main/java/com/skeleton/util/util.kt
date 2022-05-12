package com.skeleton.util

import android.annotation.SuppressLint
import android.text.Editable

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

@SuppressLint("SimpleDateFormat")

fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)

fun String.decimalFormat(): String {
    val decimal = round(this.toDouble()).toInt()
    if (decimal > 999) {
        val df = DecimalFormat("#,###")
        return df.format(decimal)
    }
    return decimal.toString()
}

fun String.toColorCode(): String {
    val code = this.toUpperCase().replace("0X" ,"#")
    if (code.length == 7) return code
    if (code.length == 6) return "#$code"
    if (code.length != 4) return "#000000"
    var hex = code.replace("#" ,"")
    return "#$hex$hex"
}

fun String.isPasswordType():Boolean {
    return this.length >= 6
}


fun String.isNickNameType():Boolean {
    val n = this.length
    if (n < 2) return false
    if (n > 10) return false
    val resultNum = this.replace("[0-9]".toRegex(), "")
    if (resultNum.isEmpty()) return false
    val resultEng = this.replace("[a-zA-Z]".toRegex(), "")
    if (resultEng.isEmpty() && n<3) return false
    val resultStr = this.replace("[0-9가-힣a-zA-Z]".toRegex(), "")
    if (resultStr.isNotEmpty()) return false
    return true
}



fun Double.yyyymmdd():String{
    val sdf = SimpleDateFormat("MM dd yyyy")
    val netDate = Date(this.toLong())
    return sdf.format(netDate)
}


