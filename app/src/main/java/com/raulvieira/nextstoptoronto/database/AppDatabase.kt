package com.raulvieira.nextstoptoronto.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.raulvieira.nextstoptoronto.models.FavoritesModel
import com.raulvieira.nextstoptoronto.models.StopModel

@Database(entities = [FavoritesModel::class, StopModel::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun roomDao(): RoomDao
}