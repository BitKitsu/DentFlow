package pl.edu.ur.dentflow.di

import android.content.Context
import android.content.SharedPreferences
import pl.edu.ur.dentflow.BuildConfig
import pl.edu.ur.dentflow.data.remote.ApiService
import pl.edu.ur.dentflow.data.remote.AuthService
import pl.edu.ur.dentflow.data.remote.FileApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    const val PREFS_NAME = "dentflow_prefs"
    const val AUTH_URL_KEY = "server_auth_url"
    const val CORE_URL_KEY = "server_core_url"
    const val LANGUAGE_KEY = "app_language"
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
            val requestBuilder = chain.request().newBuilder()
            if (!token.isNullOrBlank()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }
            chain.proceed(requestBuilder.build())
        }
    }

    @Provides
    @Singleton
    @Named("url_rewrite_interceptor")
    fun provideUrlRewriteInterceptor(prefs: SharedPreferences): Interceptor {
        return Interceptor { chain ->
            val original = chain.request()
            val savedUrl = prefs.getString(
                if (original.url.toString().contains("/auth/")) AUTH_URL_KEY else CORE_URL_KEY,
                null
            )
            if (!savedUrl.isNullOrBlank()) {
                try {
                    val normalized = savedUrl.trim().let { url ->
                        if (!url.startsWith("http://") && !url.startsWith("https://")) {
                            "http://$url"
                        } else url
                    }
                    val newBaseUrl = normalized.trimEnd('/').plus("/").toHttpUrl()
                    val newUrl = original.url.newBuilder()
                        .scheme(newBaseUrl.scheme)
                        .host(newBaseUrl.host)
                        .port(newBaseUrl.port)
                        .build()
                    chain.proceed(original.newBuilder().url(newUrl).build())
                } catch (_: Exception) {
                    chain.proceed(original)
                }
            } else {
                chain.proceed(original)
            }
        }
    }

    @Provides
    @Singleton
    @Named("auth_retrofit")
    fun provideAuthRetrofit(
        @Named("auth_interceptor") authInterceptor: Interceptor,
        @Named("url_rewrite_interceptor") urlRewriteInterceptor: Interceptor
    ): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(urlRewriteInterceptor)
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
    fun provideCoreRetrofit(
        @Named("auth_interceptor") authInterceptor: Interceptor,
        @Named("url_rewrite_interceptor") urlRewriteInterceptor: Interceptor
    ): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(urlRewriteInterceptor)
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

    @Provides
    @Singleton
    fun provideFileApiService(@Named("core_retrofit") retrofit: Retrofit): FileApiService =
        retrofit.create(FileApiService::class.java)

}