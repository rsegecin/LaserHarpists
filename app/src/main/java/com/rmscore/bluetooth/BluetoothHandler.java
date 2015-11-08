package com.rmscore.bluetooth;

import android.os.Handler;
import android.os.Message;

import com.rmscore.utils.Utils;

import java.lang.ref.WeakReference;

/**
 * Created by Rinaldi on 07/11/2015.
 */
public class BluetoothHandler extends Handler {
    private WeakReference<iBluetoothHandler> mInterfaceHandler;

    public BluetoothHandler(iBluetoothHandler interfaceHandler) {
        mInterfaceHandler = new WeakReference<>(interfaceHandler);
    }

    public void setTarget(iBluetoothHandler target) {
        mInterfaceHandler.clear();
        mInterfaceHandler = new WeakReference<>(target);

        Utils.log("Life cycle Bluetooth targeted to " + target.getClass().getName());
    }

    @Override
    public void handleMessage(Message msg) {
        iBluetoothHandler interfaceHandler = mInterfaceHandler.get();
        if (interfaceHandler != null) {
            DeviceConnector.BLUETOOTH_EVENT e = DeviceConnector.BLUETOOTH_EVENT.values()[msg.what];
            switch (e) {
                case READ:
                    //Utils.log("Bluetooth msg: " + msg);
                    interfaceHandler.Read((String) msg.obj);
                    interfaceHandler.BluetoothEvent(e);
                    break;
                case WRITE:
                    interfaceHandler.Written((String) msg.obj);
                    interfaceHandler.BluetoothEvent(e);
                    break;
                default:
                    interfaceHandler.BluetoothEvent(e);
                    break;
            }
        }
    }
}