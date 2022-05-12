package com.skeleton.component.camera


class CameraViewModel{

    var flashSupported: Boolean = false; internal set
    var isFront:Boolean = true
    var flashType:Camera.FlashType = Camera.FlashType.Off
    var permissionGranted: Camera.PermissionGranted = Camera.PermissionGranted.UnChecked

    fun resetPermissionGranted(){
        if(permissionGranted == Camera.PermissionGranted.Denied) permissionGranted = Camera.PermissionGranted.UnChecked
    }

    fun destroy(){

    }

}