package com.khue.blescanner.domain.ble

import kotlinx.coroutines.flow.StateFlow

interface BleController {
    val isScanning: StateFlow<Boolean>
    val foundDevices: StateFlow<List<BluetoothDevice>>

    fun startScanning()

    fun stopScanning()
}