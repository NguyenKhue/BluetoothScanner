package com.khue.blescanner.data.ble

import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.annotation.RequiresPermission
import com.khue.blescanner.domain.ble.BleController
import com.khue.blescanner.domain.ble.BluetoothDeviceDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

const val PERMISSION_BLUETOOTH_SCAN = "android.permission.BLUETOOTH_SCAN"
const val PERMISSION_BLUETOOTH_CONNECT = "android.permission.BLUETOOTH_CONNECT"

class AndroidBleController(context: Context): BleController {
    private val bluetooth = context.getSystemService(Context.BLUETOOTH_SERVICE)
            as? BluetoothManager
        ?: throw Exception("Bluetooth is not supported by this device")

    private val scanner: BluetoothLeScanner
        get() = bluetooth.adapter.bluetoothLeScanner

    private val _isScanning = MutableStateFlow(false)
    override val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _foundDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val foundDevices: StateFlow<List<BluetoothDeviceDomain>> = _foundDevices.asStateFlow()

    private val scanCallback = object : ScanCallback() {
        @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result ?: return
            val bleDomainDevice = result.device.toBluetoothDeviceDomain()
            if (!foundDevices.value.contains(bleDomainDevice) && !result.device.name.isNullOrEmpty()) {
                _foundDevices.update { it + bleDomainDevice }
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            _isScanning.update { false }
        }
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_SCAN)
    override fun startScanning() {
        _foundDevices.update { emptyList() }
        scanner.startScan(scanCallback)
        _isScanning.update { true }
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_SCAN)
    override fun stopScanning() {
        scanner.stopScan(scanCallback)
        _isScanning.update { false }
    }
}