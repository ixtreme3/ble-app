package ru.astar.bluetoothcontrol

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import no.nordicsemi.android.ble.observer.ConnectionObserver

class ControlViewModel(private val adapterProvider: BluetoothAdapterProvider) : ViewModel() {

    private val controlManager: BleControlManager = BleControlManager(adapterProvider.getContext())

    fun connect(deviceAddress: String) {
        val device = adapterProvider.getAdapter().getRemoteDevice(deviceAddress)
        controlManager.connect(device)
            .retry(2, 100)
            .useAutoConnect(false)
            .done {
                Log.e("ControlViewModel", "connection success!")
            }
            .fail { _, status ->
                Log.e("ControlViewModel", "connection failed, $status")
            }
            .enqueue()
        controlManager.setConnectionObserver(connectionObserver)
    }

    fun disconnect() {
        controlManager.disconnect().enqueue()
    }

    private val connectionObserver = object : ConnectionObserver {
        override fun onDeviceConnecting(device: BluetoothDevice) {}

        override fun onDeviceConnected(device: BluetoothDevice) {}

        override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {}

        override fun onDeviceReady(device: BluetoothDevice) {
            Log.e("ControlViewModel", "onDeviceReady() device is ready!")
        }

        override fun onDeviceDisconnecting(device: BluetoothDevice) {}

        override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {}
    }
}

/* Создание ViewModel. Используется в DeviceFragment.
Классический способ создания ViewModel через конструктор нам не подходит т.к. мы должны
получать ViewModel так: MyViewModel myViewModel = ViewModelProvider.of(this).get(MyViewModel.class)
Таким образом, чтобы передать аргумент нужно использовать фабрику.
 */
class ControlViewModelFactory(private val adapterProvider: BluetoothAdapterProvider)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ControlViewModel::class.java)) {
            return ControlViewModel(adapterProvider) as T
        }
        throw IllegalArgumentException("ViewModel not found")
    }
}