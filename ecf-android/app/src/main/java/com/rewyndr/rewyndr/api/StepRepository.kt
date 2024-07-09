package com.rewyndr.rewyndr.api

import com.rewyndr.rewyndr.model.Step
import retrofit2.Response

class StepRepository {
    private val stepService = RetrofitBuilder.buildService(StepService::class.java)

    suspend fun createStep(step : Step) : Result<Step?> {
        return try {
            Result.success(data = stepService.create(step))
        } catch (exception: Exception) {
            Result.error(data = null, message = exception.message ?: "Error Occurred!")
        }
    }

    suspend fun getStep(stepId : Int) : Result<Step?> {
        return try {
            Result.success(data = stepService.get(stepId))
        } catch (exception: Exception) {
            Result.error(data = null, message = exception.message ?: "Error Occurred!")
        }
    }

    suspend fun deleteStep(stepId : Int) : Result<Response<Void>> {
        return try {
            Result.success(data = stepService.delete(stepId))
        } catch (exception: Exception) {
            Result.error(data = null, message = exception.message ?: "Error Occurred!")
        }
    }
}