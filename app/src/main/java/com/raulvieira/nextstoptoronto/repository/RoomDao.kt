package com.raulvieira.nextstoptoronto.repository

import androidx.room.*
import com.raulvieira.nextstoptoronto.models.DateDatabaseModel
import com.raulvieira.nextstoptoronto.models.FavoritesModel
import com.raulvieira.nextstoptoronto.models.StopModel
import kotlinx.coroutines.flow.Flow
import java.util.*


@Dao
interface RoomDao {

    @Insert
    suspend fun addToFavorites(favoriteItem: FavoritesModel)

    @Query("DELETE FROM favorites_table WHERE stopTag = :stopTag AND routeTag = :routeTag ")
    suspend fun removeFromFavorites(stopTag: String, routeTag: String)

    @Query("SELECT * FROM favorites_table WHERE stopTag = :stopTag AND routeTag = :routeTag AND stopTitle = :stopTitle")
    fun getItemFromFavorites(
        stopTag: String,
        routeTag: String,
        stopTitle: String
    ): Flow<FavoritesModel?>

    @Query("SELECT EXISTS(SELECT * FROM favorites_table WHERE stopTag = :stopTag AND routeTag = :routeTag AND stopTitle = :stopTitle)")
    fun isRouteFavorited(stopTag: String, routeTag: String, stopTitle: String): Flow<Boolean>

    @Query("SELECT * FROM favorites_table")
    fun getFavorites(): Flow<List<FavoritesModel>>

    @Query("SELECT NOT EXISTS(SELECT * FROM favorites_table)")
    fun isFavoritesEmpty():Flow<Boolean>

    @Upsert
    suspend fun insertStopToDatabase(stop: StopModel)

    @Query("SELECT * FROM stops_table")
    fun getStopsFromDatabase(): Flow<List<StopModel>>

    @Upsert
    suspend fun setLastUpdatedDate(dateModel: DateDatabaseModel)

    @Query("SELECT * FROM last_updated_table")
    suspend fun getLastUpdatedDate(): DateDatabaseModel?

}