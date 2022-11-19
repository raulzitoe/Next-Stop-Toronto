package com.raulvieira.nextstoptoronto.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites_table")
data class FavoritesModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val stopTag: String,
    val routeTag: String,
    val stopTitle: String
)