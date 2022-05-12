package com.raftgroup.pupping.store.mission

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.lib.page.PageLifecycleUser
import com.lib.util.DataLog

class MissionManager (val generator:MissionGenerator): PageLifecycleUser {
    private val appTag = javaClass.simpleName
    val missions:MutableList<Mission> = arrayListOf()
    val isMissionsUpdated = MutableLiveData<Boolean>(false)
    val currentMission = MutableLiveData<Mission?>(null)
    override fun setDefaultLifecycleOwner(owner: LifecycleOwner) {
        generator.event.observe(owner){ evt ->
            evt ?: return@observe
            when (evt.type) {
                MissionGeneratorEventType.Created -> {
                    if (evt.id == appTag) {
                        evt.mission?.let {
                            missions.add(it)
                            generatedMission()
                        }
                    }
                }
            }
            generator.event.value = null
        }
        generator.error.observe(owner) { err ->
            err ?: return@observe
            when (err.type) {
                MissionGeneratorErrorType.ApiError -> if (err.id == appTag) {
                    generatedMission()
                }
                MissionGeneratorErrorType.NotFound -> if (err.id == appTag) {
                    generatedMission()
                }
            }
            generator.error.value = null
        }

    }

    override fun disposeDefaultLifecycleOwner(owner: LifecycleOwner) {
        generator.event.removeObservers(owner)
        generator.error.removeObservers(owner)
    }

    private var generateCount:Int = 3
    private var createCount:Int = 0

    val isBusy:Boolean; get(){
        return generator.isBusy.value == true
    }

    fun generateMission() {
        missions.clear()
        createCount = 0
        generator.request(
            MissionRequestQ(appTag, MissionGeneratorRequestType.Create, MissionType.Today, MissionPlayType.Nearby)
        )
        generator.request(
            MissionRequestQ(appTag, MissionGeneratorRequestType.Create, MissionType.Always)
        )
        generator.request(
            MissionRequestQ(appTag, MissionGeneratorRequestType.Create, MissionType.Event, MissionPlayType.Location)
        )
    }

    private fun generatedMission() {
        createCount += 1
        DataLog.d("generatedMission $createCount / $generateCount",appTag )
        if (createCount == generateCount) {
            isMissionsUpdated.postValue(true)
            DataLog.d("generateMission completed",appTag )
        }
    }

    fun completedMission() {
        val mission = currentMission.value
        mission ?: return
        createCount -= 1
        missions.remove(mission)
        when (mission.type) {
            MissionType.Today -> {
                generator.request(
                    MissionRequestQ(appTag, MissionGeneratorRequestType.Create, MissionType.Today, MissionPlayType.Nearby)
                )
            }
            MissionType.Always -> {
                generator.request(
                    MissionRequestQ(appTag, MissionGeneratorRequestType.Create, MissionType.Always)
                )
            }
            MissionType.Event -> {
                generator.request(
                    MissionRequestQ(appTag, MissionGeneratorRequestType.Create, MissionType.Event, MissionPlayType.Location)
                )
            }
        }

    }

    fun addMission(mission:Mission) {
        missions.add(mission)
    }

    fun startMission(mission:Mission) {
        currentMission.value = mission
    }
    fun endMission() {
        currentMission.value = null
    }
}