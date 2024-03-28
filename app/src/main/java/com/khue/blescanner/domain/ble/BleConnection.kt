package com.khue.blescanner.domain.ble

import kotlinx.coroutines.flow.StateFlow

interface BleConnection {
    val isConnected: StateFlow<Boolean>
    val activeDeviceServices: StateFlow<Map<String, List<String>>>
    val characteristicRead: StateFlow<CharacteristicRead>

    fun connect(deviceAddress: String)

    fun disconnect()

    fun discoverServices()

    fun readCharacteristic(serviceUuid: String, characteristicUuid: String)

    fun writeCharacteristic(serviceUuid: String, characteristicUuid: String, data: ByteArray)
}