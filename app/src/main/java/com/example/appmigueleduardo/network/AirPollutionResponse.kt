package com.example.appmigueleduardo.network

import com.google.gson.annotations.SerializedName

data class AirPollutionResponse(
    val list: List<AirPollutionItem>
)

data class AirPollutionItem(
    val main: AirMain,
    val components: AirComponents
)

data class AirMain(
    val aqi: Int
)

data class AirComponents(
    @SerializedName("pm2_5") val pm2_5: Double,
    @SerializedName("pm10")  val pm10: Double = 0.0,
    @SerializedName("co")    val co: Double = 0.0,
    @SerializedName("no2")   val no2: Double = 0.0,
    @SerializedName("o3")    val o3: Double = 0.0
)