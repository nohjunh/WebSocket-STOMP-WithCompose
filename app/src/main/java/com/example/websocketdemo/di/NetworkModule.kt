package com.example.websocketdemo.di

import com.example.websocketdemo.data.RealtimeBattleClient
import com.example.websocketdemo.data.RealtimeBattleClientImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @WSOkHttpClient
    @Singleton
    @Provides
    fun provideWSOkHttpClient(
        //authInterceptor: AuthInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.MINUTES)
        .writeTimeout(10, TimeUnit.MINUTES)
        .addInterceptor {
            it.proceed(
                it.request().newBuilder()
                    .header(
                        "Authorization",
                        "your -- Jwt -- Token"
                    )
                    .build()
            )
        }
        .build()

    @Singleton
    @Provides
    fun provideRealtimeBattleClient(@WSOkHttpClient httpClient: OkHttpClient): RealtimeBattleClient {
        return RealtimeBattleClientImpl(httpClient)
    }

}
