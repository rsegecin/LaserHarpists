package com.rmscore;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.rmscore.bluetooth.DeviceConnector;
import com.rmscore.datamodels.DeviceData;
import com.rmscore.laserharpists.R;
import com.rmscore.utils.Utils;

public class RMSService extends IntentService {

    public String BluetoothDeviceName;

    private BroadcastReceiver sl;
    private BluetoothAdapter bluetoothAdapter;
    private static DeviceConnector bluetoothConnector;

    private final IBinder mBinder = new LocalBinder();
    public class LocalBinder extends Binder {
        public RMSService getService() {
            // Return this instance of SmartServices so clients can call public methods
            return RMSService.this;
        }
    }

    public RMSService() {
        super("RMSService");

        sl = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent intent) {
                String action = intent.getAction();

                synchronized (RMSService.this) {
                    if (action.equals(DeviceConnector.BLUETOOTH_INTENT_MANAGER)) {
                        if (intent.getStringExtra(DeviceConnector.BLUETOOTH_EXTRA_NAME) != null) {
                            BluetoothDeviceName = intent.getStringExtra(DeviceConnector.BLUETOOTH_EXTRA_NAME);
                        }
                        else if (intent.getStringExtra(DeviceConnector.BLUETOOTH_EXTRA_STATE) != null) {
                            String st = intent.getStringExtra(DeviceConnector.BLUETOOTH_EXTRA_NAME);
                            if (st.equals(DeviceConnector.BLUETOOTH_STATUS_CONNECTED)) {
                                String harp_cmd_prompt = getString(R.string.harp_cmd_prompt);
                                SendToBluetooth(harp_cmd_prompt);
                            }
                        }
                    }
                }
            }
        };
    }

    @Override
    public void onCreate() {
        super.onCreate();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Utils.log(getString(R.string.no_bt_support));
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //return super.onBind(intent);
        return mBinder;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    public boolean HasBluetooth() {
        return (bluetoothAdapter != null);
    }

    public boolean IsBluetoothReady() {
        return (bluetoothAdapter != null) && (bluetoothAdapter.isEnabled());
    }

    public boolean IsBluetoothConnected() {
        return (bluetoothConnector != null) && (bluetoothConnector.getState() == DeviceConnector.STATE_CONNECTED);
    }

    public void DisconnectBluetooth() {
        if (bluetoothConnector != null) {
            bluetoothConnector.stop();
            bluetoothConnector = null;
            BluetoothDeviceName = null;
        }
    }

    public void ConnectWithBluetooth(String address) throws Exception {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        if (IsBluetoothReady()) {
            DisconnectBluetooth();
            String emptyName = getString(R.string.empty_device_name);
            DeviceData data = new DeviceData(device, emptyName);
            bluetoothConnector = new DeviceConnector(data, this);
            bluetoothConnector.connect();
        }
        else {
            throw new Exception("Habilite o uso do Bluetooth para a aplicação");
        }
    }

    public void SendToBluetooth(String message) {
        if (message.isEmpty()) return;
        message += "\\r\\n";
        byte[] command = message.getBytes();
        if (IsBluetoothConnected()) {
            bluetoothConnector.write(command);
        }
    }
}
