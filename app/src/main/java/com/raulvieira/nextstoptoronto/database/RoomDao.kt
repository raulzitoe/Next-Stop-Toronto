package com.raulvieira.nextstoptoronto.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.raulvieira.nextstoptoronto.models.FavoritesModel
import kotlinx.coroutines.flow.Flow


@Dao
interface RoomDao {

    @Insert
    suspend fun addToFavorites(favoriteItem: FavoritesModel)

    @Delete
    suspend fun removeFromFavorites(favoriteItem: FavoritesModel)

    @Query("SELECT * FROM favorites_table WHERE stopTag = :stopTag AND routeTag = :routeTag")
    fun getItemFromFavorites(stopTag: String, routeTag: String): Flow<FavoritesModel>

}