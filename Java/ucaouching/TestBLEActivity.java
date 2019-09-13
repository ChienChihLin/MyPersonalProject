package com.ambiq.ble.ambiqota.TB;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.ambiq.ble.ambiqota.Common.SysApplication;
import com.ambiq.ble.ambiqota.MyLocation.MyMapsActivity;
import com.ambiq.ble.ambiqota.MyNote.MyNoteActivity;
import com.ambiq.ble.ambiqota.OTA.DeviceInfoActivity;
import com.ambiq.ble.ambiqota.R;
import com.ambiq.ble.ambiqota.Services.BluetoothLeService;

/**
 * Created by pofenglin on 2017/10/30.
 */

public class TestBLEActivity extends AppCompatActivity {
    private static String TAG = TestBLEActivity.class.getSimpleName();

    public static BluetoothDevice mBluetoothDevice = null;
    public static String blestatus = "Disconnect";

    public static BluetoothLeService mBluetoothLeService = null;
    private MenuItem ble_status_item;

//    private EditText HR_log_view;
    private Button Connect;
    private Button ReadUV;
    private Button ReadHT;
    private Button SetNotify;
    private Button GotoOTA;
//    private Button SetSwitch;
    private Button ShowView;
    private Button MyDailyNote;
    private Button MyLocation;

//    private ImageView BleConnectedView;

//    public CheckBox hrm_sw;
//    public CheckBox uv_sw;
//    public CheckBox ht_sw;
//    public CheckBox acc_sw;

//    public static boolean b_htButtonStatus = false;
//    public static boolean b_uvButtonStatus = false;
//    public static boolean b_hrmButtonStatus = false;
//    public static boolean b_gButtonStatus = false;
//
//    public Timer readHtTimer;
//    public Timer readUvTimer;
//    public Timer readHrmTimer;
//    public Timer readGTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testble);
        //getWindow().setBackgroundDrawableResource(R.drawable.background);

        initUI();

        mBluetoothLeService = SysApplication.getInstance().mBluetoothLeService;
        //registerBroadcastReceiver();

        if (mBluetoothDevice != null) {
            Log.d(TAG,"mBluetoothDevice Name :"+mBluetoothDevice.getName());
            Log.d(TAG,"mBluetoothDevice Address :"+mBluetoothDevice.getAddress());
//            HR_log_view.setText("mBluetoothDevice Name :"+mBluetoothDevice.getName()+"\n"+HR_log_view.getText());
//            HR_log_view.setText("mBluetoothDevice Address :"+mBluetoothDevice.getAddress()+"\n"+HR_log_view.getText());
        } else {
            Log.d(TAG,"mBluetoothDevice is null");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.testble_menu, menu);

        // 取得選單項目物件
        ble_status_item = menu.findItem(R.id.ble_status_item);
        ble_status_item.setVisible(false);

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        Connect.setTextColor(Color.parseColor("#888888"));
        registerBroadcastReceiver();
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mBluetoothDevice.getAddress());
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void initUI() {
//        HR_log_view = (EditText) findViewById(R.id.LogView);
//        HR_log_view.setTextColor(Color.RED);
//        HR_log_view.setBackgroundColor(Color.DKGRAY);
//        HR_log_view.setText("");
//        HR_log_view.setFocusable(false);
//        HR_log_view.setFocusableInTouchMode(false);

        Connect = (Button) findViewById(R.id.button_connect_ble_device);
        Connect.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (!connect) {
                    mBluetoothLeService.connect(mBluetoothDevice.getAddress());
                    connect = true;
                    Connect.setText("DISCONNECT BLE DEVICE");
                    Connect.setTextColor(Color.parseColor("#888888"));
                    Connect.setClickable(false);
                } else {
                    mBluetoothLeService.disconnect();
                    connect = false;
                    Connect.setText("DISCONNECT BLE DEVICE");
                    Connect.setTextColor(Color.parseColor("#000000"));
                    Connect.setClickable(true);
                }
            }
        });

//        ReadUV = (Button) findViewById(R.id.button_read_uv);
//        ReadUV.setOnClickListener(new Button.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                if (connect) {
//                    mBluetoothLeService.startReadUV();
//                } else {
////                    HR_log_view.setText("please connect ble device first.\n"+HR_log_view.getText());
//                }
//            }
//        });

