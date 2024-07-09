package com.rewyndr.rewyndr.api

import com.rewyndr.rewyndr.BuildConfig
import com.rewyndr.rewyndr.RewyndrApplication
import com.rewyndr.rewyndr.api.resource.TokenStore
import com.rewyndr.rewyndr.serialization.PointJsonAdapter
import com.squareup.moshi.Moshi
import cz.msebera.android.httpclient.HttpResponseInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitBuilder {
    private val BASE_API_URL = if (BuildConfig.DEBUG) BuildConfig.API_BASE_URL_ST else BuildConfig.API_BASE_URL_PR

    private val moshi = Moshi.Builder().add(PointJsonAdapter()).build()

    private val client =
        OkHttpClient.Builder()
        .addInterceptor(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain) : Response {
                val tokenStore = TokenStore(RewyndrApplication.getInstance())
                val request = chain.request()

                if(!tokenStore.hasAuthenticationToken())
                    return chain.proceed(request)

                return chain.proceed(request.newBuilder()
                        .addHeader("Authorization", "Bearer " + tokenStore.authenticationToken)
                        .build())
            }
        }).addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
        }).build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_API_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
        .client(client)
        .build()

    @JvmStatic
    fun <T> buildService(service: Class<T>): T{
        return retrofit.create(service)
    }
}