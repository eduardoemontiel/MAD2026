package com.example.appmigueleduardo.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("find") // El endpoint para buscar por coordenadas [cite: 576]
    fun getWeatherForecast(
        @Query("lat") lat: Double, // Latitud [cite: 578]
        @Query("lon") lon: Double, // Longitud [cite: 578]
        @Query("cnt") count: Int = 1, // Número de resultados [cite: 578]
        @Query("APPID") apiKey: String // Tu clave de la API [cite: 578]
    ): Call<WeatherResponse>
}