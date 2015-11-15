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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import com.rmscore.datamodels.DeviceData;
import com.rmscore.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DeviceConnector {

    private final BluetoothAdapter btAdapter;
    private final BluetoothDevice connectedDevice;
    private final String deviceName;
    private BLUETOOTH_EVENT mBluetoothEvent;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private Handler mHandler;
    public DeviceConnector(DeviceData deviceData, Handler handler) {
        mHandler = handler;
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        connectedDevice = btAdapter.getRemoteDevice(deviceData.getAddress());
        deviceName = (deviceData.getName() == null) ? deviceData.getAddress() : deviceData.getName();
        mBluetoothEvent = BLUETOOTH_EVENT.NONE;
    }

    /**
     * Getting the status of the device
     */
    public synchronized BLUETOOTH_EVENT getState() {
        return mBluetoothEvent;
    }

    /**
     * Installing an internal device status
     *
     * @param bluetoothEvent
     */
    private synchronized void setState(BLUETOOTH_EVENT bluetoothEvent) {
        Utils.log("Bluetooth setState() " + mBluetoothEvent + " -> " + bluetoothEvent);

        switch (bluetoothEvent) {
            case NONE:
                Utils.log("Bluetooth no connection.");
                break;
            case CONNECTION_LOST:
                Utils.log("Bluetooth connection lost.");
                break;
            case CONNECTION_FAILED:
                Utils.log("Bluetooth connection failed.");
                break;
            case CONNECTING:
                Utils.log("Bluetooth connecting.");
                break;
            case CONNECTED:
                Utils.log("Bluetooth connected with " + deviceName + ".");
                break;
        }

        mBluetoothEvent = bluetoothEvent;
        Message msg = mHandler.obtainMessage(bluetoothEvent.ordinal());
        mHandler.sendMessage(msg);
    }

    /**
     * The connection request from the Device Features
     */
    public synchronized void connect() {
        Utils.log("Bluetooth connect to: " + connectedDevice);

        if (mBluetoothEvent == BLUETOOTH_EVENT.CONNECTING) {
            if (mConnectThread != null) {
                Utils.log("Bluetooth cancel mConnectThread");
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        if (mConnectedThread != null) {
            Utils.log("Bluetooth cancel mConnectedThread");
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(connectedDevice);
        mConnectThread.start();
        setState(BLUETOOTH_EVENT.CONNECTING);
    }

    public synchronized void connected(BluetoothSocket socket) {
        Utils.log("Bluetooth connected");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            Utils.log("Bluetooth cancel mConnectThread");
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            Utils.log("Bluetooth cancel mConnectedThread");
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(BLUETOOTH_EVENT.CONNECTED);

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
    }

    /**
     * Disconnecting
     */
    public synchronized void stop() {
        Utils.log("Bluetooth stop");

        if (mConnectThread != null) {
            Utils.log("Bluetooth cancel mConnectThread");
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            Utils.log("Bluetooth cancel mConnectedThread");
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(BLUETOOTH_EVENT.NONE);
    }

    public void write(byte[] data) {
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if ((mBluetoothEvent == BLUETOOTH_EVENT.NONE) ||
                    (mBluetoothEvent == BLUETOOTH_EVENT.CONNECTING) ||
                    (mBluetoothEvent == BLUETOOTH_EVENT.CONNECTION_FAILED) ||
                    (mBluetoothEvent == BLUETOOTH_EVENT.CONNECTION_LOST))
                return;
            r = mConnectedThread;
        }

        r.writeData(data);
    }

    public enum BLUETOOTH_EVENT {
        NONE, CONNECTION_LOST, CONNECTION_FAILED, CONNECTING, CONNECTED, READ, WRITE
    }

    /**
     * Class stream to communicate with the BT- device
     */
    private class ConnectThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            Utils.log("Bluetooth create ConnectThread");
            mmDevice = device;
            mmSocket = BluetoothUtils.createRfcommSocket(mmDevice);
        }
        // ==========================================================================

        /**
         * The basic working method to connect to the device.
         * If the connection is successful transfers control to another thread
         */
        public void run() {
            Utils.log("Bluetooth ConnectThread run");
            btAdapter.cancelDiscovery();
            if (mmSocket == null) {
                Utils.log("Bluetooth unable to connect to device, socket isn't created");
                setState(BLUETOOTH_EVENT.CONNECTION_FAILED);
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
                    Utils.logError("Bluetooth unable to close() socket during connection failure");
                }
                setState(BLUETOOTH_EVENT.CONNECTION_FAILED);
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (this) {
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
            Utils.log("Bluetooth ConnectThread cancel");

            if (mmSocket == null) {
                Utils.log("Bluetooth unable to close null socket");
                return;
            }
            try {
                mmSocket.close();
            } catch (IOException e) {
                Utils.logError("Bluetooth close() of connect socket failed");
            }
        }
        // ==========================================================================
    }

    /**
     * Class stream to communicate with the BT- device
     */
    private class ConnectedThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Utils.log("Bluetooth create ConnectedThread");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Utils.logError("Bluetooth temp sockets not created");
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        // ==========================================================================

        /**
         * The basic working method - waits for commands from the incoming stream
         */
        public void run() {
            Utils.log("Bluetooth ConnectedThread run");
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
                    //if (readed.contains("\n")) {
                    mHandler.obtainMessage(BLUETOOTH_EVENT.READ.ordinal(), bytes, -1, readMessage.toString()).sendToTarget();
                    readMessage.setLength(0);
                    //}

                } catch (IOException e) {
                    setState(BLUETOOTH_EVENT.CONNECTION_LOST);
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
                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(BLUETOOTH_EVENT.WRITE.ordinal(), chunk.length, -1, new String(chunk)).sendToTarget();
            } catch (IOException e) {
                Utils.logError("Bluetooth exception during write");
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
                Utils.logError("Bluetooth exception close() of connect socket failed");
            }
        }
        // ==========================================================================
    }
}
