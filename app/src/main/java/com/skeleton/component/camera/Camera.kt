package com.skeleton.component.camera
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.CallSuper
import androidx.fragment.app.FragmentActivity
import com.lib.page.PageComponent
import com.lib.page.PageRequestPermission
import com.lib.page.PageView
import com.lib.page.PageViewModel
import com.lib.util.Log
import com.skeleton.component.thread.HandlerExecutor
import java.io.File
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit


abstract class Camera : PageComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)

    companion object {
        private var viewModelInstance: CameraViewModel? = null
        fun getViewmodel(): CameraViewModel {
            if (viewModelInstance == null) viewModelInstance = CameraViewModel()
            return viewModelInstance!!
        }

        val ORIENTATIONS: SparseIntArray = object : SparseIntArray() {
            init {
                append(Surface.ROTATION_0, 90)
                append(Surface.ROTATION_90, 0)
                append(Surface.ROTATION_180, 270)
                append(Surface.ROTATION_270, 180)
            }
        }
    }

    private var delegate: Delegate? = null
    interface Delegate {
        fun onCaptureStart(camera: Camera) {}
        fun onCaptureCompleted(camera: Camera, file: File?) {}
        fun onExtractStart(camera: Camera, size: Size) {}
        fun onExtractEnd(camera: Camera) {}
        fun onError(camera: Camera, type: Error, data: Any?) {}
    }

    fun setOnCameraListener(_delegate: Delegate?) {
        delegate = _delegate
    }


    enum class State {
        Preview,
        WaitingLock,
        WaitingPrecapture,
        WaitingNonePrecapture,
        PictureTaken
    }

    enum class Error {
        CameraDevice,
        CameraState,
        CameraAccess,
        CaptureSession
    }

    enum class PermissionGranted {
        UnChecked,
        Granted,
        Denied
    }

    enum class CaptureMode {
        Image,
        Extraction,
        ExtractionBitmap,
        ExtractionData
    }

    enum class CameraRatioType {
        Largest,
        Smallest,
        LargestViewRatio,
        SmallestViewRatio,
        Custom
    }

    enum class FlashType {
        On, Off, Auto
    }

    private val appTag = javaClass.simpleName

    private var sharedModel: CameraViewModel = getViewmodel()
    protected var textureView: AutoFitTextureView? = null
    private var blankSurfaceTexture = SurfaceTexture(10)
    private var currentCameraDevice: CameraDevice? = null
    private var backgroundExecutor: HandlerExecutor? = null
    private var imageReader: ImageReader? = null
    private var extractionReader: ImageReader? = null
    private val cameraOpenCloseLock = Semaphore(1)
    private var previewRequestBuilder: CaptureRequest.Builder? = null
    private var previewRequest: CaptureRequest? = null
    private var captureSession: CameraCaptureSession? = null
    private var captureCompletedRunnable: Runnable = Runnable { onCapture() }
    private var initCameraRunnable: Runnable = Runnable { initCamera() }
    private var releaseCameraRunnable: Runnable = Runnable { releaseCamera() }
    private var isHardwareLevelSupported: Boolean = false
    protected var cameraManager: CameraManager? = null

    var isInit: Boolean = false; private set
    var isExtraction = false
    var maxZoom: Float = 1.0f
    var minZoom: Float = 1.0f
    var zoomRect: Rect = Rect()
    var previewSize: Size? = null; private set
    var cameraOutputSize: Size? = null; private set
    var cameraId: String? = null; private set
    var flashCameraId: String? = null; private set
    var state: State = State.Preview; private set
    var sensorOrientation: Int = 0; private set
    var deviceOrientation: Int = 0; private set
    var displayRotation: Int = 0; private set
    var isFront: Boolean = sharedModel.isFront; protected set
    var flashType: FlashType = sharedModel.flashType; protected set

    var cameraRatioType: CameraRatioType = CameraRatioType.LargestViewRatio
    protected open var maxPreviewWidth = 1920
    protected open var maxPreviewHeight = 1920
    var customSize: Size = Size(maxPreviewWidth, maxPreviewHeight)

    protected open var file: File? = null
    protected open var captureMode: CaptureMode = CaptureMode.Image
    protected open var extractionFps: Int = 15

    protected open fun getPreviewArea(): ViewGroup? {return null}
    protected open fun getCameraTextureView(): AutoFitTextureView {
        val parent =  getPreviewArea() ?: this
        textureView?.let { parent.removeView(it) }
        val texture = AutoFitTextureView(context)
        parent.addView(texture, 0, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        return texture
    }

    protected fun getActivity(): FragmentActivity?{
        return viewModel?.presenter?.activity
    }
    abstract fun getNeededPermissions(): Array<String>
    protected open fun requestCameraPermission() {
        viewModel ?: return

        viewModel!!.presenter.requestPermission(getNeededPermissions(), object :PageRequestPermission{
            override fun onRequestPermissionResult(resultAll: Boolean, permissions: List<Boolean>?) {
                sharedModel.permissionGranted =
                        if (resultAll) PermissionGranted.Granted else PermissionGranted.Denied
            }
        })
    }

    protected open fun hasCameraPermission(): Boolean {
        viewModel ?: return false
        return viewModel!!.presenter.hasPermissions(getNeededPermissions())?.first ?: true
    }


    private var viewModel: PageViewModel? = null
    override fun onPageViewModel(vm: PageViewModel): PageView {
        super.onPageViewModel(vm)
        viewModel = vm
        return this
    }

    @CallSuper
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    @CallSuper
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        releaseCamera()
        delegate = null
        file?.delete()
        file = null
    }



    @CallSuper
    open fun startCamera() {
        getActivity()?.let {
            sharedModel.resetPermissionGranted()
            deviceOrientation = it.windowManager.defaultDisplay.rotation
            Handler().post(initCameraRunnable)
        }
    }

    fun takePicture() {
        if (captureMode == CaptureMode.Extraction) return
        if (captureMode == CaptureMode.ExtractionData) return
        if (captureMode == CaptureMode.ExtractionBitmap) return
        delegate?.onCaptureStart(this)
        lockFocus()
    }


    fun setFrontCamera(isGlobalSet: Boolean = true) {
        if (isGlobalSet) sharedModel.isFront = true
        if (isFront) return
        isFront = true
        if (isInit) resetCamera()
    }

    fun setBackCamera(isGlobalSet: Boolean = true) {
        if (isGlobalSet) sharedModel.isFront = false
        if (!isFront) return
        isFront = false
        if (isInit) resetCamera()
    }

    fun onFlash(isFlash: Boolean) {
        if (!sharedModel.flashSupported) return
        flashType = if (isFlash) FlashType.On else FlashType.Off
        setRepeatingRequest()
    }

    fun setUseFlash(flash: FlashType, isGlobalSet: Boolean = true) {
        if (!sharedModel.flashSupported) return
        flashType = flash
        if (isGlobalSet) sharedModel.flashType = flash
        setRepeatingRequest()
    }

    private fun flashOn() {
        try {
            cameraManager?.let { manager ->
                flashCameraId?.let { id ->
                    manager.setTorchMode(id, true)
                }
            }

        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun flashOff() {
        try {
            cameraManager?.let { manager ->
                flashCameraId?.let { id ->
                    manager.setTorchMode(id, false)
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    fun toggleCamera() {
        isFront = !isFront
        sharedModel.isFront = isFront
        resetCamera()
    }

    fun toggleFlash() {
        when(flashType){
            FlashType.Auto -> setUseFlash(FlashType.On)
            FlashType.On -> setUseFlash(FlashType.Off)
            FlashType.Off -> setUseFlash(FlashType.Auto)
        }
    }

    fun resetCamera() {
        releaseCamera()
        val handler = Handler()
        handler.post(initCameraRunnable)
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation != deviceOrientation) {
            deviceOrientation = newConfig.orientation
            textureView?.let {
                it.viewTreeObserver.addOnGlobalLayoutListener(object :
                    ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        it.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        textureViewAspectRatio()
                    }
                })
            }

        }
    }


    override fun onPagePause() {
        super.onPagePause()
        releaseCamera()
    }


    override fun onPageResume() {
        super.onPageResume()
        sharedModel.permissionGranted =
            if (hasCameraPermission()) PermissionGranted.Granted else PermissionGranted.Denied
        if (sharedModel.permissionGranted != PermissionGranted.Denied) initCamera()
    }

    open fun onTextureViewUpdated(texture: SurfaceTexture) {}
    open fun onPreview() {}
    open fun onExtract(image: Bitmap) {}
    open fun onExtract(image: Image) {}
    open fun onCapture(image: Image) {}
    open fun onCapture() {
        delegate?.onCaptureCompleted(this, file)
    }

    @CallSuper
    open fun onError(type: Error, data: Any? = null) {
        delegate?.onError(this, type, data)
    }

    private fun initCamera() {
        if (isInit) return
        isInit = true
        textureView = getCameraTextureView()
        startBackgroundThread()
        textureView?.let { texture ->
            if (texture.isAvailable) {
                if (surfaceTextureSize == null) {
                    openCamera(texture.width, texture.height)
                } else {
                    openCamera(surfaceTextureSize!!.width, surfaceTextureSize!!.height)
                }
            } else {
                texture.surfaceTextureListener = surfaceTextureListener
            }
        }
    }

    private fun releaseCamera() {
        if (!isInit) return
        isInit = false
        flashCameraId = null
        closeCamera()
        stopBackgroundThread()
        surfaceTextureSize = null
        cameraOutputSize = null
    }

    private fun createImageReader(videoWidth: Int, videoHeight: Int) {
        if (captureMode == CaptureMode.Image) {
            imageReader = ImageReader.newInstance(videoWidth, videoHeight, ImageFormat.JPEG, 2)
            imageReader?.setOnImageAvailableListener(
                onImageAvailableListener,
                backgroundExecutor?.backgroundHandler
            )
        }
        if (captureMode == CaptureMode.ExtractionData) {
            extractionReader = ImageReader.newInstance(
                videoWidth,
                videoHeight,
                ImageFormat.YUV_420_888,
                extractionFps
            )
            extractionReader?.setOnImageAvailableListener(
                onExtractionAvailableListener,
                backgroundExecutor?.backgroundHandler
            )
        }
        if (captureMode == CaptureMode.ExtractionData || captureMode == CaptureMode.ExtractionBitmap) {
            delegate?.onExtractStart(this, Size(videoWidth, videoHeight))
        }
    }

    private fun findCamera(): CameraCharacteristics? {
        getActivity()?.let { activity ->
            val manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            var characteristics: CameraCharacteristics? = null
            cameraManager = manager

            for (cId in manager.cameraIdList) {
                val cr = manager.getCameraCharacteristics(cId)
                val facing = cr.get(CameraCharacteristics.LENS_FACING)
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT && !isFront) continue
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK && isFront) continue
                cr.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) ?: continue
                if (characteristics == null) {
                    cameraId = cId
                    Log.d(appTag, "cameraId $cameraId")
                    characteristics = cr
                }
                if (cr.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)!! && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    flashCameraId = cId
                    Log.d(appTag, "flashCameraId $flashCameraId")
                }
            }
            return characteristics
        }
        return null
    }


    private fun setUpCameraOutputs(width: Int, height: Int) {

        try {
            val activity = getActivity()
            val characteristics = findCamera()
            characteristics?.let { character ->
                maxZoom =
                    character.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM) ?: 1.0f
                val sensorSize = characteristics.get(
                    CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE
                )!!
                val cropW = (sensorSize.width() * (1 - 1 / minZoom) / 2).toInt()
                val cropH = (sensorSize.width() * (1 - 1 / minZoom) / 2).toInt()
                zoomRect =
                    Rect(cropW, cropH, sensorSize.width() - cropW, sensorSize.height() - cropH)

                activity?.let { displayRotation = it.windowManager.defaultDisplay.rotation }
                sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!

                Log.d(appTag, "sensorOrientation $sensorOrientation")
                var swappedDimensions = false
                when (displayRotation) {
                    Surface.ROTATION_0, Surface.ROTATION_180 -> if (sensorOrientation == 90 || sensorOrientation == 270) swappedDimensions =
                        true
                    Surface.ROTATION_90, Surface.ROTATION_270 -> if (sensorOrientation == 0 || sensorOrientation == 180) swappedDimensions =
                        true
                    else -> Log.e(appTag, "Display rotation is invalid: $displayRotation")
                }
                val displaySize = Point()
                activity?.windowManager?.defaultDisplay?.getSize(displaySize)

                var rotatedPreviewWidth = width
                var rotatedPreviewHeight = height
                var maxWidth = displaySize.x
                var maxHeight = displaySize.y
                if (swappedDimensions) {
                    rotatedPreviewWidth = height
                    rotatedPreviewHeight = width
                    maxWidth = displaySize.y
                    maxHeight = displaySize.x
                }

                //val maxFocus = character.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
                val map = character.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
                val outputs = map.getOutputSizes(ImageFormat.YUV_420_888)
                cameraOutputSize = when (cameraRatioType) {
                    CameraRatioType.Largest -> Collections.max(
                        listOf(*outputs),
                        CameraUtil.CompareSizesByArea()
                    )
                    CameraRatioType.Smallest -> Collections.min(
                        listOf(*outputs),
                        CameraUtil.CompareSizesByArea()
                    )
                    CameraRatioType.LargestViewRatio -> {
                        val ratio = rotatedPreviewWidth.toFloat() / rotatedPreviewHeight.toFloat()
                        Collections.min(listOf(*outputs), CameraUtil.CompareRatioByArea(ratio))
                    }
                    CameraRatioType.SmallestViewRatio -> {
                        val ratio = rotatedPreviewWidth.toFloat() / rotatedPreviewHeight.toFloat()
                        Collections.min(
                            listOf(*outputs.reversedArray()),
                            CameraUtil.CompareRatioByArea(ratio)
                        )
                    }
                    CameraRatioType.Custom -> {
                        Collections.min(
                            listOf(*outputs.reversedArray()),
                            CameraUtil.CompareByArea(customSize)
                        )
                    }
                }
                Log.d(appTag, "cameraOutputSize $cameraOutputSize")

                if (maxWidth > maxPreviewWidth) maxWidth = maxPreviewWidth
                if (maxHeight > maxPreviewHeight) maxHeight = maxPreviewHeight
                previewSize = CameraUtil.chooseOptimalSize(
                    map.getOutputSizes(SurfaceTexture::class.java),
                    rotatedPreviewWidth, rotatedPreviewHeight,
                    maxWidth, maxHeight,
                    cameraOutputSize!!
                )
                Log.d(appTag, "previewSize $previewSize")
                this.textureViewAspectRatio()
                val available = character.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
                sharedModel.flashSupported = available == true
                Log.d(appTag, "flashSupported ${sharedModel.flashSupported}")
                onPreviewSize(previewSize, cameraOutputSize, surfaceTextureSize)

            }

        } catch (e: CameraAccessException) {
            e.printStackTrace()
            onError(Error.CameraAccess)
        } catch (e: NullPointerException) {
            onError(Error.CameraDevice)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

    }


    protected open fun onPreviewSize(preview: Size?, camera: Size?, texture: Size?) {
        textureViewAspectRatio()
    }
    private fun textureViewAspectRatio() {
        previewSize?.let {
            val orientation = resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) onTextureViewAspectRatio(it.width, it.height)
            else onTextureViewAspectRatio(it.height, it.width)
        }

    }
    protected open fun onTextureViewAspectRatio(width: Int, height: Int) {
        textureView?.setAspectRatio(width, height)
    }

    @SuppressLint("MissingPermission")
    private fun openCamera(width: Int, height: Int) {
        if (!hasCameraPermission()) {
            requestCameraPermission()
            return
        }
        Log.i(appTag, "openCamera size ${width} ${height}")
        setUpCameraOutputs(width, height)
        val activity = getActivity()
        val manager = activity?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }
            cameraId?.let { cid ->
                if (captureMode == CaptureMode.Extraction) {
                    isHardwareLevelSupported = CameraUtil.isHardwareLevelSupported(
                        manager,
                        cid,
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL
                    )
                    captureMode =
                        if (isHardwareLevelSupported) CaptureMode.ExtractionData else CaptureMode.ExtractionBitmap

                    textureView?.let {
                        it.bitmap?.let { bitmap ->
                            val size = bitmap.width * bitmap.height
                            Log.d(appTag, "captureMode size ${bitmap.width} ${bitmap.height}")
                            if (captureMode == CaptureMode.ExtractionBitmap && size > (300 * 300)) {
                                captureMode = CaptureMode.ExtractionData
                            }
                        }
                    }
                }
                Log.d(appTag, "captureMode $captureMode")
                cameraOutputSize?.let { createImageReader(it.width, it.height) }
                manager.openCamera(
                    cid,
                    stateCallback,
                    backgroundExecutor?.backgroundHandler
                )

            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera opening.", e)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    fun getOrientation(): Int {
        getActivity()?.let {
            val rotation = it.windowManager.defaultDisplay.rotation
            var rotate = ORIENTATIONS.get(rotation) + ((sensorOrientation - 90) % 180)
            if (isFront && rotation == Surface.ROTATION_0) rotate = -rotate

            return rotate
        }
        return 0
    }

    fun isSwap(): Boolean {
        if (!isFront) return false
        if (((sensorOrientation - 90) % 180) == 0) return true
        return false
    }

    private fun lockFocus() {
        Log.i(appTag, "lockFocus")
        try {
            previewRequestBuilder?.let { requestBuilder ->
                requestBuilder.set(
                    CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_START
                )
                state = State.WaitingLock
                captureSession?.capture(
                    requestBuilder.build(),
                    captureCallback,
                    backgroundExecutor?.backgroundHandler
                )
            }

        } catch (e: CameraAccessException) {
            e.printStackTrace()
            onError(Error.CameraAccess)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    private fun unlockFocus() {
        Log.i(appTag, "unlockFocus")
        try {
            previewRequestBuilder?.let { requestBuilder ->
                requestBuilder.set(
                    CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL
                )
                captureSession?.capture(
                    requestBuilder.build(),
                    captureCallback,
                    backgroundExecutor?.backgroundHandler
                )
                state = State.Preview
                setRepeatingRequest()
            }

        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    private fun setRequestFlashType(type: FlashType, requestBuilder: CaptureRequest.Builder) {
        if (sharedModel.flashSupported) {
            when (type) {
                FlashType.On -> {
                    requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
                    requestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH)
                    flashOn()

                }
                FlashType.Off -> {
                    requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
                    requestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF)
                    flashOff()
                }
                FlashType.Auto -> {
                    requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
                    requestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_SINGLE)
                    flashOn()
                }
            }
            Log.d(appTag, "setupCameraFlash $type")
        }
    }

    private fun setRepeatingRequest() {
        try {
            captureSession?.stopRepeating()
            previewRequestBuilder?.let { requestBuilder ->
                setRequestFlashType(flashType, requestBuilder)
                previewRequest = requestBuilder.build()
                previewRequest?.let {
                    captureSession?.setRepeatingRequest(
                        it,
                        captureCallback,
                        backgroundExecutor?.backgroundHandler
                    )
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

    }

    private fun closeCamera() {
        try {
            cameraOpenCloseLock.acquire()
            captureSession?.close()
            captureSession = null
            currentCameraDevice?.close()
            currentCameraDevice = null
            imageReader?.close()
            imageReader = null
            if (captureMode == CaptureMode.ExtractionData || captureMode == CaptureMode.ExtractionBitmap) delegate?.onExtractEnd(
                this
            )
            extractionReader?.close()
            extractionReader = null
            cameraManager = null

        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    private fun startBackgroundThread() {
        backgroundExecutor = HandlerExecutor(appTag)
    }

    private fun stopBackgroundThread() {
        backgroundExecutor?.shutdown()
        backgroundExecutor = null
    }

    private fun createCameraPreviewSession() {
        try {
            currentCameraDevice?.let { cameraDevice ->
                val texture =
                    if (textureView != null) textureView!!.surfaceTexture else blankSurfaceTexture
                previewSize?.let { texture!!.setDefaultBufferSize(it.width, it.height) }
                val surface = Surface(texture)
                previewRequestBuilder =
                    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                previewRequestBuilder?.let { requestBuilder ->
                    val output = ArrayList<Surface>()
                    output.add(surface)
                    requestBuilder.addTarget(surface)
                    imageReader?.let { reader -> output.add(reader.surface) }
                    extractionReader?.let { reader ->
                        requestBuilder.addTarget(reader.surface)
                        output.add(reader.surface)
                    }

                    cameraDevice.createCaptureSession(
                        output,
                        object : CameraCaptureSession.StateCallback() {
                            override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                                captureSession = cameraCaptureSession
                                try {
                                    requestBuilder.set(
                                        CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                                    )
                                    requestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoomRect)
                                    setRepeatingRequest()
                                } catch (e: CameraAccessException) {
                                    e.printStackTrace()
                                } catch (e: IllegalStateException) {
                                    e.printStackTrace()
                                }
                            }

                            override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                                onError(Error.CaptureSession)
                            }
                        }, null
                    )
                }

            }

        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    private fun configureTransform(viewWidth: Int, viewHeight: Int) {

        previewSize?.let {
            Log.d(appTag, "configureTransform $viewWidth $viewHeight")
            val activity = getActivity()
            val rotation = activity?.windowManager?.defaultDisplay?.rotation
            val matrix = Matrix()
            val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
            val bufferRect = RectF(0f, 0f, it.height.toFloat(), it.width.toFloat())
            val centerX = viewRect.centerX()
            val centerY = viewRect.centerY()

            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
                matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
                matrix.postScale( viewWidth.toFloat() /it.height,viewHeight.toFloat() / it.width, centerX, centerY)
                matrix.postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
                Log.d(appTag, "configureTransform ROTATION_90")
            } else if (Surface.ROTATION_180 == rotation) {
                matrix.postRotate(180f, centerX, centerY)
                Log.d(appTag, "configureTransform ROTATION_180")
            } else {
                Log.d(appTag, "configureTransform ROTATION_NONE")
            }
            Log.d(appTag, "configureTransform $matrix")
            textureView?.setTransform(matrix)
        }
    }


    private fun runPrecaptureSequence() {
        Log.i(appTag, "runPrecaptureSequence")
        try {
            previewRequestBuilder?.let {
                it.set(
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START
                )
                state = State.WaitingPrecapture
                captureSession?.capture(
                    it.build(),
                    captureCallback,
                    backgroundExecutor?.backgroundHandler
                )
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            onError(Error.CameraAccess)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    private fun captureStillPicture() {
        Log.i(appTag, "captureStillPicture")
        try {
            currentCameraDevice?.let { cameraDevice ->

                val captureBuilder =
                    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                        .apply {
                            imageReader?.let { reader ->
                                this.addTarget(reader.surface)
                                Log.i(appTag, "imageReader exist")
                                Log.i(appTag, "imageReader exist")
                            }
                            this.set(
                                CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                            )
                        }

                Log.i(appTag, "sessionCaptureCallback start $flashType")
                val sessionCaptureCallback = object : CameraCaptureSession.CaptureCallback() {
                    override fun onCaptureCompleted(
                        session: CameraCaptureSession,
                        request: CaptureRequest,
                        result: TotalCaptureResult
                    ) {
                        Log.i(appTag, "onCaptureCompleted")
                        if (Handler(Looper.getMainLooper()).post(captureCompletedRunnable)) unlockFocus()
                        else Log.i(appTag, "Handler.post fail")
                    }

                    override fun onCaptureFailed(
                        session: CameraCaptureSession,
                        request: CaptureRequest,
                        failure: CaptureFailure
                    ) {
                        super.onCaptureFailed(session, request, failure)
                        Log.i(appTag, "failure ${failure.reason}")
                    }
                }
                captureSession?.let { session ->
                    Log.i(appTag, "captureSession start")
                    session.stopRepeating()
                    session.abortCaptures()
                    session.capture(captureBuilder.build(), sessionCaptureCallback, null)
                }

            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            onError(Error.CameraAccess)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private var surfaceTextureSize: Size? = null
    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
            surfaceTextureSize = Size(width, height)
            Log.i(appTag, "onSurfaceTextureAvailable size ${width} ${height}")
            openCamera(width, height)
        }

        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {
            Log.i(appTag, "onSurfaceTextureSizeChanged size ${width} ${height}")
            configureTransform(width, height)
        }

        override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
            return true
        }

        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) {
            onTextureViewUpdated(texture)
        }
    }


    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {
        private fun process(result: CaptureResult) {
            when (state) {
                State.Preview -> {
                    onPreview()
                    if (captureMode == CaptureMode.ExtractionBitmap && isExtraction) {
                        previewSize ?: return
                        textureView?.let {
                            val bitmap = it.getBitmap( previewSize!!.width, previewSize!!.height)
                            bitmap ?: return@let
                            onExtract(bitmap)
                        }
                    }
                }
                State.WaitingLock -> {
                    val afState = result.get(CaptureResult.CONTROL_AF_STATE)
                    if (afState == null) {
                        Log.i(appTag, "afState null")
                        captureStillPicture()
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                        if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            state = State.PictureTaken
                            Log.i(appTag, "afState $afState")
                            captureStillPicture()
                        } else {
                            runPrecaptureSequence()
                        }
                    }
                }
                State.WaitingPrecapture -> {
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null ||
                        aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                        aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED
                    ) state = State.WaitingNonePrecapture
                }
                State.WaitingNonePrecapture -> {
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        state = State.PictureTaken
                        captureStillPicture()
                    }
                }
                else -> {
                }
            }
        }

        override fun onCaptureStarted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            timestamp: Long,
            frameNumber: Long
        ) {


        }

        override fun onCaptureProgressed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            partialResult: CaptureResult
        ) {
            process(partialResult)
        }

        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            process(result)
        }
    }

    private val stateCallback = object : CameraDevice.StateCallback() {

        override fun onOpened(cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            currentCameraDevice = cameraDevice
            createCameraPreviewSession()
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            cameraDevice.close()
            currentCameraDevice = null
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            cameraOpenCloseLock.release()
            cameraDevice.close()
            currentCameraDevice = null
            onError(Error.CameraState)
        }
    }

    private val onImageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        backgroundExecutor?.execute {
            val image = reader.acquireNextImage()
            if (file != null) {
                backgroundExecutor?.backgroundHandler?.post(CameraUtil.ImageSaver(image, file!!))
            } else {
                onCapture(image)
                image.close()
            }
        }
    }


    private val onExtractionAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        backgroundExecutor?.execute {
            val image = reader.acquireNextImage()
            if (isExtraction) onExtract(image)
            image.close()
        }
    }
}