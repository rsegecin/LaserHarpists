package com.rmscore;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.rmscore.bluetooth.BluetoothHandler;
import com.rmscore.bluetooth.DeviceConnector;
import com.rmscore.bluetooth.iBluetoothHandler;
import com.rmscore.datamodels.DeviceData;
import com.rmscore.laserharpists.R;
import com.rmscore.utils.Utils;

public class RMSService extends IntentService implements iBluetoothHandler {

    private static BluetoothHandler bluetoothHandler;
    private static DeviceConnector bluetoothConnector;
    private final IBinder mBinder = new LocalBinder();
    public String BluetoothDeviceName;
    private BluetoothAdapter bluetoothAdapter;

    public RMSService() {
        super("RMSService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (bluetoothHandler == null) bluetoothHandler = new BluetoothHandler(this);
        else bluetoothHandler.setTarget(this);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Utils.log(getString(R.string.bt_no_support));
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

    /**
     * Getting the status of the device
     */
    public DeviceConnector.BLUETOOTH_EVENT getBluetoothState() {
        if (bluetoothConnector != null)
            return bluetoothConnector.getState();
        else
            return null;
    }

    public boolean HasBluetooth() {
        return (bluetoothAdapter != null);
    }

    public boolean IsBluetoothReady() {
        return (bluetoothAdapter != null) && (bluetoothAdapter.isEnabled());
    }

    public boolean IsBluetoothConnected() {
        return ((bluetoothConnector != null) &&
                ((bluetoothConnector.getState() == DeviceConnector.BLUETOOTH_EVENT.CONNECTED) ||
                        (bluetoothConnector.getState() == DeviceConnector.BLUETOOTH_EVENT.READ) ||
                        (bluetoothConnector.getState() == DeviceConnector.BLUETOOTH_EVENT.WRITE)));
    }

    public void DisconnectBluetooth() {
        if (bluetoothConnector != null) {
            bluetoothConnector.stop();
            bluetoothConnector = null;
            BluetoothDeviceName = null;
        }
    }

    public void ConnectWithBluetooth(String address, BluetoothHandler bluetoothHandlerParam) throws Exception {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        if (IsBluetoothReady()) {
            DisconnectBluetooth();
            String emptyName = getString(R.string.bt_empty_device_name);
            DeviceData data = new DeviceData(device, emptyName);

            bluetoothHandler = bluetoothHandlerParam;

            bluetoothConnector = new DeviceConnector(data, bluetoothHandler);
            bluetoothConnector.connect();
        } else {
            throw new Exception("Habilite o uso do Bluetooth para a aplicação");
        }
    }

    public void SetNewBluetoothHandler(Handler handler) {
        bluetoothConnector.SetHandler(handler);
    }

    public void UnsetBluetoothHandler() {
        bluetoothConnector.SetHandler(bluetoothHandler);
    }

    public void SendToBluetooth(String message) {
        if (message.isEmpty()) return;
        message += "\\r\\n";
        byte[] command = message.getBytes();
        if (IsBluetoothConnected()) {
            bluetoothConnector.write(command);
        }
    }

    @Override
    public void BluetoothEvent(DeviceConnector.BLUETOOTH_EVENT bluetoothEvent) {

    }

    @Override
    public void Read(String msg) {

    }

    @Override
    public void Written(String msg) {

    }

    public class LocalBinder extends Binder {
        public RMSService getService() {
            // Return this instance of SmartServices so clients can call public methods
            return RMSService.this;
        }
    }
}
