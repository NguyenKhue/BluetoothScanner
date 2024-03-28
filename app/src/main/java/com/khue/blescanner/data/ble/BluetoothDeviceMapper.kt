package com.khue.blescanner.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.khue.blescanner.domain.ble.BluetoothDeviceDomain

@SuppressLint("MissingPermission")
fun BluetoothDevice.toBluetoothDeviceDomain(): BluetoothDeviceDomain {
    return BluetoothDeviceDomain(
        name = name,
        address = address
    )
}