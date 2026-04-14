package com.example.appmigueleduardo.network

data class WeatherResponse(
    val list: List<WeatherItem> // La API devuelve una lista de resultados [cite: 586]
)

data class WeatherItem(
    val name: String, // Nombre de la ciudad [cite: 589]
    val main: WeatherMain, // Datos de temperatura [cite: 590]
    val weather: List<WeatherCondition> // Lista con descripción e icono [cite: 591]
)

data class WeatherMain(
    val temp: Double, // Temperatura en Kelvin [cite: 594]
    val humidity: Int // Humedad [cite: 599]
)

data class WeatherCondition(
    val description: String, // Ej: "cielo despejado" [cite: 602]
    val icon: String // Código del icono para Glide [cite: 603]
)