package com.raulvieira.nextstoptoronto.database

import com.raulvieira.nextstoptoronto.models.RouteConfigModel
import com.raulvieira.nextstoptoronto.models.RouteListModel
import com.raulvieira.nextstoptoronto.models.StopPredictionModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitInterface {

    @GET("publicJSONFeed?command=routeList")
    suspend fun requestRouteList(
        @Query("a") agency: String = "ttc"
    ): Response<RouteListModel>

    @GET("publicJSONFeed?command=predictions")
    suspend fun requestStopPrediction(
        @Query("a") agency: String = "ttc",
        @Query("stopId") stopId: String
    ): Response<StopPredictionModel>

    @GET("publicJSONFeed?command=routeConfig")
    suspend fun requestRouteConfig(
        @Query("a") agency: String = "ttc",
        @Query("r") routeTag: String
    ): Response<RouteConfigModel>

//    @GET("publicJSONFeed?command=predictions")
//    suspend fun requestStopPredictionByRoute(
//        @Query("a") agency: String = "ttc",
//        @Query("r") routeTag: String,
//        @Query("s") stopTag: String
//    ): Response<StopPredictionModel>

    @GET("publicJSONFeed?command=predictionsForMultiStops")
    suspend fun requestPredictionsForMultiStops(
        @Query("a") agency: String = "ttc",
        @Query("stops") stops: List<String>
    ): Response<StopPredictionModel>

}