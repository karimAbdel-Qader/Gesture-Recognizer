package com.karim.gesturerecognizer;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class Recording extends AppCompatActivity {

    private TextView textCounter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);
        textCounter = findViewById(R.id.textCounter);
        new CountDownTimer(4000,1000){

            @Override
            public void onTick(long l) {
                textCounter.setText(""+l/1000);
            }

            @Override
            public void onFinish() {
                this.cancel();
                Intent recordingActivity = new Intent(Recording.this,Recorder.class);
                startActivity(recordingActivity);
            }

    }.start();

}

}
