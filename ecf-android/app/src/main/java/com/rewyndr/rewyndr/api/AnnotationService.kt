package com.rewyndr.rewyndr.api

import com.rewyndr.rewyndr.model.Annotation
import retrofit2.Response
import retrofit2.http.*

interface AnnotationService {
    @GET("annotations/{id}")
    suspend fun get(@Path("id") id: Int) : Annotation

    @GET("annotations")
    suspend fun list() : List<Annotation>

    @POST("annotations")
    suspend fun create(@Body annotation: Annotation) : Annotation

    @PATCH("annotations/{id}")
    suspend fun update(@Path("id") id: Int, @Body annotation: Annotation) : Annotation

    @DELETE("annotations/{id}")
    suspend fun delete(@Path("id") id: Int) : Response<Void>
}