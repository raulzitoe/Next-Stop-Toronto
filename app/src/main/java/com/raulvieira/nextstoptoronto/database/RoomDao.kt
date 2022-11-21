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

    @Query("DELETE FROM favorites_table WHERE stopTag = :stopTag AND routeTag = :routeTag ")
    suspend fun removeFromFavorites(stopTag: String, routeTag: String)

    @Query("SELECT * FROM favorites_table WHERE stopTag = :stopTag AND routeTag = :routeTag")
    fun getItemFromFavorites(stopTag: String, routeTag: String): Flow<FavoritesModel?>

    @Query("SELECT EXISTS(SELECT * FROM favorites_table WHERE stopTag = :stopTag AND routeTag = :routeTag)")
    fun isOnCartDatabase(stopTag: String, routeTag: String) : Flow<Boolean>

}