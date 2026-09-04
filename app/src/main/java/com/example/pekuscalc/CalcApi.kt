package com.example.pekuscalc

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

data class Calculadora(
    val id: Int? = null,
    val valorA: Double,
    val valorB: Double,
    val operacao: String,
    val resultado: Double,
    val dataCalculo: String
)

interface CalcApiService {
    @POST("api/Calculadora")
    suspend fun salvarCalculo(
        @Query("apikey") apiKey: String,
        @Body calculo: Calculadora
    ): Response<Int>

    @GET("api/Calculadora")
    suspend fun listarCalculos(
        @Query("apikey") apiKey: String
    ): Response<List<Calculadora>>

    @DELETE("api/Calculadora")
    suspend fun deletarCalculo(
        @Query("apikey") apiKey: String,
        @Query("id") id: Int
    ): Response<Unit>
}

object RetrofitClient {
    private const val BASE_URL = "https://intranet.pekus.com.br/calcapi/"

    val instance: CalcApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CalcApiService::class.java)
    }
}
