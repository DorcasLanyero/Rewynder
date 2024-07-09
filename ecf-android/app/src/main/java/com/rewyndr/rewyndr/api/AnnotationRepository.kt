package com.rewyndr.rewyndr.api

import com.rewyndr.rewyndr.model.Annotation

class AnnotationRepository {
    private val annotationService = RetrofitBuilder.buildService(AnnotationService::class.java)

    suspend fun createAnnotation(annotation : Annotation) : Result<Annotation?> {
        return try {
            Result.success(data = annotationService.create(annotation))
        } catch (exception: Exception) {
            Result.error(data = null, message = exception.message ?: "Error Occurred!")
        }
    }

    suspend fun getAnnotation(annotationId : Int) : Result<Annotation?> {
        return try {
            Result.success(data = annotationService.get(annotationId))
        } catch (exception: Exception) {
            Result.error(data = null, message = exception.message ?: "Error Occurred!")
        }
    }
}