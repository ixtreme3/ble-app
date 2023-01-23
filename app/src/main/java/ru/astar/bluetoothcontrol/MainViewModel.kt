package ru.astar.bluetoothcontrol

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.observer.ConnectionObserver

/* Содержит всю логику, которая не относится к отображения интерфейса.
 Важно: ViewModel ничего не знает о активити или фрагменте (о View)
 Важно: ViewModel должна быть без контекста
 Важно: ViewModel не должна ничего возвращать в активити или фрагмент - для есть LiveData
 LiveData - переменная, содержащая в себе какие-то данные. На эти данные можно подписаться и при
 их изменении получать уведомление. См. метод subscribeOnViewModel() во фрагментах.
 */
class ControlViewModel(private val adapterProvider: BluetoothAdapterProvider) : ViewModel() {

    private val controlManager: BLEControlManager = BLEControlManager(adapterProvider.getContext())
    private lateinit var device: BluetoothDevice;

    /* Переменная для хранения последних трех значений высоты прыжка */

    /* Функция для обработки  */
    fun readData() {
        val data = controlManager.readDataFromServer()
//        println(data.)
    }

    fun connect(deviceAddress: String) {
        device = adapterProvider.getAdapter().getRemoteDevice(deviceAddress)
        controlManager.connect(device)
            .retry(2, 100)
            .useAutoConnect(false)
            .done {
                Log.e("ControlViewModel", "Connection success")
            }
            .fail { _, status ->
                Log.e("ControlViewModel", "Connection failed, $status")
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