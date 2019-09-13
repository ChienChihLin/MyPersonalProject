/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.ambiq.ble.ambiqota.Services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.ambiq.ble.ambiqota.OTA.SampleGattAttributes;
import com.ambiq.ble.ambiqota.TB.TestShowViewActivity;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    public int mConnectionState = STATE_DISCONNECTED;

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED = "com.ambiqmicro.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.ambiqmicro.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.ambiqmicro.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.ambiqmicro.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.ambiqmicro.bluetooth.le.EXTRA_DATA";
    public final static String ACTION_GATT_WRITE_RESULT = "com.ambiqmicro.bluetooth.le.ACTION_WRITE_RESULT";
    public final static String ACTION_GATT_GOT_RSSI = "com.ambiqmicro.bluetooth.le.ACTION_GATT_GOT_RSSI";

    public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);
    public final static UUID UUID_AMOTA_TX = UUID.fromString(SampleGattAttributes.ATT_UUID_AMOTA_TX);

    // Sensor service
    public static final UUID Sensor_Service = UUID.fromString("00002760-08C2-11E1-9073-0E8AC72E2001");
    public static final UUID Sensor_Read_UV = UUID.fromString("00002760-08C2-11E1-9073-0E8AC72E0200");
    public static final UUID Sensor_Read_HandT = UUID.fromString("00002760-08C2-11E1-9073-0E8AC72E0201");
    public static final UUID Sensor_Read_Accerometer = UUID.fromString("00002760-08C2-11E1-9073-0E8AC72E0202");
    public static final UUID Sensor_Read_Heart_Rate_Raw = UUID.fromString("00002760-08C2-11E1-9073-0E8AC72E0203");
    // Setting service
    public static final UUID Setting_Service = UUID.fromString("00002760-08C2-11E1-9073-0E8AC72E3001");
    public static final UUID Setting_Switch = UUID.fromString("00002760-08C2-11E1-9073-0E8AC72E0300");
    // Heart Rate service(Standard)
    private static final UUID HEART_RATE_SERVICE_UUID = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb");
    private static final UUID HEART_RATE_MEASUREMENT_UUID = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb");
    private static final UUID BODY_SENSOR_LOCATION_UUID = UUID.fromString("00002A38-0000-1000-8000-00805f9b34fb");
    private static final UUID HEART_RATE_CONTROL_POINT_UUID = UUID.fromString("00002A39-0000-1000-8000-00805f9b34fb");
    // Battery Information service(Standard)
    private static final UUID BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
    private static final UUID BATTERY_LEVEL_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

    public static int[] compareArray = new int[5];
    public static BluetoothGatt pubBluetoothGatt;

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
//                // 20171030, test
//                TestBLEActivity.blestatus = "Connected";
//                Intent intent = new Intent();
//                Bundle bundle = new Bundle();
//                bundle.putString("status","true");
//                intent.putExtras(bundle);
//                intent.setAction(TestBLEActivity.BLEConnectStatus);
//                sendBroadcast(intent);
//                // 20171030, test end

                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
