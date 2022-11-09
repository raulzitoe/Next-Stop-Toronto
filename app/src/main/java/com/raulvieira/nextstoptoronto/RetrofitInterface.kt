package com.raulvieira.nextstoptoronto

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitInterface {

    @GET("publicJSONFeed?command=routeList")
    suspend fun requestRouteList(
        @Query("a") agency: String = "ttc"
    ): Response<RouteListModel>
}