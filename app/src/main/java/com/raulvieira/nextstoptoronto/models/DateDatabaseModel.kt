package com.raulvieira.nextstoptoronto.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date


@Entity(tableName = "last_updated_table")
data class DateDatabaseModel(
    @PrimaryKey val id: Int = 1,
    val lastUpdated: Date?
)
