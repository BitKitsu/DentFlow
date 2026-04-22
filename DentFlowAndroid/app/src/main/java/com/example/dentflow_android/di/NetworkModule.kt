package com.example.dentflow_android.di

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.dentflow_android.data.remote.ApiService
import com.example.dentflow_android.data.remote.AuthService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val PREFS_NAME = "dentflow_prefs"
    private const val TOKEN_KEY = "jwt_token"

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(prefs: SharedPreferences): Interceptor {
        return Interceptor { chain ->
            val original = chain.request()

            // Pobieramy token i sprawdzamy co tam siedzi
            val token = prefs.getString(TOKEN_KEY, "")

            // LOGOWANIE DLA CIEBIE (Sprawdź to w Logcat!)
            if (token.isNullOrBlank()) {
                Log.e("DENTFLOW_AUTH", "ALARM: Interceptor nie znalazł tokenu w SharedPreferences!")
            } else {
                Log.d("DENTFLOW_AUTH", "Interceptor dodał token: Bearer ${token.take(10)}...")
            }

            val requestBuilder = original.newBuilder()
            if (!token.isNullOrBlank()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }

            chain.proceed(requestBuilder.build())
        }
    }

    @Provides
    @Singleton
    @Named("auth_retrofit")
    fun provideAuthRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8081/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    @Named("core_retrofit")
    fun provideCoreRetrofit(authInterceptor: Interceptor): Retrofit {
        val logging = HttpLoggingInterceptor { message ->
            Log.d("OKHTTP_CORE", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthService(@Named("auth_retrofit") retrofit: Retrofit): AuthService =
        retrofit.create(AuthService::class.java)

    @Provides
    @Singleton
    fun provideApiService(@Named("core_retrofit") retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)
}
