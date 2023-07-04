package com.example.websocketdemo.data

import kotlinx.serialization.Serializable

@Serializable
data class BattleState(
    val message: String = "서버로부터 받은 BattleState",
    val gpsData: GpsData = GpsData()
)
