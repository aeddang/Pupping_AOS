package com.skeleton.component.player
import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.widget.ImageView
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener
import com.google.android.exoplayer2.metadata.Metadata
import com.google.android.exoplayer2.metadata.MetadataOutput
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.video.VideoSize
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.framework.CastContext
import com.lib.page.PageComponent
import com.lib.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


abstract class ExoVideoPlayer :  PageComponent, PlayBack,
    Player.Listener,
    MetadataOutput,
    SessionAvailabilityListener {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)
    private val appTag = javaClass.simpleName
    companion object {
        private  var viewModelInstance: ExoPlayerViewModel? = null
        fun getViewmodel(): ExoPlayerViewModel {
            return if(viewModelInstance == null) ExoPlayerViewModel() else viewModelInstance!!
        }
    }
    val playWhenReady:Boolean
    get() {
        currentPlayer?: return false
        return currentPlayer!!.playWhenReady
    }
    protected var delegate: PlayBackDelegate? = null
    final override fun setOnPlayerListener( _delegate:PlayBackDelegate? ){ delegate = _delegate }
    protected open fun hasPlayerPermission():Boolean = true

    protected var timeDelegate: PlayBackTimeDelegate? = null
    final override fun setOnPlayTimeListener( _delegate:PlayBackTimeDelegate? ){
        timeDelegate = _delegate
        if(timeDelegate == null){
            stopTimeSearch()
            return
        }
        if( sharedModel.playWhenReady )  startTimeSearch()
    }

    private var isSearch = false
    private fun startTimeSearch() {
        if(isSearch) return
        isSearch = true
        Log.d(appTag, "startTimeSearch")
        scope.launch(){
            while (isSearch){
                //Log.d(appTag, "on startTimeSearch")
                currentPlayer?.let {
                    //Log.d(appTag, "on currentPlayerTimeSearch")
                    onTimeChange(it.currentPosition)
                }
                delay(10)
            }
        }
    }
    private fun stopTimeSearch() {
        if(!isSearch) return
        isSearch = false
        Log.d(appTag, "stopTimeSearch")
    }

    protected  var sharedModel:ExoPlayerViewModel = getViewmodel()
    protected  lateinit var castSession:CastPlayer
    private  var exoPlayer: ExoPlayer? = null
    private  var castPlayer: CastPlayer? = null
    protected  var currentPlayer:Player? = null
    var mediaType = MediaType.Player; private set
    private var playerView:StyledPlayerView? = null
    var initImage:ImageView? = null; private set

    @StringRes abstract fun getAppName():Int
    abstract fun getPlayerView():StyledPlayerView
    protected open fun getInitImageView(): ImageView? { return null }

    @CallSuper
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initImage = getInitImageView()
        castSession = CastPlayer( CastContext.getSharedInstance(context) )
        castSession.setSessionAvailabilityListener(this)
        initPlayer()
    }

    @CallSuper
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        releasePlayer()
        castSession.setSessionAvailabilityListener(null)
        castSession.release()
        sharedModel.reset()
        stopTimeSearch()
        delegate = null
        timeDelegate = null
    }

    @CallSuper
    override fun onPause(){
        releasePlayer()
    }

    @CallSuper
    override fun onResume() {
        initPlayer()
    }

    @CallSuper
    open fun initPlayer() {
        if( currentPlayer != null ) return

        val audioAttributes = AudioAttributes.Builder()
        .setUsage(C.USAGE_MEDIA)
        .setContentType(C.CONTENT_TYPE_MOVIE)
        .build()

        if(castSession.isCastSessionAvailable){
            mediaType = MediaType.Cast
            castPlayer = castSession
            currentPlayer = castPlayer
            castPlayer?.addListener( this )

        }else{
            mediaType = MediaType.Player
            playerView = getPlayerView()
            exoPlayer = ExoPlayer.Builder(context).build()
            currentPlayer = exoPlayer
            exoPlayer?.addListener( this )
            exoPlayer?.setAudioAttributes(audioAttributes, true)
            playerView?.player = exoPlayer

        }
        prepare()
        setVolume(sharedModel.currentVolume)

        if( sharedModel.playWhenReady )  resume()
        else pause()
    }

    @CallSuper
    open fun releasePlayer() {
        exoPlayer?.let {
            sharedModel.playbackPosition = it.currentPosition
            sharedModel.currentWindow = it.currentMediaItemIndex
            sharedModel.playWhenReady = it.playWhenReady
            it.removeListener( this )
            it.release()
            exoPlayer = null
            playerView = null

        }

        castPlayer?.let {
            sharedModel.playbackPosition = it.currentPosition
            sharedModel.currentWindow = it.currentMediaItemIndex
            sharedModel.playWhenReady = it.playWhenReady
            it.removeListener( this )
            it.stop()
            castPlayer = null
        }

        stopTimeSearch()
        currentPlayer = null
    }

    private fun buildDataSource(uri: Uri): MediaSource {
        val dataSourceFactory = DefaultDataSource.Factory(context)
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri))
    }
    enum class ExtensionType(val value:String){
        HLS(MimeTypes.APPLICATION_M3U8),MPD(MimeTypes.APPLICATION_MPD),PROGRESS( MimeTypes.VIDEO_MP4),UNDEFIEND(MimeTypes.APPLICATION_M3U8)
    }

    private fun getDeterminedExtension(extension:String? = null):ExtensionType{
        extension ?: return ExtensionType.UNDEFIEND
        val ext = extension.lowercase()
        return if (ext.contains("m3u8")) ExtensionType.HLS
        else if (ext.contains("mp3") || extension.contains("mp4")) ExtensionType.PROGRESS
        else if(ext.contains("mpd") || ext.contains("mp")) ExtensionType.MPD
        else ExtensionType.UNDEFIEND
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val extension = getDeterminedExtension( uri.lastPathSegment )
        val media = MediaItem.fromUri(uri)
        return  when (extension){
            ExtensionType.HLS -> HlsMediaSource.Factory(DefaultHttpDataSource.Factory()).createMediaSource(media)
            ExtensionType.MPD -> {
                val dashChunkSourceFactory = DefaultDashChunkSource.Factory(DefaultHttpDataSource.Factory())
                val manifestDataSourceFactory = DefaultHttpDataSource.Factory()
                DashMediaSource.Factory(dashChunkSourceFactory, manifestDataSourceFactory).createMediaSource(media)
            }
            ExtensionType.PROGRESS -> ProgressiveMediaSource.Factory(DefaultHttpDataSource.Factory()).createMediaSource(media)
            ExtensionType.UNDEFIEND -> HlsMediaSource.Factory(DefaultHttpDataSource.Factory()).createMediaSource(media)
        }
    }

    private fun buildMediaQueueItem(uri: Uri) : MediaQueueItem {
        val type = getDeterminedExtension( uri.lastPathSegment )
        val extension = type.value
        val movieMetadata = getMediaMetadata() ?: MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)
        val mediaInfo = MediaInfo.Builder(uri.toString())
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType(extension)
            .setMetadata(movieMetadata).build()
        return MediaQueueItem.Builder(mediaInfo).build()
    }
    protected open fun getMediaMetadata():MediaMetadata? = null

    private var isCompleted = false
    private var isInit = false
    protected var initTime = 0L

    private fun prepare(){
        sharedModel.source ?: return
        when (mediaType){
            MediaType.Player -> {
                exoPlayer?.setMediaSource(sharedModel.source!!)
                currentPlayer?.seekTo(sharedModel.currentWindow, sharedModel.playbackPosition)
            }
            MediaType.Cast -> {
                castPlayer?.addMediaItem( MediaItem.fromUri(sharedModel.videoPath!!) )
                castPlayer?.seekTo(sharedModel.currentWindow, sharedModel.playbackPosition)
            }
        }
    }

    @CallSuper
    override fun load(videoPath:String, initTime:Long, isDataSorce:Boolean) {
        Log.d(appTag, "videoPath $videoPath initTime $initTime")
        sharedModel.videoPath = videoPath
        val uri = Uri.parse(videoPath)
        val source:MediaSource = if( isDataSorce ) buildDataSource(uri) else buildMediaSource(uri)
        isInit = false
        isCompleted = false
        sharedModel.source = source
        prepare()
        initImage?.visibility = VISIBLE
        this.initTime = initTime

    }

    override fun reload(){
        isCompleted = false
        sharedModel.source?.let {
            prepare()
            resume()
            return
        }
        onError(-1)
    }



    override fun pause(){
        sharedModel.playWhenReady = false
        currentPlayer?.playWhenReady = false
        delegate?.onStop(this)
        stopTimeSearch()
    }


    override fun resume(){
        if(isCompleted){
            reload()
            return
        }

        sharedModel.playWhenReady = true
        currentPlayer?.playWhenReady = true
        initImage?.visibility = GONE
        delegate?.onPlay(this)
        if( timeDelegate != null ) startTimeSearch()
    }

    @CallSuper
    override fun seek(t:Long){
        Log.d(appTag, "seek $t")
        stopTimeSearch()
        currentPlayer?.seekTo(sharedModel.currentWindow, t)
    }

    @CallSuper
    override fun seekMove(t:Long){
        Log.d(appTag, "seek $t")
        stopTimeSearch()
        currentPlayer?.let {
            val target = it.contentPosition + t
            it.seekTo(sharedModel.currentWindow, target)
        }
    }

    @CallSuper
    override fun setVolume(v: Float) {
        sharedModel.currentVolume = v
        exoPlayer?.volume = v

    }

    @CallSuper
    override fun onCompleted(){
        isCompleted = true
        pause()
        initImage?.visibility = VISIBLE


    }
    @CallSuper
    override fun onError(e:Any?){
        pause()
        Log.d(appTag, "onError $e")
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

    override fun onTimeChange(t:Long){
        timeDelegate?.onTimeChanged(this, t)
    }
    override fun onMetadata(metadata: Metadata) {}
    override fun onMediaMetadataChanged(mediaMetadata: com.google.android.exoplayer2.MediaMetadata) {}

    @CallSuper
    override fun onLoadingChanged(isLoading: Boolean) {
        Log.d(appTag, "onLoadingChanged $isLoading")
        if( isLoading && !isInit ){
            isInit = true
            onInit()
            val d = exoPlayer?.duration ?: 0L
            delegate?.onLoad(this, d)
            Log.d(appTag, "onInit $initTime")
            if(initTime != 0L) seek(initTime)
            initTime = 0L
        }
    }
    @CallSuper
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        Log.d(appTag, "onPlayerStateChanged $playbackState")
        when(playbackState){
            Player.STATE_ENDED -> {
                onCompleted()
                stopTimeSearch()
                delegate?.onCompleted(this)
            }
            Player.STATE_BUFFERING -> onBuffering()
            Player.STATE_READY -> {
                if(!isInit) onLoadingChanged(true)
                onReady()
            }
        }
    }

    private fun changePlayer(){
        if(initTime == 0L) initTime = currentPlayer?.currentPosition ?: 0L
        releasePlayer()
        initPlayer()
        delegate?.onChangedPlayer(mediaType)
        onChangedPlayer(mediaType)
    }
    protected open fun onChangedPlayer(mediaType:MediaType){}
    override fun onCastSessionAvailable() { changePlayer() }
    override fun onCastSessionUnavailable() { changePlayer() }

    @CallSuper
    override fun onPlayerError(error: PlaybackException) {
        onError(error)
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        Log.d(appTag, "onTimelineChanged $timeline")
    }
    override fun onTracksChanged( trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {
        Log.d(appTag, "onTracksChanged $trackGroups")
    }

    @CallSuper
    override fun onSeekProcessed() {
        Log.d(appTag, "onSeekProcessed")
        if( sharedModel.playWhenReady && timeDelegate != null ) startTimeSearch()
    }

    override fun onRepeatModeChanged(@Player.RepeatMode repeatMode: Int) {}
    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}
    override fun onPositionDiscontinuity(@Player.DiscontinuityReason reason: Int) {}
    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}
    override fun onVideoSizeChanged(videoSize: VideoSize) { }
    override fun onSurfaceSizeChanged(width: Int, height: Int) {}
    override fun onRenderedFirstFrame() {}


    class ExoPlayerViewModel{

        var playbackPosition:Long = 0
        var currentWindow:Int = 0
        var videoPath:String? = null
        var playWhenReady:Boolean = false
        var source: MediaSource? = null
        var currentVolume:Float = 1.0f


        fun reset(){
            source = null
            videoPath = null
            playbackPosition = 0
            currentWindow = 0
        }

        fun destroy(){
            source = null
            videoPath = null
        }

    }


}