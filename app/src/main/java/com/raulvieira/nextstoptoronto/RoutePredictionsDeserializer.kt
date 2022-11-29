package com.raulvieira.nextstoptoronto

import com.google.gson.*
import com.raulvieira.nextstoptoronto.models.PredictionModel
import com.raulvieira.nextstoptoronto.models.RoutePredictionsModel
import java.lang.reflect.Type

class RoutePredictionsDeserializer : JsonDeserializer<RoutePredictionsModel> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): RoutePredictionsModel {
        if (json == null) {
            return RoutePredictionsModel("", "", "", "", listOf())
        }
        val gson = GsonBuilder().apply {
            registerTypeAdapter(PredictionModel::class.java, PredictionModelDeserializer())
        }.create()
        val jsonObject = json.asJsonObject

        val routeTag = if (jsonObject.has("routeTag")) jsonObject.get("routeTag").asString else ""
        val stopTag = if (jsonObject.has("stopTag")) jsonObject.get("stopTag").asString else ""
        val routeTitle =
            if (jsonObject.has("routeTitle")) jsonObject.get("routeTitle").asString else ""
        val stopTitle =
            if (jsonObject.has("stopTitle")) jsonObject.get("stopTitle").asString else ""
        val direction: ArrayList<PredictionModel> = arrayListOf()
        if (jsonObject.has("direction")) {
            val data = jsonObject.get("direction")
            val elements: JsonArray = if (data.isJsonArray) data.asJsonArray else {
                JsonArray(1).apply { add(data) }
            }

            elements.forEach { element ->
                direction.add(gson.fromJson(element, PredictionModel::class.java))
            }
        }
        return RoutePredictionsModel(routeTag, stopTag, routeTitle, stopTitle, direction)
    }
}