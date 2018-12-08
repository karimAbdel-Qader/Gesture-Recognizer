package com.karim.gesturerecognizer;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import umich.cse.yctung.androidlibsvm.LibSVM;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private static final String TAG = "MainActivity";
    private HandlerThread mSensorThread;
    private Handler mSensorHandler;
    private GesturesDataAdapter mAdapter;
    private SensorManager sensorManager;
    private TextToSpeech myTTS;
    private Sensor accelerometer, gyroscope;
    public SpeechRecognizer mySpeechRecognizer;
    private EditText input;
    private List<String> fileList = new ArrayList<String>();
    private AlertDialog.Builder alertAdd,alertStart,alertRecording,alertDone;
    private ArrayList <Float> acXData = new ArrayList<Float>();
    private ArrayList <Float> acYData = new ArrayList<Float>();
    private ArrayList <Float> acZData = new ArrayList<Float>();
    private ArrayList <Float> gXData = new ArrayList<Float>();
    private ArrayList <Float> gYData = new ArrayList<Float>();
    private ArrayList <Float> gZData = new ArrayList<Float>();
    private AlertDialog alert;
    private String x = "testing";
    SwipeController swipeController = null;
    private boolean flag = false;
    private long tStart = 0L;
    private long tEnd = 0L;
    private long tDelta ;
    private double elapsedSeconds ;
    private LibSVM svm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        svm = new LibSVM();
        ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},1);
        textToSpeech();
        speechRecognizer();
        File root = new File("/storage/emulated/0/Gestures/");
        setGestureDataAdapter(root);
        setupRecyclerView();
        Log.d(TAG, "External storage: " + Environment.getExternalStorageState());
