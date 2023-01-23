package ru.astar.bluetoothcontrol

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.*
import android.os.ParcelUuid
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

/* Содержит всю логику, которая не относится к отображения интерфейса.
 Важно: ViewModel ничего не знает о активити или фрагменте (о View)
 Важно: ViewModel должна быть без контекста
 Важно: ViewModel не должна ничего возвращать в активити или фрагмент - для есть LiveData
 LiveData - переменная, содержащая в себе какие-то данные. На эти данные можно подписаться и при
 их изменении получать уведомление.
 */
class DevicesViewModel(adapterProvider: BluetoothAdapterProvider) : ViewModel() {
    private val _devices: MutableLiveData<List<BluetoothDevice>> = MutableLiveData()
    /* Геттер для _devices. Нужен для того, чтобы получатель данных (активити или фрагмент) не
    смогли ничего поменять во ViewModel. Изменение ViewModel из View противоречит паттерну MVVM*/
    val devices: LiveData<List<BluetoothDevice>> get() = _devices

    private val adapter = adapterProvider.getAdapter()
    private var scanner : BluetoothLeScanner? = null
    private var callback : BleScanCallback? = null

    private val settings: ScanSettings
    private val filters: List<ScanFilter>

    private val foundDevices = HashMap<String, BluetoothDevice>()

    init {
        settings = buildSettings()
        filters = buildFilter()
    }

    private fun buildSettings() =
        ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .build()

    private fun buildFilter() =
        listOf(
            ScanFilter.Builder()
                .setServiceUuid(FILTER_UUID)
                .build()
        )

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (callback == null) {
            callback = BleScanCallback()
            scanner = adapter.bluetoothLeScanner
            scanner?.startScan(filters, settings, callback)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (callback != null) {
            scanner?.stopScan(callback)
            scanner = null
            callback = null
        }
    }

    // Вызывается, когда связанная с данной ViewModel активити уничтожается
    override fun onCleared() {
        super.onCleared()
        stopScan()
    }

    inner class BleScanCallback : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            foundDevices[result.device.address] = result.device
            _devices.postValue(foundDevices.values.toList())
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            results.forEach { result ->
                foundDevices[result.device.address] = result.device
            }
            _devices.postValue(foundDevices.values.toList())
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BluetoothScanner", "onScanFailed:  scan error $errorCode")
        }
    }

    companion object {
        val FILTER_UUID: ParcelUuid = ParcelUuid.fromString("6f59f19e-2f39-49de-8525-5d2045f4d999")
    }
}

/* Создание ViewModel. Используется в DeviceFragment.
Классический способ создания ViewModel через конструктор нам не подходит т.к. мы должны
получать ViewModel так: MyViewModel myViewModel = ViewModelProvider.of(this).get(MyViewModel.class)
Таким образом, чтобы передать аргумент нужно использовать фабрику.
 */
class DeviceViewModelFactory(private val adapterProvider: BluetoothAdapterProvider)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DevicesViewModel::class.java)) {
            return DevicesViewModel(adapterProvider) as T
        }
        throw IllegalArgumentException("View Model not found")
    }
}