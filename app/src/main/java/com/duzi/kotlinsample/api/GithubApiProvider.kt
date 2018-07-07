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

object GithubApiProvider {
    fun provideAuthApi(): AuthApi {
        return Retrofit.Builder()
                .baseUrl("https://github.com/")
                .client(provideOkHttpClient(provideLoggingInterceptor(), null))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AuthApi::class.java)
    }

    fun provideGithubApi(context: Context): GithubApi {
        return Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .client(provideOkHttpClient(provideLoggingInterceptor(),
                        provideAuthInterceptor(provideAuthTokenProvider(context))))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GithubApi::class.java)
    }

    private fun provideOkHttpClient(interceptor: HttpLoggingInterceptor,
                            authInterceptor: AuthInterceptor?): OkHttpClient {
        val b: OkHttpClient.Builder = OkHttpClient.Builder()
        if(authInterceptor != null) {
            b.addInterceptor(authInterceptor)
        }
        b.addInterceptor(interceptor)
        return b.build()
    }

    private fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return interceptor
    }

    private fun provideAuthInterceptor(provider: AuthTokenProvider): AuthInterceptor {
        provider.getToken().let {
            return AuthInterceptor(it!!)
        }
    }

    private fun provideAuthTokenProvider(context: Context): AuthTokenProvider {
        return AuthTokenProvider(context.applicationContext)
    }

    class AuthInterceptor(token: String) : Interceptor {
        private var token: String = token

        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()
            return chain.proceed(original.newBuilder().addHeader("Authorization", "token " + token).build())
        }

    }

}