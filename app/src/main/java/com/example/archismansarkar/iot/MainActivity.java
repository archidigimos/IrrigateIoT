package com.example.archismansarkar.iot;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketConnectionHandler;
import de.tavendo.autobahn.WebSocketException;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class MainActivity extends AppCompatActivity {

    private final WebSocketConnection mConnection = new WebSocketConnection();
    private boolean webSocketConnected = false;

    private String[] parsed_data;
    private int sizeArr = 0;
    private String hub = "";
    private String device = "";

    private final Handler mHandler = new Handler();
    private Runnable mTimer;

    private LineGraphSeries<DataPoint> popSeries;
    private LineGraphSeries<DataPoint> flowSeries;
    private LineGraphSeries<DataPoint> tempSeries;
    private LineGraphSeries<DataPoint> moistureSeries;
    private LineGraphSeries<DataPoint> humiditySeries;
    private LineGraphSeries<DataPoint> computedSeries;

    private double graphLastXValue = 0d;

    private EditText cloudAd, hubAd, nodeAd, controlData, thresHold;
    private Button setOrsend;

    private String cloudAddress = "";
    private String hubAddress = "";
    private String nodeAddress = "";
    private String dataControl = "";
    private String thresholdSet = "";

    private double threshold = 200.0;
    private double temp = 0.0;

    SharedPreferences pref; // 0 - for private mode
    public static final String cloudAdS = "cloudAdSt";
    public static final String hubAdS = "hubAdSt";
    public static final String nodeAdS = "nodeAdSt";
    public static final String controlDataS = "controlDataSt";
    public static final String thresHoldS = "thresHoldSt";
    private Boolean exit = false;
    private Boolean thread_Start = false;

    private Handler handler = new Handler();
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GraphView popGraph = (GraphView) findViewById(R.id.pop);
        popSeries = new LineGraphSeries<>();
        popGraph.setTitle("Probability of Precipitation");
        popGraph.setTitleColor(Color.BLUE);
        popGraph.addSeries(popSeries);popGraph.getViewport().setXAxisBoundsManual(true);popGraph.getViewport().setMinX(0);popGraph.getViewport().setMaxX(100);popGraph.getViewport().setScalable(true);popGraph.getViewport().setScalableY(true);

        GraphView flowGraph = (GraphView) findViewById(R.id.flow);
        flowSeries = new LineGraphSeries<>();
        flowGraph.setTitle("Flowrate");
        flowGraph.setTitleColor(Color.BLUE);
        flowGraph.addSeries(flowSeries);flowGraph.getViewport().setXAxisBoundsManual(true);flowGraph.getViewport().setMinX(0);flowGraph.getViewport().setMaxX(100);flowGraph.getViewport().setScalable(true);flowGraph.getViewport().setScalableY(true);

        GraphView tempGraph = (GraphView) findViewById(R.id.temperature);
        tempSeries = new LineGraphSeries<>();
        tempGraph.setTitle("Temperature");
        tempGraph.setTitleColor(Color.BLUE);
        tempGraph.addSeries(tempSeries);tempGraph.getViewport().setXAxisBoundsManual(true);tempGraph.getViewport().setMinX(0);tempGraph.getViewport().setMaxX(100);tempGraph.getViewport().setScalable(true);tempGraph.getViewport().setScalableY(true);

        GraphView moistureGraph = (GraphView) findViewById(R.id.moisture);
        moistureSeries = new LineGraphSeries<>();
        moistureGraph.setTitle("Moisture");
        moistureGraph.setTitleColor(Color.BLUE);
        moistureGraph.addSeries(moistureSeries);moistureGraph.getViewport().setXAxisBoundsManual(true);moistureGraph.getViewport().setMinX(0);moistureGraph.getViewport().setMaxX(100);moistureGraph.getViewport().setScalable(true);moistureGraph.getViewport().setScalableY(true);

        GraphView humidityGraph = (GraphView) findViewById(R.id.humidity);
        humiditySeries = new LineGraphSeries<>();
        humidityGraph.setTitle("Humidity");
        humidityGraph.setTitleColor(Color.BLUE);
        humidityGraph.addSeries(humiditySeries);humidityGraph.getViewport().setXAxisBoundsManual(true);humidityGraph.getViewport().setMinX(0);humidityGraph.getViewport().setMaxX(100);humidityGraph.getViewport().setScalable(true);humidityGraph.getViewport().setScalableY(true);

        GraphView computedGraph = (GraphView) findViewById(R.id.Resultant);
        computedSeries = new LineGraphSeries<>();
        computedGraph.setTitle("Resultant");
        computedGraph.setTitleColor(Color.RED);
        computedGraph.addSeries(computedSeries);computedGraph.getViewport().setXAxisBoundsManual(true);computedGraph.getViewport().setMinX(0);computedGraph.getViewport().setMaxX(100);computedGraph.getViewport().setScalable(true);computedGraph.getViewport().setScalableY(true);

        cloudAd = (EditText) findViewById(R.id.cloudAddress);
        hubAd = (EditText) findViewById(R.id.hubAddress);
        nodeAd = (EditText) findViewById(R.id.nodeAddress);
        controlData = (EditText) findViewById(R.id.data);
        thresHold = (EditText) findViewById(R.id.threshold);

        setOrsend = (Button) findViewById(R.id.scrd);
        //setOrsend.setText("Connect");
        //setOrsend.setTextColor(Color.WHITE);
        //setOrsend.setBackgroundColor(Color.RED);

        pref = getApplicationContext().getSharedPreferences("MyPref", 0);

        if (pref.contains(cloudAdS)) {
            cloudAd.setText(pref.getString(cloudAdS, ""));
            connectToServer(pref.getString(cloudAdS, "10.124.195.9:9001"),setOrsend);
        }
        if (pref.contains(hubAdS)) {
            hubAd.setText(pref.getString(hubAdS, ""));
        }
        if (pref.contains(nodeAdS)) {
            nodeAd.setText(pref.getString(nodeAdS, ""));
        }
        if (pref.contains(controlDataS)) {
            controlData.setText(pref.getString(controlDataS, ""));
        }
        if (pref.contains(thresHoldS)) {
            thresHold.setText(pref.getString(thresHoldS, ""));
        }

        setOrsend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cloudAddress = cloudAd.getText().toString();
                hubAddress = hubAd.getText().toString();
                nodeAddress = nodeAd.getText().toString();
                dataControl = controlData.getText().toString();
                thresholdSet = thresHold.getText().toString();
                threshold = Double.parseDouble(thresholdSet);

                SharedPreferences.Editor editor = pref.edit();
                editor.putString(cloudAdS,cloudAddress);
                editor.putString(hubAdS,hubAddress);
                editor.putString(nodeAdS,nodeAddress);
                editor.putString(controlDataS,dataControl);
                editor.putString(thresHoldS,thresholdSet);

                editor.commit();

                connectToServer(cloudAddress,setOrsend);
                if(!(hubAddress.equals(""))){
                    if(!(nodeAddress.equals(""))){

                        if(webSocketConnected){
                            mConnection.sendTextMessage("USERDATA-"+hubAddress+"-"+nodeAddress+"-THRESHOLD"+"-"+thresholdSet);
                            mConnection.sendTextMessage("USERDATA-"+hubAddress+"-"+nodeAddress+"-"+dataControl);
                        }
                    }
                    else{
                        Toast msgNode = Toast.makeText(getBaseContext(),"Please enter a valid Node address!!!",Toast.LENGTH_LONG);
                        msgNode.show();
                    }
                }
                else{
                    Toast msgHub = Toast.makeText(getBaseContext(),"Please enter a valid Hub address!!!",Toast.LENGTH_LONG);
                    msgHub.show();
                }
            }
        });
    }
    @Override
    public void onBackPressed() {
        if (exit) {
            handler.removeCallbacks(runnable);
            mConnection.disconnect();
            finish();
            System.exit(0);
        } else {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }

    }
    public double computeData(double[] arr, int index){
        int length = arr.length;
        if(index==length)return 0.0;
        else return arr[index]+computeData(arr,index+1);
    }

    public void connectToServer(final String wsuri,final Button b) {

        String cloudAddressLink = "ws://";
        cloudAddressLink = cloudAddressLink + wsuri;
        if(!webSocketConnected) {
            b.setText("Connecting...");
            b.setBackgroundColor(Color.WHITE);
            b.setTextColor(Color.RED);
            Toast msgNode = Toast.makeText(getBaseContext(),"Connecting to cloud Server",Toast.LENGTH_LONG);
            msgNode.show();
            runnable = new Runnable() {
                public void run() {
                    Toast msgNode = Toast.makeText(getBaseContext(),"Unable to connect the Cloud Server. Check cloud address and retry",Toast.LENGTH_LONG);
                    msgNode.show();
                    if(thread_Start==true) {
                        b.setText("Connect");
                        b.setTextColor(Color.WHITE);
                        b.setBackgroundColor(Color.RED);
                        thread_Start=false;
                        handler.removeCallbacks(runnable);
                    }
                    else {
                        thread_Start = true;
                        handler.postDelayed(this, 5000);
                    }
                }
            };
            runnable.run();
        }
        try {
            mConnection.connect(cloudAddressLink, new WebSocketConnectionHandler() {

                @Override
                public void onOpen() {
                    Toast msgNode = Toast.makeText(getBaseContext(),"Connected succesfully to Cloud Server",Toast.LENGTH_LONG);
                    msgNode.show();
                    webSocketConnected = true;
                    mConnection.sendTextMessage("ADMIN");
                    handler.removeCallbacks(runnable);
                    b.setText("Send Command / Receive Data");
                    b.setTextColor(Color.WHITE);
                    b.setBackgroundColor(Color.BLUE);
                }

                @Override
                public void onTextMessage(String payload) {
                    parsed_data = payload.split("-");
                    sizeArr = parsed_data.length;
                    double[] data = new double[sizeArr-4];
                    if(parsed_data[0].equals("DATA")){
                        hub = parsed_data[1];
                        device = parsed_data[2];
                        if(hub.equals(hubAddress)) {
                            if(device.equals(nodeAddress)) {
                                for (int i = 4; i < sizeArr; i++)
                                    data[i - 4] = Double.parseDouble(parsed_data[i]);
                                graphLastXValue += 1d;
                                //graphLastXValue = Double.parseDouble(parsed_data[3]);
                                if (data.length == 6) {
                                    popSeries.appendData(new DataPoint(graphLastXValue, data[4]), true, 100);
                                    flowSeries.appendData(new DataPoint(graphLastXValue, data[0]), true, 100);
                                    tempSeries.appendData(new DataPoint(graphLastXValue, data[1]), true, 100);
                                    moistureSeries.appendData(new DataPoint(graphLastXValue, data[2]), true, 100);
                                    humiditySeries.appendData(new DataPoint(graphLastXValue, data[3]), true, 100);
                                    //temp = computeData(data,0);
                                    temp= data[5];
                                    computedSeries.appendData(new DataPoint(graphLastXValue, temp), true, 100);
                                    if(temp>threshold)computedSeries.setColor(Color.RED);
                                    else computedSeries.setColor(Color.GREEN);

                                }
                            }
                        }
                    }

                }

                @Override
                public void onClose(int code, String reason) {
                    Toast msgNode = Toast.makeText(getBaseContext(),"Connection to the Cloud Server has been closed",Toast.LENGTH_LONG);
                    msgNode.show();
                    webSocketConnected = false;
                    b.setText("Connect");
                    b.setTextColor(Color.WHITE);
                    b.setBackgroundColor(Color.RED);
                }
            });
        } catch (WebSocketException e) {

        }
    }
}