//        ReadHT = (Button) findViewById(R.id.button_read_ht);
//        ReadHT.setOnClickListener(new Button.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
////                if (connect){
////                    mBluetoothLeService.startReadHT();
////                }else{
////                    HR_log_view.setText("please connect ble device first.\n"+HR_log_view.getText());
////                }
//                if (connect) {
//                    b_htButtonStatus = true;
//                    readHtTimer = new Timer(true);
//                    readHtTimer.schedule(new ReadSensorDataTask(), 500, 3000);
//                } else {
////                    HR_log_view.setText("please connect ble device first.\n"+HR_log_view.getText());
//                }
//            }
//        });

//        SetNotify = (Button) findViewById(R.id.button_read_hrm_raw);
//        SetNotify.setOnClickListener(new Button.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                mBluetoothLeService.setNotify();
//            }
//        });

        GotoOTA = (Button) findViewById(R.id.go_to_ota);
        GotoOTA.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent mainIntent = new Intent(TestBLEActivity.this, DeviceInfoActivity.class);
                DeviceInfoActivity.mBluetoothDevice = mBluetoothDevice;
                TestBLEActivity.this.startActivity(mainIntent);
                //mBluetoothLeService.startReadHR();
            }
        });

//        SetSwitch = (Button) findViewById(R.id.button_set_switch);
//        SetSwitch.setOnClickListener(new Button.OnClickListener(){
//            @Override
//            public void onClick(final View v) {
//                // TODO Auto-generated method stub
//
//                AlertDialog.Builder AlertDialog = new AlertDialog.Builder(TestBLEActivity.this);
//                LayoutInflater adbInflater = LayoutInflater.from(TestBLEActivity.this);
//                View eulaLayout = adbInflater.inflate(R.layout.testble_alert, null);
//                hrm_sw = (CheckBox) eulaLayout.findViewById(R.id.hrm_sw);
//                uv_sw = (CheckBox) eulaLayout.findViewById(R.id.uv_sw);
//                ht_sw = (CheckBox) eulaLayout.findViewById(R.id.ht_sw);
//                acc_sw = (CheckBox) eulaLayout.findViewById(R.id.acc_sw);
//
////                sw1.setOnCheckedChangeListener(listener);
////                sw2.setOnCheckedChangeListener(listener);
////                sw3.setOnCheckedChangeListener(listener);
////                sw4.setOnCheckedChangeListener(listener);
//                AlertDialog.setView(eulaLayout);
//
//                checkSW();
//
//                AlertDialog.setTitle("Switch");
//                AlertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        curHrmCheckBoxStatus = hrm_sw.isChecked();
//                        curUVCheckBoxStatus = uv_sw.isChecked();
//                        curHTCheckBoxStatus = ht_sw.isChecked();
//                        curACCCheckBoxStatus = acc_sw.isChecked();
//                // Zen mod, start
//                        if (curHrmCheckBoxStatus != lastHrmCheckBoxStatus) {
//                            Log.d(TAG, "[Zen add] Hrm checkbox changed!!!");
//                            mBluetoothLeService.startSetting(0, curHrmCheckBoxStatus);
//                            mBluetoothLeService.SoftwareDelay(500);
//                        }
//                        if (curUVCheckBoxStatus != lastUVCheckBoxStatus) {
//                            Log.d(TAG, "[Zen add] Hrm checkbox changed!!!");
//                            mBluetoothLeService.startSetting(1, curUVCheckBoxStatus);
//                            mBluetoothLeService.SoftwareDelay(500);
//                        }
//                        if (curHTCheckBoxStatus != lastHTCheckBoxStatus) {
//                            Log.d(TAG, "[Zen add] Hrm checkbox changed!!!");
//                            mBluetoothLeService.startSetting(2, curHTCheckBoxStatus);
//                            mBluetoothLeService.SoftwareDelay(500);
//                        }
//                        if (curACCCheckBoxStatus != lastACCCheckBoxStatus) {
//                            Log.d(TAG, "[Zen add] Hrm checkbox changed!!!");
//                            mBluetoothLeService.startSetting(3, curACCCheckBoxStatus);
//                            mBluetoothLeService.SoftwareDelay(500);
//                        }
//                        lastHrmCheckBoxStatus = curHrmCheckBoxStatus;
//                        lastUVCheckBoxStatus = curUVCheckBoxStatus;
//                        lastHTCheckBoxStatus = curHTCheckBoxStatus;
//                        lastACCCheckBoxStatus = curACCCheckBoxStatus;
////                        mBluetoothLeService.startSetting(0, swFalg1);
////                        mBluetoothLeService.SoftwareDelay(1000);
////                        mBluetoothLeService.startSetting(1,swFalg2);
////                        mBluetoothLeService.SoftwareDelay(1000);
////                        mBluetoothLeService.startSetting(2, swFalg3);
////                        mBluetoothLeService.SoftwareDelay(1000);
////                        mBluetoothLeService.startSetting(3,swFalg4);
//                // Zen mod, end
//                        return;
//                    } });
//                AlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        return;
//                    } });
//                AlertDialog alert;
//                alert = AlertDialog.create();
//                alert.show();
//                /*
//                if (!t){
//                    mBluetoothLeService.startSetting(1);
//                    t = true;
//                }else{
//                    mBluetoothLeService.startSetting(0);
//                    t = false;
//                }
//                */
//            }
//        });

        // Zen mod, add button to start view show activity
        ShowView = (Button) findViewById(R.id.view_data);
        ShowView.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent viewIntent = new Intent(TestBLEActivity.this, TestShowViewActivity.class);
                TestBLEActivity.this.startActivityForResult(viewIntent, 1);
            }
        });

        MyDailyNote = (Button) findViewById(R.id.daily_note);
        MyDailyNote.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent viewIntent = new Intent(TestBLEActivity.this, MyNoteActivity.class);
                TestBLEActivity.this.startActivityForResult(viewIntent, 1);
            }
        });

        MyLocation = (Button) findViewById(R.id.show_location);
        MyLocation.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent viewIntent = new Intent(TestBLEActivity.this, MyMapsActivity.class);
                TestBLEActivity.this.startActivityForResult(viewIntent, 1);
            }
        });
