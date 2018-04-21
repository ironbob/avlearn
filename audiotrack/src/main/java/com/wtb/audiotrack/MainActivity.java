package com.wtb.audiotrack;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.wtb.permission.BasePermissionActivity;
import com.wtb.permission.PermissionRequired;

@PermissionRequired(permession = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO})
public class MainActivity extends BasePermissionActivity implements View.OnClickListener {
    EditText mFilePath;
    Button mPlayBtn;
    Button mPauseBtn;
    Button mStopBtn;
    Button mResumeBtn;
    AudioTrackPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFilePath = findViewById(R.id.file_path);
        mPlayBtn = findViewById(R.id.play_btn);
        mStopBtn = findViewById(R.id.stop_btn);
        mPauseBtn = findViewById(R.id.pause_btn);
        mResumeBtn = findViewById(R.id.resume_btn);
        mPlayBtn.setOnClickListener(this);
        mStopBtn.setOnClickListener(this);
        mPauseBtn.setOnClickListener(this);
        mResumeBtn.setOnClickListener(this);
        player = new AudioTrackPlayer();
    }

    @Override
    public void onClick(View v) {
        if (mPlayBtn == v) {
            String filePath = mFilePath.getText().toString();
            if (TextUtils.isEmpty(filePath)) {
                filePath = "/sdcard/zzz/testav1.pcm";
            }
            player.play(filePath);

        } else if (mStopBtn == v) {
            player.stop();
        } else if (v == mPauseBtn) {
            player.pause();
        } else if (v == mResumeBtn) {
            player.resume();
        }
    }
}
