package com.example.websocketdemo.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.websocketdemo.data.BattleState
import com.example.websocketdemo.data.GpsData
import com.example.websocketdemo.data.RealtimeBattleClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.ConnectException
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val client: RealtimeBattleClient
): ViewModel() {
    val state = client
        .getBattleStateStream()
        .onStart { _isConnecting.value = true }
        .onEach { _isConnecting.value = false }
        .catch { t -> _showConnectionError.value = t is ConnectException }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(3000), BattleState())

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting = _isConnecting.asStateFlow()

    private val _showConnectionError = MutableStateFlow(false)

    fun sendGPS(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            client.sendGPS("your pub EndPoint", GpsData(latitude, longitude))
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            client.close()
        }
    }
}
