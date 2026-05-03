package com.example.feperfectsquat.network

import com.example.feperfectsquat.models.AnalysisSession
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    @GET("session/{id}")
    suspend fun getSession(@Path("id") id: Long): AnalysisSession

    @Multipart
    @POST("session/{id}/video") // TODO: Reemplazar con endpoint real
    suspend fun uploadVideo(
        @Path("id") sessionId: Long,
        @Part file: MultipartBody.Part
    ): AnalysisSession
}
