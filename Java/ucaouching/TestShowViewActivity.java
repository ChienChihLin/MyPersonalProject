package com.ambiq.ble.ambiqota.TB;

import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.ambiq.ble.ambiqota.R;
import com.ambiq.ble.ambiqota.Services.BluetoothLeService;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by asus on 2017/12/30.
 */
public class TestShowViewActivity extends AppCompatActivity {

    private static String TAG = TestShowViewActivity.class.getSimpleName();
    private EditText hrmView, humidityView, tempView, uvView, gView;
    private Button returnButton;
    public PathView pathView;
    public static int[] hrRawDataArray = new int[1250];
    public static int dataIndex = 0;
    private BluetoothLeService mBluetoothLeService = TestBLEActivity.mBluetoothLeService;
    private BluetoothGattService btGattService = BluetoothLeService.pubBluetoothGatt.getService(BluetoothLeService.Sensor_Service);

    public static String BLEGetSensorData = "BLE_GET_SENSOR_DATA";
    public static String RecvDataStatus = "RECV_DATA_STATUS";
    public static String BLEGetNotify = "BLE_GET_Notify";
    public static String UV = "";
    public static String HT = "";
    public static String HR = "";
    public static String Humidity = "";
    public static String Temp = "";
    /*set switch parameters*/
    private Button SetSwitch;

    public CheckBox hrm_sw;
    public CheckBox uv_sw;
    public CheckBox ht_sw;
    public CheckBox acc_sw;

//    public static boolean b_htButtonStatus = false;
//    public static boolean b_uvButtonStatus = false;
//    public static boolean b_hrmButtonStatus = false;
//    public static boolean b_gButtonStatus = false;

    public Timer readHtTimer;
    public Timer readUvTimer;
    public Timer readHrmTimer;
    public Timer readGTimer;

    boolean curHrmCheckBoxStatus = false, lastHrmCheckBoxStatus = false;
    boolean curUVCheckBoxStatus = false, lastUVCheckBoxStatus = false;
    boolean curHTCheckBoxStatus = false, lastHTCheckBoxStatus = false;
    boolean curACCCheckBoxStatus = false, lastACCCheckBoxStatus = false;

    public static float temprature;
    public static float humidity;
    public static float uvlevel;

//    private CircleProgress mTempProgress = new CircleProgress(this, null);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testble_data_view);

        initUI();
        registerBroadcastReceiver();
        //RelativeLayout layout = (RelativeLayout) findViewById(R.id.chart_container);
        //pathView = new PathView(this, null, 0);

        //GPSManager g = new GPSManager(this);
        //AccelerometerManager a = new AccelerometerManager(this);

//        LinearLayout layout=(LinearLayout) findViewById(R.id.chart_container);
//        final DrawView view=new DrawView(this);
//        view.setBackgroundColor(Color.BLACK);
//        layout.addView(view);
//
//        int [] histogram_value = {1,2,3,4,6};
//        view.drawHistogram(histogram_value);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        if (readHtTimer != null) {
            readHtTimer.cancel();
        }

        if (readUvTimer != null) {
            readUvTimer.cancel();
        }
    }

    private void initUI() {
        hrmView = (EditText) findViewById(R.id.HrmView);
        hrmView.setText("60");
        hrmView.setTextColor(Color.RED);
        hrmView.setFocusable(false);
        hrmView.setFocusableInTouchMode(false);

//        humidityView = (EditText) findViewById(R.id.HumidityView);
//        humidityView.setText("0");
//        humidityView.setTextColor(Color.RED);
//        humidityView.setFocusable(false);
//        humidityView.setFocusableInTouchMode(false);

//        tempView = (EditText) findViewById(R.id.TempView);
//        tempView.setText("0");
//        tempView.setTextColor(Color.RED);
//        tempView.setFocusable(false);
//        tempView.setFocusableInTouchMode(false);

//        uvView = (EditText) findViewById(R.id.UvView);
//        uvView.setText("0");
//        uvView.setTextColor(Color.RED);
//        uvView.setFocusable(false);
//        uvView.setFocusableInTouchMode(false);

//        gView = (EditText) findViewById(R.id.GView);
//        gView.setText("0");
//        gView.setTextColor(Color.RED);
//        gView.setFocusable(false);
//        gView.setFocusableInTouchMode(false);

        returnButton = (Button) findViewById(R.id.button_view_return);
        returnButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
            // TODO Auto-generated method stub
                finish();
            }
        });

