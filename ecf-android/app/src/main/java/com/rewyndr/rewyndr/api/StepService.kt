package com.rewyndr.rewyndr.api

import com.rewyndr.rewyndr.model.Step
import retrofit2.Response
import retrofit2.http.*

interface StepService {
    @GET("steps/{id}")
    suspend fun get(@Path("id") id: Int) : Step

    @POST("steps")
    suspend fun create(@Body step: Step) : Step

    //This call might not actually return a Step
    @POST("steps/{id}/executions")
    suspend fun saveExecutionResult(@Path("id") id: Int, @Body step: Step) : Step

    @PATCH("steps/{id}")
    suspend fun update(@Path("id") id: Int, @Body step: Step) : Step

    @DELETE("steps/{id}")
    suspend fun delete(@Path("id") id: Int) : Response<Void>
}