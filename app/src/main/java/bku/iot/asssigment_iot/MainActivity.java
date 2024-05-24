package bku.iot.asssigment_iot;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.MarkerType;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.Stroke;
import com.anychart.APIlib;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import android.widget.Toast;
import android.content.Context;

import android.app.Activity;
import android.os.Bundle;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class MainActivity extends AppCompatActivity {
    MQTTHelper mqttHelper;
    Button button;
    int i = 0;
    private static final Random RANDOM = new Random();
    private LineGraphSeries<DataPoint> series, series1;
    private int lastX = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        GraphView graph = findViewById(R.id.temperature_linear_chart);
        // data
        series = new LineGraphSeries<DataPoint>();
        series.setTitle("Temperature");
        series.setColor(Color.RED);
        series.setDrawDataPoints(true);
        series.setDrawBackground(true);
        graph.addSeries(series);
        // customize a little bit viewport
        Viewport viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(10);
        viewport.setScrollable(true);
        viewport.setBorderColor(100);
        viewport.setBackgroundColor(Color.argb(40, 255,64, 25));

        GraphView graph1 = findViewById(R.id.humidity_linear_chart);
        // data
        series1 = new LineGraphSeries<DataPoint>();
        series1.setTitle("Humidity");
        series1.setColor(Color.rgb(0,191,255));
        series1.setDrawDataPoints(true);
        series1.setDrawBackground(true);
        graph1.addSeries(series1);
        // customize a little bit viewport
        Viewport viewport1 = graph1.getViewport();
        viewport1.setYAxisBoundsManual(true);
        viewport1.setMinY(0);
        viewport1.setMaxY(10);
        viewport1.setScrollable(true);
        viewport1.setBorderColor(100);
        viewport1.setBackgroundColor(Color.argb(40, 77,136,255));

        button = findViewById(R.id.button);
        if(button != null){
            button.setOnClickListener((View.OnClickListener)(new View.OnClickListener() {
                public final void onClick(View it) {

                    // displaying a toast message
                    addEntry();
                }
            }));
        }
    }

    public void sendDataMQTT(String topic, String value){
        MqttMessage msg = new MqttMessage();
        msg.setId(1234);
        msg.setQos(0);
        msg.setRetained(false);

        byte[] b = value.getBytes(Charset.forName("UTF-8"));
        msg.setPayload(b);

        try {
            mqttHelper.mqttAndroidClient.publish(topic, msg);
        }catch(MqttException e){
        }
    }
    public void startMQTT(){
        mqttHelper = new MQTTHelper(this);
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Calendar calendar = Calendar.getInstance();
                Date date = calendar.getTime();
                Log.d("TEST", topic + " " + message.toString());
                if(topic.contains("cambien1")){
//                    txtTemp.setText(message.toString() + "°C");
                    series.appendData(new DataPoint(Integer.parseInt(date.toString()), Integer.parseInt(message.toString())), true, 30);
                }else if(topic.contains("cambien2")) {
//                    txtHumi.setText(message.toString() + "%");
                    series1.appendData(new DataPoint(Integer.parseInt(date.toString()), Integer.parseInt(message.toString())), true, 30);
                }else if(topic.contains("cambien3")){
//                    txtLux.setText(message.toString() + "lux");
                }else if(topic.contains("nutnhan1")){
                    if(message.toString().equals("1")){
//                        btnLED.setOn(true);
                    }else{
//                        btnLED.setOn(false);
                    }
                }else if(topic.contains("nutnhan2")){
                    if(message.toString().equals("1")){
//                        btnPUMP.setOn(true);
                    }else{
//                        btnPUMP.setOn(false);
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Thread(new Runnable() {

            @Override
            public void run() {
                // we add 100 new entries
                for (int i = 0; i < 100; i++) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
//                            addEntry();
                        }
                    });

                    // sleep to slow down the add of entries
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // manage error ...
                    }
                }
            }
        }).start();
    }
    private void addEntry() {
        // here, we choose to display max 10 points on the viewport and we scroll to end
        series.appendData(new DataPoint(lastX++, RANDOM.nextDouble() * 10d), true, 30);
        series1.appendData(new DataPoint(lastX++, RANDOM.nextDouble() * 10d), true, 30);
    }
}