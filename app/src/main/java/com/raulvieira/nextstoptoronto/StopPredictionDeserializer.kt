package com.raulvieira.nextstoptoronto

import com.google.gson.*
import com.raulvieira.nextstoptoronto.models.PredictionModel
import com.raulvieira.nextstoptoronto.models.RoutePredictionsModel
import com.raulvieira.nextstoptoronto.models.StopPredictionModel
import java.lang.reflect.Type

class StopPredictionDeserializer : JsonDeserializer<StopPredictionModel> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): StopPredictionModel {
        if (json == null) {
            return StopPredictionModel(listOf())
        }
        val gson = GsonBuilder().apply {
            registerTypeAdapter(RoutePredictionsModel::class.java, RoutePredictionsDeserializer())
        }.create()
        val jsonObject = json.asJsonObject


        val predictions: ArrayList<RoutePredictionsModel> = arrayListOf()
        if (jsonObject.has("predictions")) {
            val data = jsonObject.get("predictions")
            val elements: JsonArray = if (data.isJsonArray) data.asJsonArray else {
                JsonArray(1).apply { add(data) }
            }

            elements.forEach { element ->
                predictions.add(gson.fromJson(element, RoutePredictionsModel::class.java))
            }
        }
        return StopPredictionModel(predictions)
    }
}