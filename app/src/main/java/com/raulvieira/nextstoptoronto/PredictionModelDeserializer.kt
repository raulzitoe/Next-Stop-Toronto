package com.raulvieira.nextstoptoronto

import com.google.gson.*
import java.lang.reflect.Type

class PredictionModelDeserializer : JsonDeserializer<PredictionModel> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): PredictionModel {
        if (json == null) {
            return PredictionModel("", arrayListOf())
        }
        val jsonObject = json.asJsonObject

        val title = if (jsonObject.has("title")) jsonObject.get("title").asString else ""
        val predictions: ArrayList<SinglePredictionModel> = arrayListOf()
        if (jsonObject.has("prediction")) {
            val data = jsonObject.get("prediction")
            val elements: JsonArray = if (data.isJsonArray) data.asJsonArray else {
                JsonArray(1).apply { add(data) }
            }
            elements.forEach { element ->
                predictions.add(Gson().fromJson(element, SinglePredictionModel::class.java))
            }

        }
        return PredictionModel(title, predictions)
    }
}