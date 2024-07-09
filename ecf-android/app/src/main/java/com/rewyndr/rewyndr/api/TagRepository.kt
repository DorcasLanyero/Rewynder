package com.rewyndr.rewyndr.api

import com.rewyndr.rewyndr.model.Tag
import retrofit2.Response

class TagRepository {

    private val tagService = RetrofitBuilder.buildService(TagService::class.java)

    suspend fun createTag(tag : Tag) : Result<Tag?> {
        return try {
            Result.success(data = tagService.create(tag))
        } catch (exception: Exception) {
            Result.error(data = null, message = exception.message ?: "Error Occurred!")
        }
    }

    suspend fun updateTag(tag : Tag) : Result<Tag?> {
        return try {
            Result.success(data = tagService.update(tag.id, tag))
        } catch (exception: Exception) {
            Result.error(data = null, message = exception.message ?: "Error Occurred!")
        }
    }

    suspend fun deleteTag(id: Int) : Result<Response<Void>> {
        return try {
            Result.success(data = tagService.delete(id))
        } catch (exception: Exception) {
            Result.error(data = null, message = exception.message ?: "Error Occurred!")
        }
    }
}