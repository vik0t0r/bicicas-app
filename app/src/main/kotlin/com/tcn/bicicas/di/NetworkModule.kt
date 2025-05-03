package com.tcn.bicicas.di

import android.content.Context
import com.tcn.bicicas.BuildConfig
import com.tcn.bicicas.data.datasource.remote.CacheInterceptorRewriteRequest
import com.tcn.bicicas.data.datasource.remote.CacheInterceptorRewriteResponse
import com.tcn.bicicas.data.datasource.remote.TestAuthInterceptor
import com.tcn.bicicas.data.datasource.remote.serialization.ApiConverter
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import java.sql.Time
import java.util.concurrent.TimeUnit

val networkModule = module {
    single { provideRetrofitBuilder(getOrNull()) }
}

private fun provideRetrofitBuilder(context: Context?) = Retrofit.Builder()
    .client(
        OkHttpClient.Builder()
            .callTimeout(120, TimeUnit.SECONDS)
            .connectTimeout(20 , TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30,TimeUnit.SECONDS)
            .cache(context?.cacheDir?.let { file -> Cache(file, 1024 * 1024) })
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = when {
                    BuildConfig.DEBUG -> HttpLoggingInterceptor.Level.BODY
                    else -> HttpLoggingInterceptor.Level.NONE
                }
            })
            .addNetworkInterceptor(CacheInterceptorRewriteResponse())
            .addInterceptor(CacheInterceptorRewriteRequest())
            .addInterceptor(TestAuthInterceptor())
            .build()
    )
    .addConverterFactory(ApiConverter())