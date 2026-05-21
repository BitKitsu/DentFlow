package com.example.dentflow_android.di

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.dentflow_android.BuildConfig
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
    @Named("auth_interceptor")
    fun provideAuthInterceptor(prefs: SharedPreferences): Interceptor {
        return Interceptor { chain ->
            val token = prefs.getString(TOKEN_KEY, "")
            Log.d("AUTH_DEBUG", "Token z prefs: '${token?.take(20)}...' (długość: ${token?.length})")
            val requestBuilder = chain.request().newBuilder()
            if (!token.isNullOrBlank()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }
            chain.proceed(requestBuilder.build())
        }
    }

    @Provides
    @Singleton
    @Named("auth_retrofit")
    fun provideAuthRetrofit(@Named("auth_interceptor") authInterceptor: Interceptor): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_AUTH_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("core_retrofit")
    fun provideCoreRetrofit(@Named("auth_interceptor") authInterceptor: Interceptor): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_CORE_URL)
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