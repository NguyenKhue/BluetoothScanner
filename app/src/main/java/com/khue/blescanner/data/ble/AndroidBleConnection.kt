package com.khue.blescanner.data.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import com.khue.blescanner.domain.ble.BleConnection
import com.khue.blescanner.domain.ble.CharacteristicRead
import com.khue.blescanner.domain.ble.CharacteristicWrite
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class AndroidBleConnection(
    private val context: Context
): BleConnection {
    private val bluetooth = context.getSystemService(Context.BLUETOOTH_SERVICE)
            as? BluetoothManager
        ?: throw Exception("Bluetooth is not supported by this device")

    private val adapter: BluetoothAdapter
        get() = bluetooth.adapter

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _activeDeviceServices = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    override val activeDeviceServices: StateFlow<Map<String, List<String>>> = _activeDeviceServices.asStateFlow()

    private val _characteristicRead = MutableStateFlow(CharacteristicRead())
    override val characteristicRead: StateFlow<CharacteristicRead> = _characteristicRead.asStateFlow()

    private val _characteristicWrite = MutableStateFlow(CharacteristicWrite())
    override val characteristicWrite: StateFlow<CharacteristicWrite> = _characteristicWrite.asStateFlow()

    private var gatt: BluetoothGatt? = null

    private val callback = object: BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.d("BLEClient", "BluetoothGattCallback:  status: $status, newState: $newState")
            super.onConnectionStateChange(gatt, status, newState)
            val connected = newState == BluetoothGatt.STATE_CONNECTED
            if (connected) {
                _activeDeviceServices.update { gatt.services.associate {  service -> Pair(service.uuid.toString(), service.characteristics.map { it.uuid.toString() }) } }
            }
            _isConnected.update { connected }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            _activeDeviceServices.update { gatt.services.associate {  service -> Pair(service.uuid.toString(), service.characteristics.map { it.uuid.toString() }) } }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray, status: Int) {
            super.onCharacteristicRead(gatt, characteristic, value, status)
            _characteristicRead.update { CharacteristicRead(characteristic.uuid.toString(), value, status) }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            _characteristicWrite.update { CharacteristicWrite(characteristic.uuid.toString(), status) }
        }
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    override fun connect(deviceAddress: String) {
        gatt = adapter.getRemoteDevice(deviceAddress).connectGatt(context, false, callback)
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    override fun disconnect() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    override fun discoverServices() {
        gatt?.discoverServices()
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    override fun readCharacteristic(serviceUuid: String, characteristicUuid: String) {
        val service = gatt?.getService(UUID.fromString(serviceUuid))
        val characteristic = service?.getCharacteristic(UUID.fromString(characteristicUuid))
        if (characteristic != null) {
            val success = gatt?.readCharacteristic(characteristic)
            Log.v("BLEClient", "Read status: $success")
        }
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    @Suppress("DEPRECATION")
    override fun writeCharacteristic(serviceUuid: String, characteristicUuid: String, data: ByteArray) {
        val service = gatt?.getService(UUID.fromString(serviceUuid))
        val characteristic = service?.getCharacteristic(UUID.fromString(characteristicUuid))
        if (characteristic != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt?.writeCharacteristic(characteristic, data, WRITE_TYPE_DEFAULT)?.also {
                    Log.v("BLEClient", "Write status: $it")
                }
            } else {
                characteristic.value = data
                val success = gatt?.writeCharacteristic(characteristic)
                Log.v("BLEClient", "Write status: $success")
            }
        }
    }
}