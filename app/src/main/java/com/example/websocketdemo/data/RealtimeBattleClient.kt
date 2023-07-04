package com.example.websocketdemo.data

import kotlinx.coroutines.flow.Flow

interface RealtimeBattleClient {
    fun getBattleStateStream(): Flow<BattleState>
    suspend fun sendGPS(topic: String, gpsData: GpsData)
    suspend fun close()
}
