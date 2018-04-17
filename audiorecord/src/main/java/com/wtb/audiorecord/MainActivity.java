package com.wtb.audiorecord;

import android.Manifest;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.wtb.permission.BasePermissionActivity;
import com.wtb.permission.PermissionRequired;

@PermissionRequired(permession = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO})
public class MainActivity extends BasePermissionActivity implements View.OnClickListener, PCMDataSaver.Callback {
    EditText mFileName;
    EditText mRecordTime;
    Button mStart;
    Button mStop;
    Button mRelease;
    Button mMakeWav;
    AudioRecorder mRecorder;
    private TextView mDataSize;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                mRecorder.stop();
            } else if (msg.what == 2) {
                long size = (long) msg.obj;
                mDataSize.setText(String.valueOf(size / 1000) +" KB");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("AudioRecord录制pcm音频");
        setContentView(R.layout.activity_main);
        mFileName = findViewById(R.id.file_name);
        mRecordTime = findViewById(R.id.record_time);
        mStart = findViewById(R.id.start_btn);
        mStop = findViewById(R.id.stop_btn);
        mRelease = findViewById(R.id.release_btn);
        mDataSize = findViewById(R.id.data_size);
        mMakeWav = findViewById(R.id.make_wav);
        mStart.setOnClickListener(this);
        mStop.setOnClickListener(this);
        mRelease.setOnClickListener(this);
        mMakeWav.setOnClickListener(this);
        mRecorder = new AudioRecorder(this);
    }

    @Override
    public void onClick(View v) {
        if (v == mStart) {
            String fileName = mFileName.getText().toString();
            if (TextUtils.isEmpty(fileName)) {
                fileName = "/sdcard/testav1.pcm";
            }
            mRecorder.start(fileName);
            String timeStr = mRecordTime.getText().toString();
            int time = 20;
            try {
                time = Integer.parseInt(timeStr);
            }catch (Exception e){

            }
            mHandler.sendEmptyMessageDelayed(1, time * 1000);
        } else if (v == mStop) {
            mRecorder.stop();
        } else if (v == mRelease) {
            mRecorder.release();
        } else if(v == mMakeWav){
            MediaUtils.pcm2wav(44100,16,1,0,"/sdcard/testav1.pcm","/sdcard/testav1.wav");
        }
    }

    private long mLastRecordTime;

    @Override
    public void onWrite(long length, long totalWritted) {
        long current = System.currentTimeMillis();
        if (current - mLastRecordTime > 500) {
            Message msg = Message.obtain();
            msg.what = 2;
            msg.obj = totalWritted;
            mHandler.sendMessage(msg);
            mLastRecordTime = current;
        }
    }
}
