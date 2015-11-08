package com.rmscore.bluetooth;

/**
 * Created by Rinaldi on 07/11/2015.
 */
public interface iBluetoothHandler {
    void BluetoothEvent(DeviceConnector.BLUETOOTH_EVENT bluetoothEvent);

    void Read(String msg);

    void Written(String msg);
}
