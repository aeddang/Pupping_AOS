package com.skeleton.component.player

interface PlayBack{
    fun load(videoPath:String, initTime:Long = 0, isDataSorce:Boolean = false)
    fun reload()
    fun pause()
    fun resume()
    fun seek(t:Long)
    fun seekMove(t:Long)
    fun onPause()
    fun onResume()
    fun setVolume(v:Float)
    fun setOnPlayerListener( _delegate:PlayBackDelegate? )
    fun setOnPlayTimeListener( _delegate:PlayBackTimeDelegate? )
    fun onInit(){}
    fun onCompleted(){}
    fun onError(e:Any?){}
    fun onBuffering(){}
    fun onReady(){}
    fun onTimeChange(t:Long){}
}

interface PlayBackDelegate{
    fun onChangedPlayer(mediaType:MediaType){}
    fun onLoad(player: PlayBack, duration:Long ){}
    fun onPlay(player: PlayBack){}
    fun onStop(player: PlayBack ){}
    fun onBuffering(player: PlayBack){}
    fun onReady(player: PlayBack){}
    fun onCompleted(player: PlayBack ){}

    fun onError(player: PlayBack, error: Any){}
}

interface PlayBackTimeDelegate{
    fun onTimeChanged(player: PlayBack, t:Long )
}

enum class MediaType{
    Player, Cast
}
