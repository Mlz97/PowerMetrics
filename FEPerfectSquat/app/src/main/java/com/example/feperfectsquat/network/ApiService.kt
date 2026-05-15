package com.example.feperfectsquat.network

import com.example.feperfectsquat.models.AnalysisSession
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Streaming

interface ApiService {
    @POST("session")
    suspend fun createSession(): AnalysisSession

    @GET("session/{id}")
    suspend fun getSession(@Path("id") id: Long): AnalysisSession

    @Multipart
    @POST("session/{id}/video")
    suspend fun uploadVideo(
        @Path("id") sessionId: Long,
        @Part file: MultipartBody.Part,
        @Part("weightKg") weightKg: RequestBody?
    ): AnalysisSession

    @Streaming
    @GET("video/download/{sessionId}")
    suspend fun downloadProcessedVideo(@Path("sessionId") sessionId: Long): ResponseBody
}
