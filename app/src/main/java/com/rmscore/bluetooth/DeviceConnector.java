/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rmscore.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

import com.rmscore.RMSService;
import com.rmscore.datamodels.DeviceData;

public class DeviceConnector {
    private static final String TAG = "DeviceConnector";
    private static final boolean D = false;

    public static final String BLUETOOTH_INTENT_MANAGER = "BtMng";
    public static final String BLUETOOTH_EXTRA_STATE = "BtState";
    public static final String BLUETOOTH_EXTRA_MESSAGE = "BtMsg";
    public static final String BLUETOOTH_EXTRA_NAME = "BtName";
    public static final String BLUETOOTH_STATUS_CONNECTED = "CONNECTED";
    public static final String BLUETOOTH_STATUS_CONNECTING = "CONNECTING";
    public static final String BLUETOOTH_STATUS_NONE = "NONE";

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_CONNECTING = 1; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 2;  // now connected to a remote device

    private int mState;

    private final BluetoothAdapter btAdapter;
    private final BluetoothDevice connectedDevice;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private final RMSService rmsService;
    private final String deviceName;
    // ==========================================================================


    public DeviceConnector(DeviceData deviceData, RMSService service) {
        rmsService = service;
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        connectedDevice = btAdapter.getRemoteDevice(deviceData.getAddress());
        deviceName = (deviceData.getName() == null) ? deviceData.getAddress() : deviceData.getName();
        mState = STATE_NONE;
    }
    // ==========================================================================


    /**
     * The connection request from the Device Features
     */
    public synchronized void connect() {
        if (D) Log.d(TAG, "connect to: " + connectedDevice);

        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                if (D) Log.d(TAG, "cancel mConnectThread");
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        if (mConnectedThread != null) {
            if (D) Log.d(TAG, "cancel mConnectedThread");
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(connectedDevice);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }
    // ==========================================================================

    /**
     * Disconnecting
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");

        if (mConnectThread != null) {
            if (D) Log.d(TAG, "cancel mConnectThread");
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            if (D) Log.d(TAG, "cancel mConnectedThread");
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);
    }
    // ==========================================================================


    /**
     * Installing an internal device status
     *
     * @param state
     */
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        Intent intent = new Intent(BLUETOOTH_INTENT_MANAGER);
        switch (state) {
            case DeviceConnector.STATE_CONNECTED:
                intent.putExtra(BLUETOOTH_EXTRA_STATE, BLUETOOTH_STATUS_CONNECTED);
                break;
            case DeviceConnector.STATE_CONNECTING:
                intent.putExtra(BLUETOOTH_EXTRA_STATE, BLUETOOTH_STATUS_CONNECTING);
                break;
            case DeviceConnector.STATE_NONE:
                intent.putExtra(BLUETOOTH_EXTRA_STATE, BLUETOOTH_STATUS_NONE);
                break;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        rmsService.sendBroadcast(intent);
    }
    // ==========================================================================


    /**
     * Getting the status of the device
     */
    public synchronized int getState() {
        return mState;
    }
    // ==========================================================================


    public synchronized void connected(BluetoothSocket socket) {
        if (D) Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            if (D) Log.d(TAG, "cancel mConnectThread");
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            if (D) Log.d(TAG, "cancel mConnectedThread");
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_CONNECTED);

        Intent intent = new Intent(BLUETOOTH_INTENT_MANAGER);
        intent.putExtra(BLUETOOTH_EXTRA_NAME, deviceName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        rmsService.sendBroadcast(intent);

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
    }
    // ==========================================================================


    public void write(byte[] data) {
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }

        // Perform the write unsynchronized
        if (data.length == 1) r.write(data[0]);
        else r.writeData(data);
    }
    // ==========================================================================


    private void connectionFailed() {
        if (D) Log.d(TAG, "connectionFailed");
        setState(STATE_NONE);
    }
    // ==========================================================================


    private void connectionLost() {
        if (D) Log.d(TAG, "connectionLost");
        setState(STATE_NONE);
    }
    // ==========================================================================


    /**
     * Stream class to connect to the BT - device
     */
    // ==========================================================================
    private class ConnectThread extends Thread {
        private static final String TAG = "ConnectThread";
        private static final boolean D = false;

        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            if (D) Log.d(TAG, "create ConnectThread");
            mmDevice = device;
            mmSocket = BluetoothUtils.createRfcommSocket(mmDevice);
        }
        // ==========================================================================

        /**
         * The basic working method to connect to the device.
         * If the connection is successful transfers control to another thread
         */
        public void run() {
            if (D) Log.d(TAG, "ConnectThread run");
            btAdapter.cancelDiscovery();
            if (mmSocket == null) {
                if (D) Log.d(TAG, "unable to connect to device, socket isn't created");
                connectionFailed();
                return;
            }

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    if (D) Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (DeviceConnector.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket);
        }
        // ==========================================================================


        /**
         * Cancel connection
         */
        public void cancel() {
            if (D) Log.d(TAG, "ConnectThread cancel");

            if (mmSocket == null) {
                if (D) Log.d(TAG, "unable to close null socket");
                return;
            }
            try {
                mmSocket.close();
            } catch (IOException e) {
                if (D) Log.e(TAG, "close() of connect socket failed", e);
            }
        }
        // ==========================================================================
    }
    // ==========================================================================


    /**
     * Class stream to communicate with the BT- device
     */
    // ==========================================================================
    private class ConnectedThread extends Thread {
        private static final String TAG = "ConnectedThread";
        private static final boolean D = false;

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            if (D) Log.d(TAG, "create ConnectedThread");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                if (D) Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        // ==========================================================================

        /**
         * The basic working method - waits for commands from the incoming stream
         */
        public void run() {
            if (D) Log.i(TAG, "ConnectedThread run");
            byte[] buffer = new byte[512];
            int bytes;
            StringBuilder readMessage = new StringBuilder();
            while (true) {
                try {
                    // I read input data from the stream and collected in a response line
                    bytes = mmInStream.read(buffer);
                    String readed = new String(buffer, 0, bytes);
                    readMessage.append(readed);

                    // marker of the end of the team - to return a response in the main stream
                    if (readed.contains("\n")) {
                        Intent intent = new Intent(BLUETOOTH_INTENT_MANAGER);
                        intent.putExtra(BLUETOOTH_EXTRA_MESSAGE, readMessage.toString());
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        rmsService.sendBroadcast(intent);

                        readMessage.setLength(0);
                    }

                } catch (IOException e) {
                    if (D) Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }
        // ==========================================================================


        /**
         * Write a piece of data to the device
         */
        public void writeData(byte[] chunk) {

            try {
                mmOutStream.write(chunk);
                mmOutStream.flush();
            } catch (IOException e) {
                if (D) Log.e(TAG, "Exception during write", e);
            }
        }
        // ==========================================================================


        /**
         * Write bytes
         */
        public void write(byte command) {
            byte[] buffer = new byte[1];
            buffer[0] = command;

            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                if (D) Log.e(TAG, "Exception during write", e);
            }
        }
        // ==========================================================================


        /**
         * Cancel - closes the socket
         */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                if (D) Log.e(TAG, "close() of connect socket failed", e);
            }
        }
        // ==========================================================================
    }
    // ==========================================================================
}
