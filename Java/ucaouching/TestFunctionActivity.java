package com.ambiq.ble.ambiqota.TB;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;

import com.ambiq.ble.ambiqota.Common.DrawView;
import com.ambiq.ble.ambiqota.R;

/**
 * Created by pofenglin on 2017/11/1.
 */

public class TestFunctionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testfunction);

        //GPSManager g = new GPSManager(this);
        //AccelerometerManager a = new AccelerometerManager(this);

        LinearLayout layout=(LinearLayout) findViewById(R.id.chart_container);
        final DrawView view=new DrawView(this);
        view.setBackgroundColor(Color.BLACK);
        layout.addView(view);

        int [] histogram_value = {1,2,3,4,6};
        view.drawHistogram(histogram_value);
    }

}
