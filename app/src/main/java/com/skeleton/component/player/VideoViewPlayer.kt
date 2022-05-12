package com.skeleton.component.player

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaPlayer.*
import android.util.AttributeSet
import android.widget.VideoView
import androidx.annotation.CallSuper
import com.lib.page.PageComponent
import com.lib.util.Log


abstract class VideoViewPlayer : PageComponent, PlayBack, OnErrorListener, OnPreparedListener, OnInfoListener{
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)

    private val appTag = javaClass.simpleName
    protected var isCompleted = false
    protected var isInit = false
    protected var currentVideoPath:String? = null
    protected var initTime = 0L
    protected var currentVolume:Float = 1.0f
    protected var mediaPlayer:MediaPlayer? = null
    var playWhenReady = false; protected set
    var isBuffring = false; protected set

    var duration = 0L ; private set

    lateinit var player:VideoView
    abstract fun getPlayerView(): VideoView
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        player =  getPlayerView()
        player.setOnErrorListener(this)
        player.setOnPreparedListener(this)
        player.setOnInfoListener(this)
    }
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        player.stopPlayback()
        stopTimeSearch()
        mediaPlayer = null
        delegate = null
        timeDelegate = null
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
        timeDisposable = Observable.interval(100, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if( isBuffring ) return@subscribe
                player.let {
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
        Log.d(appTag, "videoPath $videoPath initTime $initTime")
        isInit = false
        isCompleted = false
        onBuffering()
        currentVideoPath = videoPath
        this.initTime = initTime
        try { player.setVideoPath(videoPath) }
        catch (e: Exception) { onError(e)}
        if(playWhenReady) resume() else pause()
    }

    @CallSuper
    override fun reload() {
        isCompleted = false
        player.start()
        resume()
    }

    @CallSuper
    override fun pause() {
        playWhenReady = false
        stopTimeSearch()
        player.pause()
        delegate?.onStop(this)
        onReady()
    }

    @CallSuper
    override fun resume() {
        if(isCompleted){
            reload()
            return
        }
        playWhenReady = true
        if(!isBuffring) onReady()
        player.start()
        delegate?.onPlay(this)
        if( timeDelegate != null ) startTimeSearch()

    }

    @CallSuper
    override fun seek(t: Long) {
        onBuffering()
        player.seekTo(t.toInt())
    }

    @CallSuper
    override fun seekMove(t: Long) {
        onBuffering()
        val target = player.currentPosition + t
        player.seekTo(target.toInt())
    }

    override fun setVolume(v: Float) {
        currentVolume = v
        try { mediaPlayer?.setVolume(v,v) }
        catch (e: Exception) { Log.d(appTag, "error setVolume ${e.message}")}

    }

    @CallSuper
    override fun onPause() {
        stopTimeSearch()
        player.pause()
    }

    @CallSuper
    override fun onResume() {
        player.resume()
        if(playWhenReady) resume()
    }

    @CallSuper
    override fun onCompleted(){
        isCompleted = true
        pause()
        delegate?.onCompleted(this)
    }
    @CallSuper
    override fun onError(e:Any?){
        pause()
        delegate?.onError(this, e ?: -1)
    }
    @CallSuper
    override fun onBuffering(){
        isBuffring = true
        delegate?.onBuffering(this)
    }

    @CallSuper
    override fun onReady(){
        isBuffring = false
        delegate?.onReady(this)
    }

    @CallSuper
    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        onError( what)
        return true
    }

    @CallSuper
    override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        Log.i(appTag, "onInfo $what")
        when(what){
            MEDIA_INFO_VIDEO_RENDERING_START -> {}
            MEDIA_INFO_BUFFERING_START -> onBuffering()
            MEDIA_INFO_BUFFERING_END -> onReady()
            else ->{}
        }
        return false
    }


    @CallSuper
    override fun onPrepared(mp: MediaPlayer?) {
        Log.i(appTag, "onPrepared")
        mediaPlayer = mp
        mediaPlayer?.setVolume(currentVolume,currentVolume)
        if( !isInit ){
            isInit = true
            duration = mp?.duration?.toLong() ?: 0L
            onInit()
            delegate?.onLoad(this, duration)
            mediaPlayer?.setOnErrorListener{ mp, w, e -> onError(mp, w, e) }
            mediaPlayer?.setOnSeekCompleteListener { onReady() }
            player.setOnCompletionListener { onCompleted() }

            Log.i(appTag, "onInit $initTime")
            if(initTime != 0L) seek(initTime)
            initTime = 0L
        }

        onReady()
    }

}