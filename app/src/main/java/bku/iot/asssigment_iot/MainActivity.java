package bku.iot.asssigment_iot;

import android.os.Bundle;
import android.util.Log;

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

public class MainActivity extends AppCompatActivity {
    MQTTHelper mqttHelper;
    List<DataEntry> seriesData = new ArrayList<>();
    List<DataEntry> seriesData1 = new ArrayList<>();
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

        ///////////
        AnyChartView tempChartView = findViewById(R.id.temperature_linear_chart);
        APIlib.getInstance().setActiveAnyChartView(tempChartView);

        Cartesian cartesian = AnyChart.line();

        cartesian.animation(true);

        cartesian.padding(10d, 20d, 5d, 20d);

        cartesian.crosshair().enabled(true);
        cartesian.crosshair()
                .yLabel(true)
                // TODO ystroke
                .yStroke((Stroke) null, null, null, (String) null, (String) null);

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);

        cartesian.title("Temperature");

        cartesian.yAxis(0).title("°C");
        cartesian.xAxis(0).labels().padding(5d, 5d, 5d, 5d);

        Set set = Set.instantiate();
        set.data(seriesData);
        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");

        Line series1 = cartesian.line(series1Mapping);
        series1.color("#dc1e1e");
        series1.hovered().markers().enabled(true);
        series1.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);
        series1.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);

        cartesian.legend().enabled(true);
        cartesian.legend().fontSize(13d);
        cartesian.legend().padding(0d, 0d, 10d, 0d);

        tempChartView.setChart(cartesian);
        //=====================================//
        AnyChartView humiChartView = findViewById(R.id.humidity_linear_chart);
        APIlib.getInstance().setActiveAnyChartView(humiChartView);

        Cartesian cartesian1 = AnyChart.line();

        cartesian1.animation(true);

        cartesian1.padding(10d, 20d, 5d, 20d);

        cartesian1.crosshair().enabled(true);
        cartesian1.crosshair()
                .yLabel(true)
                // TODO ystroke
                .yStroke((Stroke) null, null, null, (String) null, (String) null);

        cartesian1.tooltip().positionMode(TooltipPositionMode.POINT);

        cartesian1.title("Humidity");

        cartesian1.yAxis(0).title("%");
        cartesian1.xAxis(0).labels().padding(5d, 5d, 5d, 5d);

        Set set1 = Set.instantiate();
        set1.data(seriesData1);
        Mapping series2Mapping = set1.mapAs("{ x: 'x', value: 'value' }");

        Line series2 = cartesian1.line(series2Mapping);
//        series1.name("Brandy");
        series2.color("#0a31cf");
        series2.hovered().markers().enabled(true);
        series2.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);
        series2.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);

        cartesian1.legend().enabled(true);
        cartesian1.legend().fontSize(13d);
        cartesian1.legend().padding(0d, 0d, 10d, 0d);

        humiChartView.setChart(cartesian1);
        startMQTT();
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
                    seriesData.add(new ValueDataEntry(date.toString(), Integer.parseInt(message.toString())));
                }else if(topic.contains("cambien2")) {
//                    txtHumi.setText(message.toString() + "%");
                    seriesData1.add(new ValueDataEntry(date.toString(), Integer.parseInt(message.toString())));
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
}