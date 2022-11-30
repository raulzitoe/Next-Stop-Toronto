package com.raulvieira.nextstoptoronto

import android.content.Context
import androidx.room.Room
import com.google.gson.GsonBuilder
import com.raulvieira.nextstoptoronto.database.AppDatabase
import com.raulvieira.nextstoptoronto.models.PredictionModel
import com.raulvieira.nextstoptoronto.models.RoutePredictionsModel
import com.raulvieira.nextstoptoronto.models.StopPredictionModel
import com.raulvieira.nextstoptoronto.utils.PredictionModelDeserializer
import com.raulvieira.nextstoptoronto.utils.RoutePredictionsDeserializer
import com.raulvieira.nextstoptoronto.utils.StopPredictionDeserializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    private const val BASE_URL = "https://retro.umoiq.com/service/"

    @Singleton
    @Provides
    fun providesHttpLoggingInterceptor() = HttpLoggingInterceptor()
        .apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Singleton
    @Provides
    fun providesOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(httpLoggingInterceptor)
            .build()

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val gson = GsonBuilder()
            .registerTypeAdapter(RoutePredictionsModel::class.java, RoutePredictionsDeserializer())
            .registerTypeAdapter(PredictionModel::class.java, PredictionModelDeserializer())
            .registerTypeAdapter(StopPredictionModel::class.java, StopPredictionDeserializer())
            .create()
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): RetrofitInterface =
        retrofit.create(RetrofitInterface::class.java)

    @Singleton
    @Provides
    fun providesRepository(apiService: RetrofitInterface, db: AppDatabase) = Repository(apiService, db.roomDao())

    @Singleton
    @Provides
    fun provideYourDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        AppDatabase::class.java,
        "app_database"
    ).build()

    @Singleton
    @Provides
    fun provideAppDao(db: AppDatabase) = db.roomDao()
}