package com.lib.util

import android.content.Context
import android.graphics.*
import java.io.*
import kotlin.math.roundToInt


fun Bitmap.rotate(degree: Int): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(degree.toFloat())
    val scaledBitmap = Bitmap.createScaledBitmap(this, width, height, true)
    return Bitmap.createBitmap(
        scaledBitmap,
        0,
        0,
        scaledBitmap.width,
        scaledBitmap.height,
        matrix,
        true
    )
}



fun Bitmap.centerCrop(rect: RectF): Bitmap {
    return Bitmap.createBitmap(
        this,
        rect.left.roundToInt(),
        rect.top.roundToInt(),
        rect.width().roundToInt(),
        rect.height().roundToInt(),
        null,
        false
    )
}

fun Bitmap.crop(rect: Rect): Bitmap {
    return Bitmap.createBitmap(
        this,
        rect.left,
        rect.top,
        rect.width(),
        rect.height(),
        null,
        false
    )
}

fun Bitmap.size(w: Int, h: Int, degree: Int? = null): Bitmap {
    val matrix = Matrix()
    degree?.let { matrix.postRotate(it.toFloat()) }
    val scaledBitmap = Bitmap.createScaledBitmap(this, w, h, true)
    return Bitmap.createBitmap(
        scaledBitmap,
        0,
        0,
        scaledBitmap.width,
        scaledBitmap.height,
        matrix,
        true
    )
}

fun Bitmap.mergeHorizental(w: Int, h: Int, merdeImg: Bitmap): Bitmap {
    val bmMerge = Bitmap.createBitmap(w, h, config)
    val canvas = Canvas(bmMerge)
    canvas.drawBitmap(this,0f,0f, null)
    canvas.drawBitmap( merdeImg, this.width.toFloat() , 0f, null)
    return bmMerge
}

fun Bitmap.merge(w: Int, h: Int, merdeImg: Bitmap): Bitmap {
    val bmMerge = Bitmap.createBitmap(w, h, config)
    val canvas = Canvas(bmMerge)
    canvas.drawBitmap(this, Matrix(), null)
    canvas.drawBitmap( merdeImg, Matrix(), null)
    return bmMerge
}

fun Bitmap.swapHolizental(): Bitmap {
    val matrix = Matrix()
    matrix.postScale(-1F, 1F)
    val scaledBitmap = Bitmap.createScaledBitmap(this, width, height, true)
    return Bitmap.createBitmap(
        scaledBitmap,
        0,
        0,
        scaledBitmap.width,
        scaledBitmap.height,
        matrix,
        true
    )
}

fun Bitmap.swapVertical(): Bitmap {
    val matrix = Matrix()
    matrix.postScale(1F, -1F)
    val scaledBitmap = Bitmap.createScaledBitmap(this, width, height, true)
    return Bitmap.createBitmap(
        scaledBitmap,
        0,
        0,
        scaledBitmap.width,
        scaledBitmap.height,
        matrix,
        true
    )
}



//bitmap -> File
fun Bitmap.toFile(context: Context,  name:String = "profile.jpg"): File {
    val file = File(context.cacheDir, name)
    try {
        val stream: OutputStream = FileOutputStream(file)
        this.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.flush()
        stream.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return file
}