package com.raulvieira.nextstoptoronto.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.raulvieira.nextstoptoronto.models.FavoritesModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow


@Dao
interface RoomDao {

    @Insert
    suspend fun addToFavorites(favoriteItem: FavoritesModel)

    @Query("DELETE FROM favorites_table WHERE stopTag = :stopTag AND routeTag = :routeTag ")
    suspend fun removeFromFavorites(stopTag: String, routeTag: String)

    @Query("SELECT * FROM favorites_table WHERE stopTag = :stopTag AND routeTag = :routeTag AND stopTitle = :stopTitle")
    fun getItemFromFavorites(stopTag: String, routeTag: String, stopTitle: String): Flow<FavoritesModel?>

    @Query("SELECT EXISTS(SELECT * FROM favorites_table WHERE stopTag = :stopTag AND routeTag = :routeTag AND stopTitle = :stopTitle)")
    fun isOnCartDatabase(stopTag: String, routeTag: String, stopTitle: String) : Flow<Boolean>

    @Query("SELECT EXISTS(SELECT * FROM favorites_table WHERE stopTag = :stopTag AND routeTag = :routeTag AND stopTitle = :stopTitle)")
    fun isOnCartDatabase2(stopTag: String, routeTag: String, stopTitle: String) : Flow<Boolean>

    @Query("SELECT * FROM favorites_table")
    fun getFavorites() : Flow<List<FavoritesModel>>

}