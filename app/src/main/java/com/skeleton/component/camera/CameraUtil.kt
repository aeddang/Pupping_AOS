package com.skeleton.component.camera

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.Image
import android.util.Size
import com.lib.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.math.abs

object CameraUtil {

    private val appTag = javaClass.simpleName

    internal fun chooseOptimalSize(
        choices: Array<Size>,
        textureViewWidth: Int,
        textureViewHeight: Int,
        maxWidth: Int,
        maxHeight: Int,
        aspectRatio: Size
    ): Size {
        val bigEnough = ArrayList<Size>()
        val notBigEnough = ArrayList<Size>()
        val w = aspectRatio.width
        val h = aspectRatio.height
        for (option in choices) {
            if (option.width <= maxWidth && option.height <= maxHeight &&
                option.height == option.width * h / w
            ) {
                if (option.width >= textureViewWidth && option.height >= textureViewHeight) {
                    bigEnough.add(option)
                } else {
                    notBigEnough.add(option)
                }
            }
        }
        return when {
            bigEnough.size > 0 -> Collections.min(bigEnough, CompareSizesByArea())
            notBigEnough.size > 0 -> Collections.max(notBigEnough, CompareSizesByArea())
            else -> choices[0]
        }
    }

    internal class CompareSizesByArea : Comparator<Size> {
        override fun compare(lhs: Size, rhs: Size): Int {
            return java.lang.Long.signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)
        }

    }

    internal class CompareRatioByArea(private val ratio: Float) : Comparator<Size> {
        override fun compare(lhs: Size, rhs: Size): Int {
            val rL = abs(lhs.width.toFloat() / lhs.height.toFloat() - ratio)
            val rR = abs(rhs.width.toFloat() / rhs.height.toFloat() - ratio)
            if (rL > rR) return 1
            else if (rL < rR) return -1
            return 0
        }

    }

    internal class CompareByArea(private val findSize: Size) : Comparator<Size> {
        override fun compare(lhs: Size, rhs: Size): Int {
            val gepL = Size(abs(lhs.width - findSize.width), abs(lhs.height - findSize.height))
            val gepR = Size(abs(rhs.width - findSize.width), abs(rhs.height - findSize.height))
            return return java.lang.Long.signum(gepL.width.toLong() * gepL.height - gepR.width.toLong() * gepR.height)
        }

    }


    internal class ImageSaver internal constructor(
        private val mImage: Image,
        private val mFile: File
    ) : Runnable {
        override fun run() {
            val buffer = mImage.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            var output: FileOutputStream? = null
            try {
                output = FileOutputStream(mFile)
                output.write(bytes)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                mImage.close()
                if (null != output) {
                    try {
                        output.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }

    }

    internal fun isAutoFocusSupported(manager: CameraManager, cameraId: String?): Boolean {
        return isHardwareLevelSupported(
            manager,
            cameraId,
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3
        ) || getMinimumFocusDistance(manager, cameraId) > 0
    }

    internal fun isHardwareLevelSupported(
        manager: CameraManager,
        cameraId: String?,
        requiredLevel: Int
    ): Boolean {
        var res = false
        if (cameraId == null) return res
        try {

            val cameraCharacteristics =
                manager.getCameraCharacteristics(cameraId)
            val deviceLevel =
                cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)

            deviceLevel?.let { level ->
                when (level) {
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> Log.d(
                        appTag,
                        "Camera support level: INFO_SUPPORTED_HARDWARE_LEVEL_3"
                    )
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> Log.d(
                        appTag,
                        "Camera support level: INFO_SUPPORTED_HARDWARE_LEVEL_FULL"
                    )
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY -> Log.d(
                        appTag,
                        "Camera support level: INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY"
                    )
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> Log.d(
                        appTag,
                        "Camera support level: INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED"
                    )
                    else -> Log.d(appTag, "Unknown INFO_SUPPORTED_HARDWARE_LEVEL: $level")
                }
                res =
                    if (level == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                        requiredLevel == level
                    } else { // deviceLevel is not LEGACY, can use numerical sort
                        requiredLevel <= level
                    }
            }
        } catch (e: java.lang.Exception) {
            Log.e(appTag, "isHardwareLevelSupported Error", e)
        }
        return res
    }

    internal fun getMinimumFocusDistance(manager: CameraManager, cameraId: String?): Float {
        if (cameraId == null) return 0.0f
        var minimumLens: Float? = null
        try {
            val c = manager.getCameraCharacteristics(cameraId)
            minimumLens =
                c.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE)
        } catch (e: java.lang.Exception) {
            Log.e(appTag, "isHardwareLevelSupported Error", e)
        }
        return minimumLens ?: 0.0f
    }
}