package com.ambiq.ble.ambiqota;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.ambiq.ble.ambiqota.Common.SysApplication;
import com.ambiq.ble.ambiqota.OTA.ScanDeviceActivity;
import com.ambiq.ble.ambiqota.Services.BluetoothLeService;

import java.text.SimpleDateFormat;

/**
 * Created by pofenglin on 2017/10/30.
 */

public class SplashScreenActivity extends Activity {
    private final static String TAG = SplashScreenActivity.class.getSimpleName();
    private BluetoothLeService mBluetoothLeService;
    private BluetoothAdapter mBluetoothAdapter;
    Intent gattServiceIntent;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_QUIT_APP = 2;
    boolean bBTStatus = true;
    boolean bFirstFlag = false;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            SysApplication.getInstance().addBluetoothLeService(mBluetoothLeService);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
            SysApplication.getInstance().addBluetoothLeService(null);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "[Zen add] splash onCreate!");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //--Add activity to list
        SysApplication.getInstance().addActivity(this);

        //--Version Number
        String versionName = null;
        try {
            versionName = SplashScreenActivity.this.getPackageManager()
                    .getPackageInfo(SplashScreenActivity.this.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        TextView tvVersionName = (TextView) this.findViewById(R.id.textView_Splash_VersionName);

        try {
            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm");
            String date = sDateFormat.format(new java.util.Date());

            tvVersionName.setText("Version " + versionName + " , " + date);

        } catch(Exception e) {
        }

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "[Zen add] splash onResume!");
        super.onResume();

        if (bFirstFlag) {
            return;
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            //if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                return;
            //}
        }

        startService (new Intent(this,BluetoothLeService.class));
        gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        //--Showing splash and Start Main Activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(SysApplication.getInstance().mBluetoothLeService == null)
                {
                    Log.d(getLocalClassName(), "BluetoothLe Service is null");
                    return;
                }
                startMainActivity(true);
            }
        }, 3000);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)	{
        Log.d(TAG, "[Zen add] splash onActivityResult!" + "[requestCode]:" + requestCode + "[resultCode]:" + resultCode);
        switch (resultCode) {
            case RESULT_FIRST_USER:

                break;
            case RESULT_OK:
                if (requestCode == REQUEST_ENABLE_BT) {
                    bBTStatus = true;
                    startService(new Intent(this, BluetoothLeService.class));
                    gattServiceIntent = new Intent(this, BluetoothLeService.class);
                    bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                    //--Showing splash and Start Main Activity
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            if (SysApplication.getInstance().mBluetoothLeService == null) {
                                Log.d(getLocalClassName(), "BluetoothLe Service is null");
                                return;
                            }
                            startMainActivity(true);
                        }
                    }, 3000);
                }
//                else if (requestCode == REQUEST_QUIT_APP) {
//                    Log.d(getLocalClassName(), "Quit from ScanDeviceActivity!!");
//                    finish();
//                }
                break;
            case RESULT_CANCELED:
                bFirstFlag = true;
                if (requestCode == REQUEST_QUIT_APP) {
                    Log.d(getLocalClassName(), "Quit from ScanDeviceActivity!!");
                    finish();
                } else {
                    bBTStatus = false;
                    //need to check if need to do something
                    finish();
                }
//                bFirstFlag = true;
//
//                startService (new Intent(this,BluetoothLeService.class));
//                gattServiceIntent = new Intent(this, BluetoothLeService.class);
//                bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
//                //--Showing splash and Start Main Activity
//                new Handler().postDelayed(new Runnable() {
//
//                    @Override
//                    public void run() {
//
//                        if(SysApplication.getInstance().mBluetoothLeService == null)
//                        {
//                            Log.d(getLocalClassName(), "BluetoothLe Service is null");
//                            return;
//                        }
//                        startMainActivity(false);
//                    }
//                }, 3000);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "splash onDestroy.");
        System.exit(0);
//        if (bBTStatus) {
//            Log.d(TAG, "splash close.");
//            unbindService(mServiceConnection);
//        }
    }

    private void startMainActivity(boolean BTStatus) {
        Intent intent = new Intent();
        //intent.setClass(this, DashboardActivity.class);
        //intent.setClass(this,TestFunctionActivity.class);
        if (BTStatus) {
            intent.setClass(this, ScanDeviceActivity.class);
        }
        //this.startActivity(intent);
        this.startActivityForResult(intent, REQUEST_QUIT_APP);
    }
}
