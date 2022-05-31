package com.lib.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.RectF
import android.net.Uri
import android.provider.MediaStore
import android.util.Patterns
import android.util.Size
import java.sql.Timestamp
import java.text.DecimalFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern
import kotlin.math.floor
import kotlin.math.round


fun String.isEmailType():Boolean {
    val pattern: Pattern = Patterns.EMAIL_ADDRESS
    return pattern.matcher(this).matches()
}

fun String.toDate(
    dateFormat: String = "yyyy-MM-dd'T'HH:mm:ssZ"
): LocalDate? {
    val pattern = DateTimeFormatter.ofPattern(dateFormat)
    return LocalDate.parse(this, pattern)
}

fun LocalDate.toFormatString(
    dateFormat: String = "yyyy-MM-dd'T'HH:mm:ss"
): String? {
    val date  = LocalDateTime.of(this, LocalTime.MIN)
    return date.toFormatString(dateFormat)
}
fun LocalDateTime.toFormatString(
    dateFormat: String = "yyyy-MM-dd'T'HH:mm:ss"
): String? {
    val pattern = DateTimeFormatter.ofPattern(dateFormat)
    return this.format(pattern)
}

fun String.toDecimalFormat(): String {
    val decimal = round(this.toDouble()).toInt()
    if (decimal > 999) {
        val df = DecimalFormat("#,###")
        return df.format(decimal)
    }
    return decimal.toString()
}

fun String.toFixLength(l:Int, prefix:String = "000000"): String {
    if (length >= l) { return this }
    val fix:String = prefix + this
    return fix.takeLast(l)
}

fun Double.secToMinString(div:String = ":") : String {
    val sec = this.toInt() % 60
    val min = floor( this / 60.0 ).toInt()
    return min.toString().toFixLength(2) + div + sec.toString().toFixLength(2)
}

fun Double.toDecimal(divid:Double = 1.0 ,f:Int = 0) : String {
    val decimal = if( (this % divid) == 0.0 )  "%.0f" else "%.${f.toString()}f"
    val n = this / divid
    return String.format(decimal, n)
}

fun Double.toThousandUnit(f:Int = 0) : String {
    when {
        this < 1000 -> { return round(this).toInt().toString() }
        this < 100000 -> return "${this.toDecimal(1000.0, f)}K"
        this < 100000000 -> return "${this.toDecimal(100000.0, f)}M"
        else -> return "${this.toDecimal(100000000.0, f)}B"
    }
}

fun Double.millisecToSec() : Double {
    return this/1000.0
}


fun Size.getCropRatioSize(crop: Size):RectF{
    val cropRatio = crop.width.toFloat()/crop.height.toFloat()
    val originWidth = width.toFloat()
    val originHeight = height.toFloat()

    var ratioWidth = originWidth
    var ratioHeight = originWidth / cropRatio
    if( ratioHeight > originHeight ){
        ratioHeight = originHeight
        ratioWidth = originHeight * cropRatio
    }
    val marginX = (originWidth - ratioWidth)/2
    val marginY = (originHeight - ratioHeight)/2
    return RectF(marginX, marginY, marginX + ratioWidth, marginY + ratioHeight)
}



val Int.toPx: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.toDp: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()


@SuppressLint("Range")
fun Uri.getAbsuratePathFromUri(context: Context): String {
    var path = ""
    val array = arrayOf(MediaStore.Images.Media.DATA)
    val cursor = context.contentResolver.query(this, array, null, null, null)
    cursor?.let {
        it.moveToFirst()
        path = it.getString(it.getColumnIndex(array[0]))
        cursor.close()
    }
    return path
}
