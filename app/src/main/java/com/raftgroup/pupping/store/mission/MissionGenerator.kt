package com.raftgroup.pupping.store.mission
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.lib.util.DataLog
import java.util.*
import kotlin.collections.ArrayList

enum class MissionGeneratorEventType {
    Created
}

enum class MissionGeneratorErrorType {
    ApiError, NotFound
}

enum class MissionGeneratorRequestType {
    Create
}

data class MissionGeneratorEvent (
    var id:String? = UUID.randomUUID().toString(),
    val type:MissionGeneratorEventType,
    val mission:Mission? = null
)

data class MissionGeneratorError(
    var id:String? = UUID.randomUUID().toString(),
    val type:MissionGeneratorErrorType,
)

data class MissionRequestQ (
    var id:String = UUID.randomUUID().toString(),
    val type:MissionGeneratorRequestType,
    var missionType:MissionType? = null,
    var playType:MissionPlayType? = null,
    var lv:MissionLv? = null,
    var keyword:String? = null
)

data class CashedPlace (
    val date:Date,
    val places:List<Place>
){
    companion object {
        const val resetTime:Double = 10.0
    }
}

class MissionGenerator(
    private val context: Context
) {
    private val appTag = javaClass.simpleName
    val request = MutableLiveData<MissionRequestQ?>(null)
    val event = MutableLiveData<MissionGeneratorEvent?>(null)
    val error = MutableLiveData<MissionGeneratorError?>(null)
    val isBusy = MutableLiveData<Boolean>(false)

    private val placeFields: List<Place.Field> = listOf(Place.Field.NAME,  Place.Field.LAT_LNG, Place.Field.ID)
    private val lookupFields: List<Place.Field> = listOf(Place.Field.NAME,  Place.Field.LAT_LNG, Place.Field.ID)
    var finalLocation:Location? = null
    private var requestQ :ArrayList<MissionRequestQ> = arrayListOf()
    private var currentId:String? = null

    fun request(mission:MissionRequestQ){
        if (isBusy.value == true) {
            requestQ.add(mission)
            return
        }
        requestAction(mission)
    }
    fun request(type:MissionGeneratorRequestType, id:String? = null){
        val q = MissionRequestQ(id ?: UUID.randomUUID().toString(), type)
        if (isBusy.value == true) {
            requestQ.add(q)
            return
        }
        requestAction(q)
    }
    private fun requestAction(q:MissionRequestQ){
        currentId = q.id
        request.value = q
        when(q.type) {
            MissionGeneratorRequestType.Create -> {
                createMission(q.missionType, q.playType, q.lv, q.keyword)
            }
        }
    }

    private fun executeQ(){
        isBusy.value = false
        if (requestQ.isNotEmpty()) { request(requestQ.removeFirst()) }
    }

    private fun createMission(type:MissionType?,playType:MissionPlayType?, lv:MissionLv?, keyword:String?){
        isBusy.value = true
        val genType = type ?: MissionType.random()
        val genPlayType = playType ?: MissionPlayType.random()
        val genLv = lv ?: MissionLv.random()
        val mission = Mission(genType, genPlayType, genLv)
        when(genPlayType){
            MissionPlayType.Nearby -> {
                getCurrentPlace { place, nearbyPlaces ->
                    mission.add(place)
                    mission.add(nearbyPlaces)
                    created(mission)
                }
            }
            MissionPlayType.Location  -> {
                val genKeyword = keyword ?: MissionKeyword.random().keyword()
                getCurrentPlace { place, _ ->
                    mission.add(place)
                    getKeywordPlace(genKeyword){ keywordPlaces ->
                        mission.addRecommandPlaces( keywordPlaces)
                        val keywordPlace = keywordPlaces.random()
                        lookUp(keywordPlace.placeId){ place ->
                            mission.add(arrayListOf(place))
                            created(mission)
                        }
                    }
                }
            }
            MissionPlayType.Endurance -> {
                getCurrentPlace(){ place, _ ->
                    mission.add(place)
                    created(mission)
                }
            }
            MissionPlayType.Speed -> {
                getCurrentPlace(){ place, _ ->
                    mission.add(place)
                    created(mission)
                }
            }
        }
    }

    private fun created (mission:Mission?){
        mission?.build(context)
        event.value = MissionGeneratorEvent( currentId, MissionGeneratorEventType.Created, mission)
        executeQ()
    }
    private fun lookUp(placeId:String, completionHandler:(Place) -> Unit){
        isBusy.value = true
        val request = FetchPlaceRequest.newInstance(placeId, lookupFields)
        val placeResult = Places.createClient(context).fetchPlace(request)
        placeResult.addOnCompleteListener {  task ->
            if (task.isSuccessful) {
                val response = task.result
                val find = response?.place
                if (find == null) {
                    error.value = MissionGeneratorError(currentId  , MissionGeneratorErrorType.NotFound)
                    executeQ()
                    return@addOnCompleteListener
                }
                DataLog.d("lookup Place '${find}'", appTag)
                completionHandler(find)
            } else {
                val exception = task.exception
                if (exception is ApiException) {
                    DataLog.e("Place not found: ${exception.statusCode}", appTag)
                    error.value = MissionGeneratorError(currentId  , MissionGeneratorErrorType.ApiError)
                    executeQ()
                }
            }
        }
    }

    private var cashedCurrentPlace:CashedPlace? = null
    @SuppressLint("MissingPermission")
    private fun getCurrentPlace(completionHandler: (Place, List<Place>) -> Unit) {
        cashedCurrentPlace?.let { cashed ->
            val diff = Date().time - cashed.date.time
            if (diff < CashedPlace.resetTime && cashed.places.isNotEmpty()) {
                completionHandler(cashed.places.first(), cashed.places.drop(1))
                return
            }
        }
        val request = FindCurrentPlaceRequest.newInstance(placeFields)
        val placeResult = Places.createClient(context).findCurrentPlace(request)
        placeResult.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val response = task.result
                val find = response?.placeLikelihoods
                if (find == null) {
                    error.value = MissionGeneratorError(currentId  , MissionGeneratorErrorType.NotFound)
                    executeQ()
                    return@addOnCompleteListener
                }
                for (placeLikelihood: PlaceLikelihood in response.placeLikelihoods ?: emptyList()) {
                    DataLog.d(
                        "Place '${placeLikelihood.place.name}' has likelihood: ${placeLikelihood.likelihood}", appTag
                    )
                }
                val places = find.map { it.place }
                val cplace = places.first()
                cashedCurrentPlace = CashedPlace(Date(),places)
                completionHandler(cplace, places.drop(1))
            } else {
                val exception = task.exception
                if (exception is ApiException) {
                    DataLog.e("Place not found: $exception", appTag)
                    error.value = MissionGeneratorError(currentId  , MissionGeneratorErrorType.ApiError)
                    executeQ()
                }
            }
        }
    }

    private fun getKeywordPlace(ketword:String, completionHandler: (List<AutocompletePrediction>) -> Unit){
        val request = FindAutocompletePredictionsRequest.newInstance(ketword)
        val placeResult = Places.createClient(context).findAutocompletePredictions(request)
        placeResult.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val response = task.result
                val find = response?.autocompletePredictions
                if (find == null) {
                    error.value = MissionGeneratorError(currentId  , MissionGeneratorErrorType.NotFound)
                    executeQ()
                    return@addOnCompleteListener
                }
                for (prediction: AutocompletePrediction in find  ?: emptyList()) {
                    DataLog.d(
                        "AutocompletePrediction '${prediction}'", appTag
                    )
                }
                completionHandler(find)
            } else {
                val exception = task.exception
                if (exception is ApiException) {
                    DataLog.e("Place not found: ${exception.statusCode}", appTag)
                    error.value = MissionGeneratorError(currentId  , MissionGeneratorErrorType.ApiError)
                    executeQ()
                }
            }
        }
    }
}



