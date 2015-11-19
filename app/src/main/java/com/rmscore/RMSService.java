package com.rmscore;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.rmscore.bases.BaseActivity;
import com.rmscore.bluetooth.BluetoothHandler;
import com.rmscore.bluetooth.DeviceConnector;
import com.rmscore.bluetooth.iBluetoothHandler;
import com.rmscore.data.DataBaseManager;
import com.rmscore.datamodels.DeviceData;
import com.rmscore.laserharpists.R;
import com.rmscore.utils.Utils;

public class RMSService extends IntentService implements iBluetoothHandler {

    private final IBinder mBinder = new LocalBinder();
    public BaseActivity CurrentActivity = null;
    public DataBaseManager DBManager;
    public MusicManager musicManager;

    public String BluetoothDeviceName;
    private BluetoothHandler bluetoothHandler;
    private DeviceConnector bluetoothConnector;
    private BluetoothAdapter bluetoothAdapter;

    private BTInterpreter btInterpreter;

    public RMSService() {
        super("RMSService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (bluetoothHandler == null)
            bluetoothHandler = new BluetoothHandler(this);
        else
            bluetoothHandler.setTarget(this);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Utils.log(getString(R.string.bt_no_support));
        }

        btInterpreter = new BTInterpreter(this);

        DBManager = new DataBaseManager(this); // DataBaseManager must be created before any other manager that uses DataBase

        musicManager = new MusicManager(this);

        DBManager.SetDataTables(musicManager.DBTables);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
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

    public void ConnectWithBluetooth(String address) throws Exception {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        if (IsBluetoothReady()) {
            DisconnectBluetooth();
            String emptyName = getString(R.string.bt_empty_device_name);
            DeviceData data = new DeviceData(device, emptyName);

            bluetoothConnector = new DeviceConnector(data, bluetoothHandler);
            bluetoothConnector.connect();
        } else {
            throw new Exception("Habilite o uso do Bluetooth para a aplicação");
        }
    }

    public void SendToBluetooth(String message) {
        if (message.isEmpty())
            return;

        message += "\r\n";

        byte[] command = message.getBytes();

        if (IsBluetoothConnected()) {
            bluetoothConnector.write(command);
        }
    }

    @Override
    public void BluetoothEvent(DeviceConnector.BLUETOOTH_EVENT bluetoothEvent) {

        if (CurrentActivity != null) {
            CurrentActivity.BluetoothEvent(bluetoothEvent);
        }

        switch (bluetoothEvent) {
            case NONE:
                break;
            case CONNECTION_LOST:
                break;
            case CONNECTION_FAILED:
                break;
            case CONNECTING:
                break;
            case CONNECTED:
                break;
        }

    }

    @Override
    public void Read(String msg) {
        Utils.log("Bluetooth received: " + msg);

        btInterpreter.ReadMessage(msg);
    }

    @Override
    public void Written(String msg) {
        Utils.log("Bluetooth sent: " + msg);
    }

    public class LocalBinder extends Binder {
        public RMSService getService() {
            // Return this instance of SmartServices so clients can call public methods
            return RMSService.this;
        }
    }
}