//        BleConnectedView = (ImageView) findViewById(R.id.ble_connected_view);
//        BleConnectedView.setVisibility(View.INVISIBLE);
//        ble_status_item.setVisible(false);
    }

    private CheckBox.OnCheckedChangeListener listener = new CheckBox.OnCheckedChangeListener()
    {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
//            if (aa){
//                swFalg1 = sw1.isChecked();
//                swFalg2 = sw2.isChecked();
//                swFalg3 = sw3.isChecked();
//                swFalg4 = sw4.isChecked();
//            }
        }
    };

//    private void checkSW() {
//        if (lastHrmCheckBoxStatus) {
//            hrm_sw.setChecked(true);
//        } else {
//            hrm_sw.setChecked(false);
//        }
//        if (lastUVCheckBoxStatus) {
//            uv_sw.setChecked(true);
//        } else{
//            uv_sw.setChecked(false);
//        }
//        if (lastHTCheckBoxStatus) {
//            ht_sw.setChecked(true);
//        } else {
//            ht_sw.setChecked(false);
//        }
//        if (lastACCCheckBoxStatus) {
//            acc_sw.setChecked(true);
//        } else {
//            acc_sw.setChecked(false);
//        }
//    }

    public static boolean connect = false;
    boolean t = false;

    boolean aa = false;
    boolean swFalg1 = false;
    boolean swFalg2 = false;
    boolean swFalg3 = false;
    boolean swFalg4 = false;

//    boolean curHrmCheckBoxStatus = false, lastHrmCheckBoxStatus = false;
//    boolean curUVCheckBoxStatus = false, lastUVCheckBoxStatus = false;
//    boolean curHTCheckBoxStatus = false, lastHTCheckBoxStatus = false;
//    boolean curACCCheckBoxStatus = false, lastACCCheckBoxStatus = false;
    /**********************************************************************************************/
    // Server Broadcast
    public static String BLEConnectStatus = "BLE_CONNECT_STATUS";
