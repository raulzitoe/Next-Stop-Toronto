package com.raulvieira.nextstoptoronto

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class RoutePredictionsDeserializer : JsonDeserializer<RoutePredictionsModel> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): RoutePredictionsModel {
        if (json == null) {
            return RoutePredictionsModel("", "", "", "", arrayListOf())
        }
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
                direction.add(Gson().fromJson(element, PredictionModel::class.java))
            }
        }
        return RoutePredictionsModel(routeTag, stopTag, routeTitle, stopTitle, direction)
    }
}