//                TestBLEActivity.blestatus = "Disconnected";
//
//                // 20171030, test
//                Intent intent = new Intent();
//                Bundle bundle = new Bundle();
//                bundle.putString("status","true");
//                intent.putExtras(bundle);
//                intent.setAction(TestBLEActivity.BLEConnectStatus);
//                sendBroadcast(intent);
//                // 20171030, test end

                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                List<BluetoothGattService> btService = gatt.getServices();
                for (int i =0;i<btService.size();i++) {
                    Log.d(TAG, "discovered service:" + btService.get(i).getUuid().toString());
                    for(int j = 0;j< btService.get(i).getCharacteristics().size();j++){
                        Log.d(TAG, "discovered characteristic:" + btService.get(i).getCharacteristics().get(j).getUuid().toString());
//                        if (btService.get(i).getCharacteristics().get(j).getUuid().toString().equals(HEART_RATE_MEASUREMENT_UUID.toString())){
//                            setCharacteristicNotification(btService.get(i).getCharacteristics().get(j),true);
//                        }
                    }
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);

                Log.d(TAG,"GET READ!!!!!,"+characteristic.getUuid().toString());
                if (characteristic.getUuid().toString().equals(Sensor_Read_UV.toString())){
                    Log.d(TAG,"GET UV!!!!!");

                    byte[] theByteArray = characteristic.getValue();
                    Log.d(TAG,"theByteArray:"+ Arrays.toString(theByteArray));
                    int value= 0;
                    for(int i=0; i<theByteArray.length; i++)
                        value = (value << 8) | theByteArray[i];
                    Log.d(TAG,"UV value:"+value);
                    TestShowViewActivity.UV = String.valueOf(value);

                    // 20171030, test
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putString("sensor","UV");
                    intent.putExtras(bundle);
                    intent.setAction(TestShowViewActivity.BLEGetSensorData);
                    sendBroadcast(intent);
                    // 20171030, test end

                } else if(characteristic.getUuid().toString().equals(Sensor_Read_HandT.toString())) {
                    Log.d(TAG,"GET HT!!!!!");

                    byte[] theByteArray = characteristic.getValue();
                    Log.d(TAG,"theByteArray:"+ Arrays.toString(theByteArray)+",size:"+theByteArray.length);
                    float H_value;
                    float T_value;

                    int l;
                    l = theByteArray[0];
                    l &= 0xff;
                    l |= ((long) theByteArray[1] << 8);
                    l &= 0xffff;
                    l |= ((long) theByteArray[2] << 16);
                    l &= 0xffffff;
                    l |= ((long) theByteArray[3] << 24);
                    H_value =  Float.intBitsToFloat(l);

                    for(int i=0; i< 4; i++){
                        //H_value = (H_value << 8) | theByteArray[i];
                    }
                    Log.d(TAG,"H value:"+H_value);

                    int l2;
                    l2 = theByteArray[4];
                    l2 &= 0xff;
                    l2 |= ((long) theByteArray[5] << 8);
                    l2 &= 0xffff;
                    l2 |= ((long) theByteArray[6] << 16);
                    l2 &= 0xffffff;
                    l2 |= ((long) theByteArray[7] << 24);
                    T_value =  Float.intBitsToFloat(l2);

                    for(int i=4; i< theByteArray.length; i++){
                        //T_value = (T_value << 8) | theByteArray[i];
                    }
                    Log.d(TAG,"T value:"+T_value);

                    TestShowViewActivity.HT = String.valueOf(H_value) + "," + String.valueOf(T_value);
                    TestShowViewActivity.Humidity = String.valueOf(H_value);
                    TestShowViewActivity.Temp = String.valueOf(T_value);

                    // 20171030, test
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putString("sensor","HT");
                    intent.putExtras(bundle);
                    intent.setAction(TestShowViewActivity.BLEGetSensorData);
                    sendBroadcast(intent);
                    // 20171030, test end
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            //Log.d(TAG,"GET Notification!!!!!");

            byte[] theByteArray = characteristic.getValue();
//            Log.d(TAG,"theByteArray:"+ Arrays.toString(theByteArray)+",size:"+theByteArray.length);
            int value = 0;
//            int cnt = 0;
//            boolean checkTag = false;
            byte[] valueArray = new byte[4];
            if (characteristic.getUuid().toString().equals(Sensor_Read_Heart_Rate_Raw.toString())) {
                //int j;
                Log.d(TAG,"GET HR_RAW_DATA!!!!!");

                //Log.d(TAG,"theByteArray:"+ Arrays.toString(theByteArray)+",size:"+theByteArray.length);
                int checkCnt = 0;
                for (int i = 0; i < (theByteArray.length/4); i++) {
                    //value = 0;
                    for (char j = 0; j < 4; j++) {
                        valueArray[j] = theByteArray[(j+(i*4))];
                    }
                    value = byteArrayToInt(valueArray);
                    //Log.d(TAG,"compareArray[" + i + "]: " + compareArray[i]);
                    //Log.d(TAG,"value[" + i + "]: " + value);
                    if (compareArray[i] == value) {
                        checkCnt++;
                    }
                    compareArray[i] = value;
                }

                if (checkCnt == (theByteArray.length/4)) {
                    //Log.d(TAG,"repeat package!!!!");
                } else {
                    for (int j = 0; j < (theByteArray.length/4); j++) {
                        if (compareArray[j] != -1) {
//                            if (compareArray[j] > 0) {
//                                compareArray[j] = compareArray[j] * 18000 / 131071;
//                            } else {
//                                compareArray[j] = compareArray[j] * -18000 / -131072;
//                            }
//                            Log.d(TAG, "//////***** ecg_test_rawData:" + compareArray[j]);
                            if (TestShowViewActivity.dataIndex < 375) {
                                TestShowViewActivity.hrRawDataArray[TestShowViewActivity.dataIndex] = compareArray[j];
                                TestShowViewActivity.dataIndex += 1;
                            } else {
                                TestShowViewActivity.dataIndex = 0;
                                TestShowViewActivity.hrRawDataArray[TestShowViewActivity.dataIndex] = compareArray[j];
                            }
                        }
                    }
                }
//                for (int j = 0; j < (theByteArray.length/4); j++) {
//                    //Log.d(TAG,"(theByteArray.length/4):" + (theByteArray.length/4));
//                    value = 0;
//                    for (int k = 0; k < 4; k++) {
//                        value = (theByteArray[cnt++] << (k*8) | value);
//                    }
//                    //Log.d(TAG,"i value:" + cnt);
//                    //Log.d(TAG,"value:" + value);
//                    if (value != -1) {
//                        Log.d(TAG, "ecg_test_rawData:" + String.valueOf(value));
//                        if (TestShowViewActivity.dataIndex < 375) {
//                            TestShowViewActivity.hrRawDataArray[TestShowViewActivity.dataIndex] = value;
//                            TestShowViewActivity.dataIndex += 1;
//                        } else {
//                            TestShowViewActivity.dataIndex = 0;
//                            TestShowViewActivity.hrRawDataArray[TestShowViewActivity.dataIndex] = value;
//                        }
//                    }
//                    //Log.d(TAG,"j value:" + j);
//                    //Log.d(TAG,"HR raw value:"+TestBLEActivity.hrRawDataArray[j]);
//                }

                //TestBLEActivity.HR = String.valueOf(value);

                // 20171030, test
//                Intent intent = new Intent();
//                Bundle bundle = new Bundle();
//                bundle.putString("setnotify","HR");
//                intent.putExtras(bundle);
//                intent.setAction(TestBLEActivity.BLEGetNotify);
//                sendBroadcast(intent);
                // 20171030, test end
            } else if (characteristic.getUuid().toString().equals(HEART_RATE_MEASUREMENT_UUID.toString())) {
                //Log.d(TAG,"theByteArray:" + Arrays.toString(theByteArray) + ",size:" + theByteArray.length);
                int flag = characteristic.getProperties();
                int format = -1;
                if ((flag & 0x01) != 0) {
                    format = BluetoothGattCharacteristic.FORMAT_UINT16;
                    //Log.d(TAG, "Heart rate format UINT16.");
                } else {
                    format = BluetoothGattCharacteristic.FORMAT_UINT8;
                    //Log.d(TAG, "Heart rate format UINT8.");
                }
                final int heartRate = characteristic.getIntValue(format, 1);
                //Log.d(TAG, String.format("Received heart rate: %d", heartRate));
                TestShowViewActivity.HR = String.valueOf(heartRate);
                Log.d(TAG, "HR Bpm: " + TestShowViewActivity.HR);

                // 20171030, test
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("setnotify","HR");
                intent.putExtras(bundle);
                intent.setAction(TestShowViewActivity.BLEGetNotify);
                sendBroadcast(intent);
                // 20171030, test end
//                value = byteArrayToInt(theByteArray);
//                Log.d(TAG,"HRM value:" + value);
//                TestShowViewActivity.HR = String.valueOf(value);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status) {
            broadcastUpdate(ACTION_GATT_WRITE_RESULT, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            // TODO Auto-generated method stub
            super.onReadRemoteRssi(gatt, rssi, status);
            broadcastUpdate(ACTION_GATT_GOT_RSSI, rssi);
            System.out.println(rssi);
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,int status) {
        final Intent intent = new Intent(action);
        intent.putExtra(action, status);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        //Log.d(TAG, String.format("Received heart rate"));
        // For all other profiles, writes the data formatted in HEX.
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            intent.putExtra(EXTRA_DATA, data);
        }
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }


    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(getApplicationContext(), false, mGattCallback);
        pubBluetoothGatt = mBluetoothGatt;
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothDeviceAddress = null;
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public String getConnectedStateString()
    {
        if(mConnectionState == STATE_CONNECTED){
            return "Connected";
        }else if(mConnectionState == STATE_CONNECTING){
            return "Connecting";
        }else if(mConnectionState == STATE_DISCONNECTED){
            return "Disconnected";
        }else{
            return "Unknown State";
        }
    }

    public int getConnectedState()
    {
        return mConnectionState;
    }
    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        //if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid()) || UUID_AMOTA_TX.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            if(descriptor != null) {
                if (enabled) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                } else {
                    descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                }
            }

            boolean isSuccess = mBluetoothGatt.writeDescriptor(descriptor);
        //}