//    public static String BLEGetSensorData = "BLE_GET_SENSOR_DATA";
//    public static String BLEGetNotify = "BLE_GET_Notify";
    TestBLEActivity.Server_Broadcast receiver = new TestBLEActivity.Server_Broadcast();

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter(BluetoothLeService.ACTION_GATT_CONNECTED);
        registerReceiver(receiver,filter);
        filter = new IntentFilter(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        registerReceiver(receiver,filter);
        filter = new IntentFilter(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        registerReceiver(receiver,filter);
//        filter = new IntentFilter(BLEGetSensorData);
//        registerReceiver(receiver,filter);
//        filter = new IntentFilter(BLEGetNotify);
//        registerReceiver(receiver,filter);
    }

    public class Server_Broadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothLeService.ACTION_GATT_CONNECTED)) {
                if (mBluetoothLeService.mConnectionState == BluetoothLeService.STATE_CONNECTED) {
//                    HR_log_view.setText("BLE device connect!! \n" + HR_log_view.getText());
                    connect = true;
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Connect.setText("DISCONNECT BLE DEVICE");
                            Connect.setTextColor(Color.parseColor("#000000"));
                            Connect.setClickable(true);
//                            BleConnectedView.setVisibility(View.VISIBLE);
//                            ble_status_item.setVisible(true);
                            if (ble_status_item != null) {
                                ble_status_item.setVisible(true);
                            }
                        }
                    });
                }
            } else if (action.equals(BluetoothLeService.ACTION_GATT_DISCONNECTED)) {
                if (mBluetoothLeService.mConnectionState == BluetoothLeService.STATE_DISCONNECTED) {
//                    HR_log_view.setText("BLE device disconnect!! \n" + HR_log_view.getText());
                    connect = false;
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Connect.setText("CONNECT BLE DEVICE");
                            Connect.setTextColor(Color.parseColor("#000000"));
                            Connect.setClickable(true);
//                            BleConnectedView.setVisibility(View.INVISIBLE);
//                            ble_status_item.setVisible(false);
                            if (ble_status_item != null) {
                                ble_status_item.setVisible(false);
                            }
                        }
                    });
                }
            } else if (action.equals(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)) {
                Connect.setTextColor(Color.parseColor("#000000"));
                Connect.setClickable(true);
            }
//            if (action.equals(BLEConnectStatus)) {
//                Bundle bundle = intent.getExtras();
//                String str = bundle.getString("status");
//                Log.d(TAG,"get ble status:"+str);
//                HR_log_view.setText("BLE device connect? :"+blestatus+"\n"+HR_log_view.getText());
//                if (blestatus.equals("Connected")) {
//                    connect = true;
//                    runOnUiThread(new Runnable() {
//                        public void run()
//                        {
//                            Connect.setText("DISCONNECT BLE DEVICE");
//                        }
//                    });
//                } else if (blestatus.equals("Disconnected")) {
//                    mBluetoothLeService.disconnect();
//                    connect = false;
//                    runOnUiThread(new Runnable() {
//                        public void run()
//                        {
//                            Connect.setText("CONNECT BLE DEVICE");
//                        }
//                    });
//                }
//            }
//            else if(action.equals(BLEGetSensorData)){
//                Bundle bundle = intent.getExtras();
//                String str = bundle.getString("sensor");
//                Log.d(TAG,"get sensor data:"+str);
//                if (str.equals("UV")){
//                    HR_log_view.setText("UV :"+UV+" % \n"+HR_log_view.getText());
//                }else if (str.equals("HT")){
//                    HR_log_view.setText("HT :"+HT+"\n"+HR_log_view.getText());
//                }else if (str.equals("HR")){
//                    HR_log_view.setText("HR :"+HR+" bmp\n"+HR_log_view.getText());
//                }
//            }else if (action.equals(BLEGetNotify)){
//                Bundle bundle = intent.getExtras();
//                String str = bundle.getString("setnotify");
//                Log.d(TAG,"set notify:"+str);
//                HR_log_view.setText(str+"\n"+HR_log_view.getText());
//            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        HR_log_view.setText("requestCode:" + requestCode + "resultCode:" + resultCode + "\n" + HR_log_view.getText());
        //Log.d(TAG,"requestCode:" + requestCode + "resultCode:" + resultCode);
        if (resultCode == Activity.RESULT_CANCELED) {
            if (requestCode == 1) {
                if (!connect) {
                    mBluetoothLeService.connect(mBluetoothDevice.getAddress());
                    connect = true;
                    Connect.setText("CONNECT BLE DEVICE");
                    Connect.setTextColor(Color.parseColor("#000000"));
                    Connect.setClickable(true);
                } else {
                    mBluetoothLeService.disconnect();
                    connect = false;
                    Connect.setText("DISCONNECT BLE DEVICE");
                    Connect.setTextColor(Color.parseColor("#000000"));
                    Connect.setClickable(true);
                }

//                if (b_htButtonStatus) {
////                    HR_log_view.setText("HT Timer Cancel!" + "\n" + HR_log_view.getText());
//                    b_htButtonStatus = false;
//                    readHtTimer.cancel();
//                }
            }
        }
    }

//    public class ReadSensorDataTask extends TimerTask {
//        public void run()
//        {
//            if (b_htButtonStatus) {
//                if (connect) {
//                    mBluetoothLeService.startReadHT();
//                } else {
////                    HR_log_view.setText("please connect ble device first.\n"+HR_log_view.getText());
//                }
//            }
//        }
//    }
}
