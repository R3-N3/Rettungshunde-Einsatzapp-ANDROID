package com.rettungshundeEinsatzApp.functions.areas

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("downloadareas")
    suspend fun downloadAreas(@Body tokenPayload: Map<String, String>): Response<DownloadAreasResponse>

    @POST("uploadarea")
    suspend fun uploadAreas(@Body request: UploadAreasRequest): Response<UploadAreasResponse>

}