//        HR_log_view.setTextColor(Color.RED);
//        HR_log_view.setBackgroundColor(Color.DKGRAY);
//        //HR_log_view.setFocusable(false);
//        //HR_log_view.setFocusableInTouchMode(false);
        SetSwitch = (Button) findViewById(R.id.button_set_switch);
        SetSwitch.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(final View v) {
                // TODO Auto-generated method stub

                AlertDialog.Builder AlertDialog = new AlertDialog.Builder(TestShowViewActivity.this);
                LayoutInflater adbInflater = LayoutInflater.from(TestShowViewActivity.this);
                View eulaLayout = adbInflater.inflate(R.layout.testble_alert, null);
                hrm_sw = (CheckBox) eulaLayout.findViewById(R.id.hrm_sw);
                uv_sw = (CheckBox) eulaLayout.findViewById(R.id.uv_sw);
                ht_sw = (CheckBox) eulaLayout.findViewById(R.id.ht_sw);
                acc_sw = (CheckBox) eulaLayout.findViewById(R.id.acc_sw);

//                sw1.setOnCheckedChangeListener(listener);
//                sw2.setOnCheckedChangeListener(listener);
//                sw3.setOnCheckedChangeListener(listener);
//                sw4.setOnCheckedChangeListener(listener);
                AlertDialog.setView(eulaLayout);

                checkSW();

                AlertDialog.setTitle("Switch");
                AlertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        curHrmCheckBoxStatus = hrm_sw.isChecked();
                        curUVCheckBoxStatus = uv_sw.isChecked();
                        curHTCheckBoxStatus = ht_sw.isChecked();
                        curACCCheckBoxStatus = acc_sw.isChecked();
                        // Zen mod, start
                        if (TestBLEActivity.connect == true) {
                            if (curHrmCheckBoxStatus != lastHrmCheckBoxStatus) {
                                Log.d(TAG, "[Zen add] Hrm checkbox changed!!!");
                                mBluetoothLeService.startSetting(0, curHrmCheckBoxStatus);
                                mBluetoothLeService.SoftwareDelay(500);
                                if (curHrmCheckBoxStatus == true) {
                                    if (btGattService == null) {
                                        Log.d(TAG,"BLE Service fail!!");
                                        return;
                                    }

//                                    BluetoothGattCharacteristic mCharacteristic;
//                                    mCharacteristic = btGattService.getCharacteristic(BluetoothLeService.Sensor_Read_Heart_Rate_Raw);
//                                    mBluetoothLeService.setCharacteristicNotification(mCharacteristic, true);

                                    //mBluetoothLeService.startReadHR(true);
                                    //mBluetoothLeService.SoftwareDelay(500);
                                    mBluetoothLeService.startReadHrRaw(true);
                                } else {
                                    if (btGattService == null) {
                                        Log.d(TAG,"BLE Service fail!!");
                                        return;
                                    }

//                                    BluetoothGattCharacteristic mCharacteristic;
//                                    mCharacteristic = btGattService.getCharacteristic(BluetoothLeService.Sensor_Read_Heart_Rate_Raw);
//                                    mBluetoothLeService.setCharacteristicNotification(mCharacteristic, false);
                                    //mBluetoothLeService.startReadHR(false);
                                    //mBluetoothLeService.SoftwareDelay(500);
                                    mBluetoothLeService.startReadHrRaw(false);
                                }
                            }
                            if (curUVCheckBoxStatus != lastUVCheckBoxStatus) {
                                Log.d(TAG, "[Zen add] Hrm checkbox changed!!!");
                                mBluetoothLeService.startSetting(1, curUVCheckBoxStatus);
                                mBluetoothLeService.SoftwareDelay(500);
                                if (curUVCheckBoxStatus == true) {
                                    readUvTimer = new Timer(true);
                                    readUvTimer.schedule(new ReadUVDataTask(), 500, 3000);
                                } else {
                                    readUvTimer.cancel();
                                }
                            }
                            if (curHTCheckBoxStatus != lastHTCheckBoxStatus) {
                                Log.d(TAG, "[Zen add] Hrm checkbox changed!!!");
                                mBluetoothLeService.startSetting(2, curHTCheckBoxStatus);
                                mBluetoothLeService.SoftwareDelay(500);
                                if (curHTCheckBoxStatus == true) {
                                    readHtTimer = new Timer(true);
                                    readHtTimer.schedule(new ReadHTDataTask(), 500, 3000);
                                } else {
                                    readHtTimer.cancel();
                                }
                            }
                            if (curACCCheckBoxStatus != lastACCCheckBoxStatus) {
                                Log.d(TAG, "[Zen add] Hrm checkbox changed!!!");
                                mBluetoothLeService.startSetting(3, curACCCheckBoxStatus);
                                mBluetoothLeService.SoftwareDelay(500);
                            }
                            lastHrmCheckBoxStatus = curHrmCheckBoxStatus;
                            lastUVCheckBoxStatus = curUVCheckBoxStatus;
                            lastHTCheckBoxStatus = curHTCheckBoxStatus;
                            lastACCCheckBoxStatus = curACCCheckBoxStatus;
                        }
//                        mBluetoothLeService.startSetting(0, swFalg1);
//                        mBluetoothLeService.SoftwareDelay(1000);
//                        mBluetoothLeService.startSetting(1,swFalg2);
//                        mBluetoothLeService.SoftwareDelay(1000);
//                        mBluetoothLeService.startSetting(2, swFalg3);
//                        mBluetoothLeService.SoftwareDelay(1000);
//                        mBluetoothLeService.startSetting(3,swFalg4);
                        // Zen mod, end
                        return;
                    } });
                AlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    } });
                AlertDialog alert;
                alert = AlertDialog.create();
                alert.show();
                /*
                if (!t){
                    mBluetoothLeService.startSetting(1);
                    t = true;
                }else{
                    mBluetoothLeService.startSetting(0);
                    t = false;
                }
                */
            }
        });
    }

    TestShowViewActivity.Server_Broadcast receiver = new TestShowViewActivity.Server_Broadcast();

    private void registerBroadcastReceiver() {
//        IntentFilter filter = new IntentFilter(TestBLEActivity.BLEConnectStatus);
//        registerReceiver(receiver,filter);
        IntentFilter filter = new IntentFilter(BLEGetSensorData);
        registerReceiver(receiver,filter);
//        filter = new IntentFilter(TestBLEActivity.BLEGetNotify);
//        registerReceiver(receiver,filter);
        filter = new IntentFilter(BLEGetNotify);
        registerReceiver(receiver,filter);
    }

    public class Server_Broadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Float tmp_f;
            Log.d(TAG, "Server_Broadcast get sensor data");
            if (action.equals(BLEGetSensorData)) {
                Bundle bundle = intent.getExtras();
                String str = bundle.getString("sensor");
                Log.d(TAG, "get sensor data:" + str);
                if (str.equals("UV")) {
//                    uvView.setText(TestBLEActivity.UV);
                    uvlevel = Float.parseFloat(UV);
//                    // add notification for UV progress view
//                    Intent view_intent = new Intent();
//                    Bundle view_bundle = new Bundle();
//                    view_bundle.putString("view_data", "UV");
//                    view_intent.putExtras(bundle);
//                    view_intent.setAction(RecvDataStatus);
//                    sendBroadcast(intent);
                } else if (str.equals("HT")) {
//                    humidityView.setText(TestBLEActivity.Humidity);
                    //tempView.setText(TestBLEActivity.Temp);
                    temprature = Float.parseFloat(Temp);
                    humidity = Float.parseFloat(Humidity);
                    // add notification for HT progress view
//                    Intent view_intent = new Intent();
//                    Bundle view_bundle = new Bundle();
//                    view_bundle.putString("view_data", "HT");
//                    view_intent.putExtras(bundle);
//                    view_intent.setAction(RecvDataStatus);
//                    sendBroadcast(intent);
//                    mTempProgress.setValue(tmp_f);
//                    Float tmp_f = Float.parseFloat(TestBLEActivity.Humidity);
//                    humidity = tmp_f;
                } else if (str.equals("HR")) {
                    hrmView.setText(HR);
                }
            } else if (action.equals(BLEGetNotify)) {
                Bundle bundle = intent.getExtras();
                //String str = bundle.getString(mBluetoothLeService.EXTRA_DATA);
                String str = bundle.getString("setnotify");
                Log.d(TAG, "set notify:" + str);
                if (str.equals("HR")) {
                    hrmView.setText(HR);
                }
                //pathView.setData(hrRawDataArray);
                //HR_log_view.setText(str+"\n"+HR_log_view.getText());
            }
        }
    }

    private void checkSW() {
        if (lastHrmCheckBoxStatus) {
            hrm_sw.setChecked(true);
        } else {
            hrm_sw.setChecked(false);
        }
        if (lastUVCheckBoxStatus) {
            uv_sw.setChecked(true);
        } else{
            uv_sw.setChecked(false);
        }
        if (lastHTCheckBoxStatus) {
            ht_sw.setChecked(true);
        } else {
            ht_sw.setChecked(false);
        }
        if (lastACCCheckBoxStatus) {
            acc_sw.setChecked(true);
        } else {
            acc_sw.setChecked(false);
        }
    }

    public class ReadHTDataTask extends TimerTask {
        public void run()
        {
                if (TestBLEActivity.connect) {
                    mBluetoothLeService.startReadHT();
                } else {
//                    HR_log_view.setText("please connect ble device first.\n"+HR_log_view.getText());
                }
        }
    }

    public class ReadUVDataTask extends TimerTask {
        public void run()
        {
            if (TestBLEActivity.connect) {
                mBluetoothLeService.startReadUV();
            } else {
//                    HR_log_view.setText("please connect ble device first.\n"+HR_log_view.getText());
            }
        }
    }

    class HRUpdateThread implements Runnable {
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                //scrollBy(1,0);
                if (TestShowViewActivity.temprature != 0.0f) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //setValue(TestShowViewActivity.temprature);
                        }
                    });
                }
            }
        }
    }
}
