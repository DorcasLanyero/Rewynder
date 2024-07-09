package com.rewyndr.rewyndr.api

import com.rewyndr.rewyndr.model.Tag
import retrofit2.Response
import retrofit2.http.*

interface TagService {
    @GET("tags/{id}")
    suspend fun get(@Path("id") id: Int) : Tag

    @POST("tags")
    suspend fun create(@Body step: Tag) : Tag

    @PATCH("tags/{id}")
    suspend fun update(@Path("id") id: Int, @Body step: Tag) : Tag

    @DELETE("tags/{id}")
    suspend fun delete(@Path("id") id: Int) : Response<Void>
}