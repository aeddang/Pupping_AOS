package com.lib.util

import android.content.Context
import android.graphics.*
import androidx.annotation.ColorRes
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

fun Bitmap.cropCircle(stroke:Float? = null, strokeColor: Int = Color.BLACK): Bitmap {
    val output = Bitmap.createBitmap(
        this.width,
        this.height, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(output)
    val color = -0xbdbdbe
    val paint = Paint()
    val rect = Rect(0, 0, width, height)
    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = color
    val rd = width / 2.0f
    canvas.drawCircle(
        rd, height / 2.0f,
        rd, paint
    )
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(this, rect, rect, paint)
    stroke?.let { st->
        val paintStroke = Paint()
        paintStroke.style = Paint.Style.STROKE
        paintStroke.strokeCap = Paint.Cap.ROUND
        paintStroke.strokeWidth =  st
        paintStroke.color = strokeColor
        canvas.drawCircle(rd, rd, rd-(st/2), paintStroke)
    }
    return output
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