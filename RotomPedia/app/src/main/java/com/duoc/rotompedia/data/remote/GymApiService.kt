package com.duoc.rotompedia.data.remote

import com.duoc.rotompedia.data.model.GymLeader
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

// OJO: Usamos 10.0.2.2 para que el emulador vea tu PC localhost
private const val BASE_URL_MICROSERVICE = "http://10.0.2.2:8080/"

interface GymService {
    // Obtener líderes por región
    @GET("api/leaders/region/{regionName}")
    suspend fun getLeadersByRegion(@Path("regionName") regionName: String): List<GymLeader>
}

object GymApi {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl(BASE_URL_MICROSERVICE)
        .build()

    val service: GymService by lazy {
        retrofit.create(GymService::class.java)
    }
}