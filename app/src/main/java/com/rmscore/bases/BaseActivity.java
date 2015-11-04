package com.rmscore.bases;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.rmscore.RMSService;
import com.rmscore.RMSService.LocalBinder;
import com.rmscore.bluetooth.DeviceConnector;
import com.rmscore.bluetooth.DeviceListActivity;
import com.rmscore.laserharpists.R;
import com.rmscore.laserharpists.SettingsActivity;
import com.rmscore.laserharpists.Welcome;
import com.rmscore.utils.Utils;

/**
 * Created by Rinaldi on 03/11/2015.
 */
public abstract class BaseActivity extends AppCompatActivity implements iBaseActivity {

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    public boolean PendingRequestBluetoothPermission = true;
    BroadcastReceiver sl;

    protected RMSService RmsService;
    protected boolean ServiceConnected = false;

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection rmsServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to RMSService, cast the IBinder and get RMSService instance
            LocalBinder binder = (LocalBinder) service;
            RmsService = binder.getService();
            ServiceConnected = true;

            ServiceStarted();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            ServiceConnected = false;
        }
    };

    protected BaseActivity() {
        sl = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent intent) {
                String action = intent.getAction();

                synchronized (BaseActivity.this) {
                    if (action.equals(DeviceConnector.BLUETOOTH_INTENT_MANAGER)) {
                        if (intent.getStringExtra(DeviceConnector.BLUETOOTH_EXTRA_MESSAGE) != null) {
                            Interpreter(intent.getStringExtra(DeviceConnector.BLUETOOTH_EXTRA_MESSAGE));
                        }
                    }
                }
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, RMSService.class);
        startService(intent);
        bindService(intent, rmsServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                final Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if ((!(this instanceof Welcome)) && (RmsService != null) && (!RmsService.IsBluetoothConnected())) {
            finish();
        }
        else if ((this instanceof Welcome) && (RmsService != null) && (PendingRequestBluetoothPermission)) {
            MakeBluetoothHappen();
        }
    }

    @Override
    public void ServiceStarted() {
        MakeBluetoothHappen();
    }

    @Override
    public void Interpreter(String msg) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    try {
                        RmsService.ConnectWithBluetooth(address);
                    } catch (Exception e) {
                        showAlertDialog(e.getMessage());
                    }
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    startBluetoothDeviceListActivity();
                    Utils.log("BT enabled");
                }
                else {
                    showAlertDialog(getString(R.string.app_needs_bluetooth));
                    PendingRequestBluetoothPermission = false;
                }
                break;
        }
    }

    private void startBluetoothDeviceListActivity() {
        RmsService.DisconnectBluetooth();
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    /**
     * Connects, disconnects with bluetooth and asks permission if necessary
     */
    protected void MakeBluetoothHappen() {
        if (RmsService.HasBluetooth()) {
            if (RmsService.IsBluetoothReady()) {
                if (RmsService.IsBluetoothConnected()) {
                    RmsService.DisconnectBluetooth();
                    Toast.makeText(this, getString(R.string.bt_device_disconnected), Toast.LENGTH_SHORT).show();
                }
                else
                    startBluetoothDeviceListActivity();
            } else {
                // Asks for bluetooth permission
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        else {
            showAlertDialog(getString(R.string.no_bt_support));
        }
    }

    protected void UnbindService() {
        unbindService(this.rmsServiceConnection);
        ServiceConnected = false;
    }

    protected void showAlertDialog(String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getString(R.string.app_name));
        alertDialogBuilder.setMessage(message);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
