package com.rmscore.bases;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
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
    public RMSService RmsService;
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

    public BaseActivity() {
        Utils.log("Life cycle BaseActivity started on " + (getClass().getName()));
    }

    protected void UnbindService() {
        Utils.log("Life cycle Service Disconnected at " + BaseActivity.this.getClass().getName());

        unbindService(this.rmsServiceConnection);
        ServiceConnected = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utils.log("Life cycle onCreate at " + getClass().getName());

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
        Utils.log("Life cycle onStart at " + getClass().getName());

        if ((!(this instanceof Welcome)) && (RmsService != null) && (!RmsService.IsBluetoothConnected())) {
            finish();
        }

        if (RmsService != null)
            RmsService.CurrentActivity = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.log("Life cycle onResume at " + getClass().getName());

        if (RmsService != null)
            RmsService.CurrentActivity = this;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.log("Life cycle onPause at " + getClass().getName());

        if (RmsService != null)
            RmsService.CurrentActivity = null;
    }

    @Override
    protected void onDestroy() {
        Utils.log("Life cycle onDestroy at " + getClass().getName());
        super.onDestroy();

        if (RmsService != null)
            RmsService.CurrentActivity = null;
    }

    @Override
    public void ServiceStarted() {
        Utils.log("Life cycle Service started at " + BaseActivity.this.getClass().getName());

        RmsService.CurrentActivity = this;
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
                } else {
                    showAlertDialog(getString(R.string.bt_app_needs_bluetooth));
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
                    Toast.makeText(this, getString(R.string.bt_device_lost), Toast.LENGTH_SHORT).show();
                } else
                    startBluetoothDeviceListActivity();
            } else {
                // Asks for bluetooth permission
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        } else {
            showAlertDialog(getString(R.string.bt_no_support));
        }
    }

    public void BluetoothEvent(DeviceConnector.BLUETOOTH_EVENT bluetoothEvent) {
        switch (bluetoothEvent) {
            case NONE:
                break;
            case CONNECTION_LOST:
                toast(getString(R.string.bt_device_lost));
                if (!(this instanceof Welcome)) {
                    finish();
                }
                break;
            case CONNECTION_FAILED:
                toast(getString(R.string.bt_device_failed));
                break;
            case CONNECTING:
                toast(getString(R.string.bt_device_connecting));
                break;
            case CONNECTED:
                toast(getString(R.string.bt_device_connected));
                break;
        }
    }

    public void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void showAlertDialog(String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getString(R.string.app_name));
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton("OK", null);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
