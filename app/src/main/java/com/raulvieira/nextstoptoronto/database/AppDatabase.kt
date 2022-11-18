package com.raulvieira.nextstoptoronto.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [], version = 0)
abstract class AppDatabase : RoomDatabase() {
    abstract fun roomDao(): RoomDao
}