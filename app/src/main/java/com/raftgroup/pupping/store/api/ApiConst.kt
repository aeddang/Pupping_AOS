package com.raftgroup.pupping.store.api


object Api {
    private const val VERSION_V1 = "v1"
    const val ACTION = "action"
    const val CONTENT_ID = "contentID"

    /**
     * API PATH
     */
    object Auth {
        private const val PATH = "auth"
        const val Login = "$VERSION_V1/$PATH/login"
    }

    object User {
        private const val PATH = "users"
        const val Users = "$VERSION_V1/$PATH"
        const val User = "$VERSION_V1/$PATH/{${CONTENT_ID}}"
    }

    object Pet {
        private const val PATH = "pets"
        const val Pets = "$VERSION_V1/$PATH"
        const val Pet = "$VERSION_V1/$PATH/{${CONTENT_ID}}"
    }

    object Misc {
        private const val PATH = "misc"
        const val weather = "$VERSION_V1/$PATH/weather"
    }

    object Mission {
        private const val PATH = "missions"
        const val missions = "$VERSION_V1/$PATH"
        const val search = "$VERSION_V1/$PATH/search"
        const val summary = "$VERSION_V1/$PATH/summary"
    }

    object Album {
        private const val PATH = "album"
        const val pictures = "$VERSION_V1/$PATH/pictures"
        const val picturesThumbsup = "${Album.pictures}/thumbsup"
    }

    object Vision {
        private const val PATH = "vision"
        const val detect = "$VERSION_V1/$PATH/images/detecthumanwithdog"
    }
}



object ApiValue{
    const val PAGE_SIZE = 20
}

object ApiAction {
    const val password  = "password"
    const val accessToken = "accesstoken"

}

object ApiCode{
    const val  invalidToken = "C001"
    const val  notFound = "C005" // 결과값이 없습니다 ** 정상으로 받은결과값 data가 null일때
}

object ApiField {
    const val lat = "lat"
    const val lng = "lng"
    const val email = "email"
    const val distance = "distance"
    const val userId = "userId"
    const val petId ="petId"
    const val searchType = "searchType"
    const val missionCategory = "missionCategory"
    const val page = "page"
    const val size = "size"

    const val name = "name"
    const val breed = "breed"
    const val birthdate = "birthdate"
    const val sex = "sex"
    const val regNumber = "regNumber"
    const val level = "level"
    const val weight = "weight"
    const val status = "status"

    const val pictureType = "pictureType"
    const val ownerId = "ownerId"
    const val pictureIds = "pictureIds"
}

enum class ApiType{
    AuthLogin, AuthReflash , GetUser, UpdateUser,
    GetWeather,
    GetMission, SearchMission, CompleteMission, CompleteWalk, GetMissionSummary,
    GetPet, GetPets, RegistPet, UpdatePetImage, UpdatePet, DeletePet,
    GetAlbumPictures, RegistAlbumPicture, DeleteAlbumPictures, UpdateAlbumPictures,
    CheckHumanWithDog
}