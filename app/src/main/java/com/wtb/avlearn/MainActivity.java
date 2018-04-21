package com.wtb.avlearn;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.wtb.avlearn.videoCapture.VideoCaptureActivity;
import com.wtb.avlearn.videoCapture.VideoCaptureActivity2;
import com.wtb.permission.BasePermissionActivity;
import com.wtb.permission.PermissionRequired;

@PermissionRequired(permession = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO})
public class MainActivity extends BasePermissionActivity {
    Button mCamera1;
    Button mCamera1_textureview;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        mCamera1 = (Button) findViewById(R.id.camera1);
        mCamera1_textureview = (Button) findViewById(R.id.camera1_texture);
        mCamera1_textureview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, VideoCaptureActivity2.class));
            }
        });
        mCamera1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, VideoCaptureActivity.class));
            }
        });
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
