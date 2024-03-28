package com.khue.blescanner.di

import android.content.Context
import com.khue.blescanner.data.ble.AndroidBleConnection
import com.khue.blescanner.data.ble.AndroidBleController
import com.khue.blescanner.domain.ble.BleConnection
import com.khue.blescanner.domain.ble.BleController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BleModule {

    @Provides
    @Singleton
    fun provideBleController(@ApplicationContext context: Context): BleController {
        return AndroidBleController(context)
    }

    @Provides
    @Singleton
    fun provideBleConnection(@ApplicationContext context: Context): BleConnection {
        return AndroidBleConnection(context)
    }
}