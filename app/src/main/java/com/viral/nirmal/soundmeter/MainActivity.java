package com.viral.nirmal.soundmeter;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private final int RC_PERMISSION_RECORD_AUDIO = 1001;
    private TextView tvDecibels;
    private MediaRecorder mediaRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvDecibels = findViewById(R.id.tv_decibel);
        mediaRecorder = null;
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        checkPermissionAndReadAudioInput();
    }

    @Override
    protected void onStop() {
        super.onStop();
        decibelCalculateTimer.cancel();
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mediaRecorder = null;
        }
    }

    private void checkPermissionAndReadAudioInput() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            analyzeAudioInput();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RC_PERMISSION_RECORD_AUDIO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RC_PERMISSION_RECORD_AUDIO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermissionAndReadAudioInput();
                } else {
                    Toast.makeText(this, "Need permission to use MicroPhone", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void analyzeAudioInput() {
        if (mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile("/dev/null/");

            try {
                mediaRecorder.prepare();
            } catch (IllegalStateException | IOException e) {
                e.printStackTrace();
            }
            mediaRecorder.start();
        }

        decibelCalculateTimer.start();
    }

    private double getAmplitude() {
        if (mediaRecorder == null) {
            return 0;
        } else {
            return (mediaRecorder.getMaxAmplitude() / 2700.0);
        }
    }

    private double getDecibles() {
        if (mediaRecorder == null) {
            return 0;
        } else {
            return (20 * Math.log10(getAmplitude() / 32767.0));
        }
    }

    private CountDownTimer decibelCalculateTimer = new CountDownTimer(2000, 2000) {
        @Override
        public void onTick(long millisUntilFinished) {
            tvDecibels.setText(getString(R.string.decibel_formatted_text, String.valueOf(getDecibles())));
        }

        @Override
        public void onFinish() {
            decibelCalculateTimer.start();
        }

    };

}
