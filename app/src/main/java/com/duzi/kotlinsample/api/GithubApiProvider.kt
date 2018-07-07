package com.duzi.kotlinsample.api

import android.content.Context
import com.duzi.kotlinsample.api.model.AuthTokenProvider
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by KIM on 2018-07-06.
 */

fun provideAuthApi(): AuthApi =
        Retrofit.Builder()
                .baseUrl("https://github.com/")
                .client(provideOkHttpClient(provideLoggingInterceptor(), null))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AuthApi::class.java)

fun provideGithubApi(context: Context): GithubApi =
        Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .client(provideOkHttpClient(provideLoggingInterceptor(),
                        provideAuthInterceptor(provideAuthTokenProvider(context))))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GithubApi::class.java)

private fun provideOkHttpClient(interceptor: HttpLoggingInterceptor,
                                authInterceptor: AuthInterceptor?): OkHttpClient =
        OkHttpClient.Builder().run {
            if(authInterceptor != null) {
                addInterceptor(authInterceptor)
            }
            addInterceptor(interceptor)
            build()
        }

private fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

private fun provideAuthInterceptor(provider: AuthTokenProvider): AuthInterceptor {
    provider.token.let {
        return AuthInterceptor(it!!)
    }
}

private fun provideAuthTokenProvider(context: Context): AuthTokenProvider =
    AuthTokenProvider(context.applicationContext)

internal class AuthInterceptor(private val token: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response =
            with(chain) {
                val newRequest = request().newBuilder().run {
                    addHeader("Authorization", "token " + token)
                    build()
                }
                proceed(newRequest)
            }
}

