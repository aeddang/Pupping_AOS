package com.skeleton.component.player

import android.media.MediaPlayer
import android.view.SurfaceHolder
import androidx.annotation.CallSuper
import com.lib.util.Log



open class MediaPlayerHandler(surfaceHolder:SurfaceHolder) : PlayBack,
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnErrorListener,
    MediaPlayer.OnSeekCompleteListener,
    MediaPlayer.OnBufferingUpdateListener,
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnVideoSizeChangedListener {


    private val appTag = javaClass.simpleName
    private var isCompleted = false
    private var isSurfaceReady = false
    protected var currentVolume:Float = 1.0f
    private var isInit = false
    protected var player:MediaPlayer = MediaPlayer()

    var currentVideoPath:String? = null; private set
    var duration = 0L ; private set
    var initTime = 0L ; private set
    var playWhenReady = false ; private set

    init {

        surfaceHolder.addCallback(object:SurfaceHolder.Callback{
            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {}
            override fun surfaceDestroyed(holder: SurfaceHolder) {}
            override fun surfaceCreated(holder: SurfaceHolder) {
                player.setDisplay(surfaceHolder)
                isSurfaceReady = true
                currentVideoPath?.let{ load(it, initTime) }
            }
        })
        player.setOnCompletionListener { this }
        player.setOnErrorListener(this)
        player.setOnPreparedListener(this)
        player.setOnBufferingUpdateListener(this)
        player.setOnSeekCompleteListener { this }
        player.setOnVideoSizeChangedListener( this )
    }

    fun destory(){
        player.release()
    }

    protected var delegate: PlayBackDelegate? = null
    final override fun setOnPlayerListener( _delegate:PlayBackDelegate? ){ delegate = _delegate }

    private var timeDelegate: PlayBackTimeDelegate? = null
    final override fun setOnPlayTimeListener( _delegate:PlayBackTimeDelegate? ){
        timeDelegate = _delegate
        if(timeDelegate == null){
            stopTimeSearch()
            return
        }
        if( playWhenReady )  startTimeSearch()
    }
    private fun startTimeSearch() {
        /*
        timeDisposable?.dispose()
        timeDisposable = Observable.interval(1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                player?.let {
                    val t = it.currentPosition.toLong()
                    onTimeChange(t)
                    timeDelegate?.onTimeChanged(this, t)

                }
            }
            */
    }
    private fun stopTimeSearch() {
        /*
        timeDisposable?.dispose()
        timeDisposable = null
         */
    }

    @CallSuper
    override fun load(videoPath: String, initTime: Long, isDataSorce: Boolean) {
        isInit = false
        isCompleted = false
        currentVideoPath = videoPath
        this.initTime = initTime
        if( !isSurfaceReady ) return
        try { player.setDataSource(videoPath) }
        catch (e: Exception) { onError(e)}

        player.prepare()
    }

    @CallSuper
    override fun reload() {
        isCompleted = false
        seek(0)
        player.start()
    }

    @CallSuper
    override fun pause() {
        playWhenReady = false
        stopTimeSearch()
        player.stop()
        delegate?.onStop(this)
    }

    @CallSuper
    override fun resume() {
        if(isCompleted){
            reload()
            return
        }
        playWhenReady = true
        player.start()
        delegate?.onPlay(this)
        if( timeDelegate != null ) startTimeSearch()

    }

    @CallSuper
    override fun seek(t: Long) {
        player.seekTo(t.toInt())
    }

    @CallSuper
    override fun seekMove(t: Long) {
        val target = player.currentPosition + t
        player.seekTo(target.toInt())
    }

    @CallSuper
    override fun setVolume(v: Float) {
        currentVolume = v
        player.setVolume(v, v)
    }

    @CallSuper
    override fun onPause() {
        pause()
    }

    @CallSuper
    override fun onResume() {
        if(playWhenReady) resume()
    }

    @CallSuper
    override fun onCompleted(){
        isCompleted = true
        pause()

    }
    @CallSuper
    override fun onError(e:Any?){
        pause()
        delegate?.onError(this, e ?: -1)
    }
    @CallSuper
    override fun onBuffering(){
        delegate?.onBuffering(this)
    }

    @CallSuper
    override fun onReady(){
        delegate?.onReady(this)
    }

    @CallSuper
    override fun onCompletion(mp: MediaPlayer?) {
        onCompleted()
        stopTimeSearch()
        delegate?.onCompleted(this)
    }

    @CallSuper
    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        onError( what)
        return true
    }

    @CallSuper
    override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
        Log.i(appTag, "percent $percent")
        onBuffering()
    }

    @CallSuper
    override fun onPrepared(mp: MediaPlayer?) {
        Log.i(appTag, "onPrepared")
        if( !isInit ){
            isInit = true
            duration = mp?.duration?.toLong() ?: 0L
            onInit()
            delegate?.onLoad(this, duration)
        }
        onReady()
    }

    override fun onSeekComplete(mp: MediaPlayer?) {}
    override fun onVideoSizeChanged(mp: MediaPlayer?, width: Int, height: Int) {}

}