/////

        File sdCard = Environment.getExternalStorageDirectory();

        /*svm.scale("-l -1 -u 1 "+sdCard.getAbsolutePath() +  "/Gestures/AG.data", sdCard.getAbsolutePath() + "/Gestures/ac_scaled.data");
        Log.d(TAG, "cWriter: scaled");

        svm.train("-t 2 -g 0.000000372 "  + sdCard.getAbsolutePath() + "/Gestures/ac_scaled.data " + sdCard.getAbsolutePath() + "/Gestures/modelsvm");
        Log.d(TAG, "cWriter: trained");

        svm.scale("-l -1 -u 1 "+ sdCard.getAbsolutePath() +  "/Gestures/Test.data", sdCard.getAbsolutePath() + "/Gestures/c_scaled");
        svm.predict(sdCard.getAbsolutePath() + "/Gestures/c_scaled " + sdCard.getAbsolutePath() + "/Gestures/modelsvm " + sdCard.getAbsolutePath() + "/Gestures/resultsvmC");
        Log.d(TAG, "cWriter: circle prediction accuracy");*/
        //////
    }

    private void textToSpeech() {
        myTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(myTTS.getEngines().size() == 0){
                    return;
                }else{
                    myTTS.setLanguage(Locale.US);

                }
            }
        });
    }

    private void speak(String message) {
        myTTS.speak(message, TextToSpeech.QUEUE_FLUSH,null,null);
    }

    private void speechRecognizer() {
        if(SpeechRecognizer.isRecognitionAvailable(this)){
            mySpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            mySpeechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle bundle) {
                    Log.d(TAG, "onReadyForSpeech: ready yaaaaah");

                }

                @Override
                public void onBeginningOfSpeech() {
                   // Log.d(TAG, "onBeginningOfSpeech: begun yaaaaah");

                }

                @Override
                public void onRmsChanged(float v) {

                }

                @Override
                public void onBufferReceived(byte[] bytes) {

                }

                @Override
                public void onEndOfSpeech() {

                }

                @Override
                public void onError(int i) {



                }

                @Override
                public void onResults(Bundle bundle) {
                    List<String> results = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    //if ((results.get(0).equalsIgnoreCase("start")||results.get(0).equalsIgnoreCase("stop"))) {
                        Log.d(TAG, "onResults: " + results.get(0));
                        processResults(results.get(0));
                }

                @Override
                public void onPartialResults(Bundle bundle) {

                }

                @Override
                public void onEvent(int i, Bundle bundle) {

                }
            });
        }

    }

    private void processResults (String command){
        command = command.toLowerCase();
        if((command.indexOf("start") != -1|| command.indexOf("stored") != -1 || command.indexOf("stock") != -1|| command.indexOf("stork") != -1)
                && flag == false){
            tStart = System.currentTimeMillis();
            alert.dismiss();
            mySpeechRecognizer.stopListening();
            flag = true;
            alertRecording = new AlertDialog.Builder(this);
            alertRecording.setTitle("Recording...");
            alertRecording.setMessage("Say Stop to stop recording");
            alert = alertRecording.create();
            alert.show();
            listen();
            record();
        }

        if(command.indexOf("stop") != -1 && flag == true){
            mSensorThread.quitSafely();
            sensorManager.unregisterListener(this);
            mySpeechRecognizer.stopListening();
            alert.dismiss();
            flag = false;
            alertDone = new AlertDialog.Builder(this);
            alertDone.setTitle("Done");
            alertDone.setMessage("Your record has been saved");
            alertDone.show();
            speak("Your record has been saved");
            writeHelper();
            //span_Exclusive spans cannot have a zero length

        }
    }

    private void writeHelper() {
        String aData="0";
        int index = 1;

        for(int i = 0; i < acXData.size(); i++){
        aData =aData + " " + index + ":" +acXData.get(i);
        index ++;
        }
        for(int i = 0; i < acYData.size(); i++){
            aData =aData + " " + index + ":" +acYData.get(i);
            index ++;
        }
        for(int i = 0; i < acZData.size(); i++){
            aData =aData + " " + index + ":" +acZData.get(i);
            index++;
        }
        for(int i = 0; i < gXData.size(); i++){
            aData =aData + " " + index + ":" +gXData.get(i);
            index++;
        }
        for(int i = 0; i < gYData.size(); i++){
            aData =aData + " " + index + ":" +gYData.get(i);
            index++;
        }
        for(int i = 0; i < gZData.size(); i++){
            aData =aData + " " + index + ":" +gZData.get(i);
            index++;
        }
        acXData.clear();
        acYData.clear();
        acZData.clear();
        gXData.clear();
        gYData.clear();
        gZData.clear();
        aData = aData + "\n";
        if(input==null)
            cWriter("testing.data",aData);
        else {
            if (input.getText() + "" == "")
                cWriter("testing.data", aData);
            else
                cWriter(input.getText() + ".data", aData);
        }
    }


    public void setGestureDataAdapter (File f){
        List<Gesture> gestures = new ArrayList<>();
        if(f.listFiles()!= null) {
            File[] files = f.listFiles();

        fileList.clear();
        if (files.length!=0)
        for(File file : files){
            Gesture gesture = new Gesture(file.getPath().substring(29),x);
            gestures.add(gesture);
        }
        mAdapter = new GesturesDataAdapter(gestures);
    }}

    private void setupRecyclerView() {
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mAdapter);

        swipeController = new SwipeController(new SwipeControllerActions() {
            @Override
            public void onRightClicked(int position) {
                mAdapter.gestures.remove(position);
                mAdapter.notifyItemRemoved(position);
                mAdapter.notifyItemRangeChanged(position, mAdapter.getItemCount());
            }
        });

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(recyclerView);

        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });
    }


    private void alertDialog() {
        alertAdd.setTitle("Add a new gesture");
        input = new EditText(this);
        alertAdd.setView(input);
        alertStart = new AlertDialog.Builder(this);
        alertAdd.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog2();
                return;
            }
        });
        alertAdd.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                return;
            }
        });
        alertAdd.show();
    }

    private void alertDialog2() {
        alertStart.setTitle("Start");
        alertStart.setMessage("Say Start to begin recording the gesture");
        alert = alertStart.create();
        listen();
        alert.show();
    }

    public void listen(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
        intent.putExtra("android.speech.extra.DICTATION_MODE",true);
        mySpeechRecognizer.startListening(intent);
    }

    private void record() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            //Log.d(TAG, "permission granted");
        Log.d(TAG, "onCreate: Initializing sensor services");

        mSensorThread = new HandlerThread("Sensor thread", Thread.MAX_PRIORITY);
        mSensorThread.start();
        mSensorHandler = new Handler(mSensorThread.getLooper());
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(MainActivity.this, accelerometer, 30000,mSensorHandler);
            Log.d(TAG, "onCreate: Registered accelerometer sensor");
        }
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (gyroscope != null) {
            sensorManager.registerListener(MainActivity.this, gyroscope, 30000,mSensorHandler);
            Log.d(TAG, "onCreate: Registered gyroscope sensor");
        }
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        tEnd = System.currentTimeMillis();
        tDelta = tEnd - tStart;
        elapsedSeconds = tDelta;
        Sensor sensor = sensorEvent.sensor;
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            //Log.d(TAG, "permission granted");
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                acXData.add(sensorEvent.values[0]);
                acYData.add(sensorEvent.values[1]);
                acZData.add(sensorEvent.values[2]);
            }
            if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                gXData.add(sensorEvent.values[0]);
                gYData.add(sensorEvent.values[1]);
                gZData.add(sensorEvent.values[2]);
            }

        }
    }

    public void cWriter (String fileName, String entry){

        try {

            File sdCard = Environment.getExternalStorageDirectory();
            File dir;
            if(input==null)
            dir = new File(sdCard.getAbsolutePath() + "/Gestures/Testing");
            else
                dir = new File(sdCard.getAbsolutePath() + "/Gestures" + "/" + input.getText());
            Boolean dirsMade = dir.mkdir();
            //Log.d(TAG, "filer: " + dirsMade.toString());

            File file = new File(dir, fileName);
            FileOutputStream f = new FileOutputStream(file,true);

            /*svm.scale(sdCard.getAbsolutePath() +  "/Gestures/acc.data", sdCard.getAbsolutePath() + "/Gestures/heart_scale_scaled");
            Log.d(TAG, "cWriter: scaled");
            svm.train("-t 2 "*//* svm kernel *//* + sdCard.getAbsolutePath() + "/Gestures/heart_scale_scaled " + sdCard.getAbsolutePath() + "/Gestures/modelsvm");
            Log.d(TAG, "cWriter: trained");
            svm.predict(sdCard.getAbsolutePath() + "/Gestures/acc.data " + sdCard.getAbsolutePath() + "/Gestures/modelsvm" + sdCard.getAbsolutePath() + "/Gestures/resultsvm");
            Log.d(TAG, "cWriter: predicted");*/



            try {
                f.write(entry.getBytes());
                f.flush();
                f.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        }
        //svmTest();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {


    }

    protected void onPause() {
        super.onPause();

    }

    public void add(View view) {
        svmTest();
        /*alertAdd = new AlertDialog.Builder(this);
        alertDialog();*/
    }

    protected void onDestroy(){
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    public void test(View view) {
        alertStart = new AlertDialog.Builder(this);
        alertDialog2();
    }

    private void svmTest() {

        File sdCard = Environment.getExternalStorageDirectory();

        svm.scale("-l -1 -u 1 "+sdCard.getAbsolutePath() +  "/Gestures/AG.data", sdCard.getAbsolutePath() + "/Gestures/Testing/ag_scaled.data");
        svm.scale("-l -1 -u 1 "+ sdCard.getAbsolutePath() +  "/Gestures/Testing/testing.data", sdCard.getAbsolutePath() + "/Gestures/Testing/T_scaled");
        Log.d(TAG, "cWriter: scaled");

        svm.train("-t 0 "  + sdCard.getAbsolutePath() + "/Gestures/Testing/ag_scaled.data " + sdCard.getAbsolutePath() + "/Gestures/Testing/modelsvm");
        Log.d(TAG, "cWriter: trained");

        svm.predict(sdCard.getAbsolutePath() + "/Gestures/Testing/T_scaled " + sdCard.getAbsolutePath() + "/Gestures/Testing/modelsvm " + sdCard.getAbsolutePath() + "/Gestures/Testing/resultsvmC");
        Log.d(TAG, "cWriter: circle prediction accuracy");
    }
}