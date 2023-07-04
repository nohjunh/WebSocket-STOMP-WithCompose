package com.example.websocketdemo.data

import com.example.websocketdemo.di.WSOkHttpClient
import com.gmail.bishoybasily.stomp.lib.Event
import com.gmail.bishoybasily.stomp.lib.StompClient
import com.google.gson.Gson
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.internal.concurrent.TaskRunner.Companion.logger
import timber.log.Timber
import java.util.logging.Level

class RealtimeBattleClientImpl(
    @WSOkHttpClient private val okHttpClient: OkHttpClient
): RealtimeBattleClient {
    private lateinit var stompConnection: Disposable
    private lateinit var topic: Disposable

    private lateinit var stomp: StompClient
    override fun getBattleStateStream(): Flow<BattleState> = callbackFlow {
        val url = "your WebSocket URL"
        val intervalMillis = 3000L

        stomp = StompClient(okHttpClient, intervalMillis).apply { this@apply.url = url }

        stompConnection = stomp.connect().subscribe {
            when (it.type) {
                Event.Type.OPENED -> {
                    /* topic 구독 수행 */
                    topic = stomp.join("your topic endpoint")
                        .subscribe { message ->
                            logger.log(Level.INFO, message)
                            Timber.tag("BattleClient").d(message)
                            val gson = Gson()
                            val gpsData = gson.fromJson(message, GpsData::class.java)

                            trySend(
                                BattleState(
                                    gpsData = gpsData
                                )
                            ).isSuccess  // Offer the converted GpsData object to the flow
                        }
                }
                Event.Type.CLOSED -> {
                    Timber.tag("BattleClient").e("CLOSED")
                    stompConnection.dispose()
                }
                Event.Type.ERROR -> {
                    Timber.tag("BattleClient").e("ERROR")
                    stompConnection.dispose()
                }
                else -> {
                    Timber.tag("BattleClient").e("ELSE")
                    disposeTopic()
                    stompConnection.dispose()
                }
            }
        }

        // Don't forget to call `awaitClose` to handle the channel closing
        awaitClose {
            stompConnection.dispose()
            disposeTopic()
        }
    }

    override suspend fun sendGPS(topic: String, gpsData: GpsData) {
        val gson = Gson()
        val jsonData = gson.toJson(gpsData)
        val result = stomp.send(topic, jsonData).subscribe { isSent ->
            if (isSent) {
                Timber.tag("Sent").d("sent to $topic: $jsonData")
            } else {
                Timber.tag("Failed").e("Failed to send to $topic: $jsonData")
            }
        }
    }

    override suspend fun close() {
        stompConnection.dispose()
    }

    private fun disposeTopic() {
        topic.dispose()
    }
}