//        // 20171103, test
//        String str = characteristic.getUuid().toString();
//        if (isSuccess){
//            if (str.equals(Sensor_Read_UV.toString())){
//                str = "Set UV Notify Success";
//            }else if (str.equals(Sensor_Read_HandT.toString())){
//                str = "Set H & T Notify Success";
//            }
//        }else{
//            if (str.equals(Sensor_Read_UV.toString())){
//                str = "Set UV Notify Fail";
//            }else if (str.equals(Sensor_Read_HandT.toString())){
//                str = "Set H & T Notify Fail";
//            }
//        }
//        Intent intent = new Intent();
//        Bundle bundle = new Bundle();
//        bundle.putString("setnotify",str);
//        intent.putExtras(bundle);
//        intent.setAction(TestBLEActivity.BLEGetNotify);
//        sendBroadcast(intent);
//        // 20171103, test end
    }

    public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] data) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }

        characteristic.setValue(data);
        boolean isSuccess = mBluetoothGatt.writeCharacteristic(characteristic);

//        // 20171103, test
//        String str = characteristic.getUuid().toString();
//        if (isSuccess){
//            if (data[0]==0x00){
//            }else if (data[0] == 0x01){
//                str = "Write UV Success," + data[1];
//            }else if (data[0] == 0x02){
//                str = "Write H & T Success," + data[1];
//            }
//        }else{
//            if(data[0]==0x00){
//            }else if (data[0] == 0x01){
//                str = "Write UV Fail," + data[1];
//            }else if (data[0] == 0x02){
//                str = "Write H & T Fail," + data[1];
//            }
//        }
//        Intent intent = new Intent();
//        Bundle bundle = new Bundle();
//        bundle.putString("setnotify",str);
//        intent.putExtras(bundle);
//        intent.setAction(TestBLEActivity.BLEGetNotify);
//        sendBroadcast(intent);
        // 20171103, test end

        return isSuccess;
        //return mBluetoothGatt.writeCharacteristic(characteristic);
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    public BluetoothDevice getDevice(){
        return mBluetoothGatt.getDevice();
    }
    /**********************************************************************************************/
    public void startReadUV() {
        if (mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothGatt not initialized");
            return;
        }
        BluetoothGattService btGattService = mBluetoothGatt.getService(Sensor_Service);
        if(btGattService == null){
            Log.d(TAG,"BLE Service fail!!");
            return;
        }

        BluetoothGattCharacteristic mCharacteristic;
        mCharacteristic = btGattService.getCharacteristic(Sensor_Read_UV);

        readCharacteristic(mCharacteristic);
    }

    public void startReadHT(){
        if (mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothGatt not initialized");
            return;
        }

        BluetoothGattService btGattService = mBluetoothGatt.getService(Sensor_Service);
        if(btGattService == null){
            Log.d(TAG,"BLE Service fail!!");
            return;
        }

        BluetoothGattCharacteristic mCharacteristic;
        mCharacteristic = btGattService.getCharacteristic(Sensor_Read_HandT);

        readCharacteristic(mCharacteristic);
    }

    public void startReadHR(boolean _switch) {
        if (mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothGatt not initialized");
            return;
        }

//        BluetoothGattDescriptor descriptor = ecgChar.getDescriptor(
//                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
//        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//        mGatt.writeDescriptor(descriptor);

        BluetoothGattService btGattService = mBluetoothGatt.getService(HEART_RATE_SERVICE_UUID);
        if (btGattService == null) {
            Log.d(TAG,"BLE Service fail!!");
            return;
        }

        BluetoothGattCharacteristic mCharacteristic;
        mCharacteristic = btGattService.getCharacteristic(HEART_RATE_MEASUREMENT_UUID);
        setCharacteristicNotification(mCharacteristic, _switch);

        BluetoothGattDescriptor descriptor = mCharacteristic.getDescriptor(
                UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
    }

    public void startReadHrRaw(boolean _switch) {
        if (mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothGatt not initialized");
            return;
        }

        BluetoothGattService btGattService = mBluetoothGatt.getService(Sensor_Service);
        if (btGattService == null) {
            Log.d(TAG,"BLE Service fail!!");
            return;
        }

        BluetoothGattCharacteristic mCharacteristic;
        mCharacteristic = btGattService.getCharacteristic(Sensor_Read_Heart_Rate_Raw);
        setCharacteristicNotification(mCharacteristic, _switch);

        BluetoothGattDescriptor descriptor = mCharacteristic.getDescriptor(
                UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
    }

    public void startSetting(int switch1){
        if (mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothGatt not initialized");
            return;
        }

        BluetoothGattService btGattService = mBluetoothGatt.getService(Setting_Service);
        if(btGattService == null){
            Log.d(TAG,"BLE Service fail!!");
            return;
        }

        BluetoothGattCharacteristic mCharacteristic;
        mCharacteristic = btGattService.getCharacteristic(Setting_Switch);

        byte[] ret = new byte[2];
        ret[0] = 0x02;
        if(switch1 == 0)
            ret[1] = 0x00;
        else
            ret[1] = 0x01;
        writeCharacteristic(mCharacteristic, ret);
    }

    public void startSetting (int switch1,boolean s) {
        if (mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothGatt not initialized");
            return;
        }

        BluetoothGattService btGattService = mBluetoothGatt.getService(Setting_Service);
        if (btGattService == null) {
            Log.d(TAG,"BLE Service fail!!");
            return;
        }

        BluetoothGattCharacteristic mCharacteristic;
        mCharacteristic = btGattService.getCharacteristic(Setting_Switch);
// Zen mod, start
        byte[] ret = new byte[2];
        if (switch1 == 0) {
            ret[0] = 0x00;
            if (s)
                ret[1] = 0x01;
            else
                ret[1] = 0x00;
        } else if (switch1 == 1) {
            ret[0] = 0x01;
            if (s)
                ret[1] = 0x01;
            else
                ret[1] = 0x00;
        } else if (switch1 == 2) {
            ret[0] = 0x02;
            if (s)
                ret[1] = 0x01;
            else
                ret[1] = 0x00;
        } else if (switch1 == 3) {
            ret[0] = 0x03;
            if (s)
                ret[1] = 0x01;
            else
                ret[1] = 0x00;
        }
// Zen mod, end
        writeCharacteristic(mCharacteristic,ret);
    }

    public void setNotify() {
        if (mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothGatt not initialized");
            return;
        }

        BluetoothGattService btGattService = mBluetoothGatt.getService(Sensor_Service);
        if (btGattService == null) {
            Log.d(TAG,"BLE Service fail!!");
            return;
        }

        BluetoothGattCharacteristic mCharacteristic;
        mCharacteristic = btGattService.getCharacteristic(Sensor_Read_Heart_Rate_Raw);
        setCharacteristicNotification(mCharacteristic,true);

        BluetoothGattDescriptor descriptor = mCharacteristic.getDescriptor(
                UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
//        SoftwareDelay(1000);
//        mCharacteristic = btGattService.getCharacteristic(Sensor_Read_HandT);
//        setCharacteristicNotification(mCharacteristic,true);
    }

    public void SoftwareDelay(int sen){
        try{
            Thread.sleep(sen);
        } catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    public static int byteArrayToInt(byte[] b) {
        return   b[0] & 0xFF |
                (b[1] & 0xFF) << 8 |
                (b[2] & 0xFF) << 16 |
                (b[3] & 0xFF) << 24;
    }

    public String getHreatRateUUID() {
        return HEART_RATE_MEASUREMENT_UUID.toString();
    }
}
