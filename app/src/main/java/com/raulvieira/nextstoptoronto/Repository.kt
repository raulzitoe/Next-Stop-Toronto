package com.raulvieira.nextstoptoronto

class Repository(private val apiService: RetrofitInterface) {

    suspend fun getRouteList() = apiService.requestRouteList()

}