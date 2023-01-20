package ru.astar.bluetoothcontrol;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import androidx.annotation.NonNull;
import java.util.UUID;
import no.nordicsemi.android.ble.BleManager;


public class BleControlManager extends BleManager {
    public static final UUID SERVICE_CONTROL_UUID = UUID.fromString("6f59f19e-2f39-49de-8525-5d2045f4d999");
    public static final UUID CONTROL_RESPONSE_UUID = UUID.fromString("a9bf2905-ee69-4baa-8960-4358a9e3a558");

    private BluetoothGattCharacteristic dataCharacteristic;


    public BleControlManager(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return new BleControlManagerGattCallback();
    }

    class BleControlManagerGattCallback extends BleManagerGattCallback {

        @Override
        protected boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {
            BluetoothGattService service = gatt.getService(SERVICE_CONTROL_UUID);

            if (service != null) {
                dataCharacteristic = service.getCharacteristic(CONTROL_RESPONSE_UUID);
            }

            return dataCharacteristic != null;
        }

        @Override
        protected void onServicesInvalidated() {
            dataCharacteristic = null;
        }
    }
}
