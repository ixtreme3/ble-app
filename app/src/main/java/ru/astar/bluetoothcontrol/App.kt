package ru.astar.bluetoothcontrol

import android.app.Application

/* Для глобального хранения adapterProvider */
class App: Application() {
    val adapterProvider: BluetoothAdapterProvider by lazy {
        BluetoothAdapterProvider.Base(applicationContext)
    }
}