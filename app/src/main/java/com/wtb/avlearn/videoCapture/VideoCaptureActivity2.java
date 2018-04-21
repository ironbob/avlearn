package com.wtb.avlearn.videoCapture;

import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import com.wtb.avlearn.R;
import com.wtb.permission.BasePermissionActivity;

import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Created by baobaowang on 2018/4/21.
 * camera+TextureView
 */
public class VideoCaptureActivity2 extends BasePermissionActivity implements TextureView.SurfaceTextureListener, Camera.AutoFocusCallback {
    private static final String TAG = "VideoCaptureActivity";
    private boolean bIfPreview;
    private int mPreviewWidth;
    private int mPreviewHeight;
    private Button mFocusBtn;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_capture2);
        mFocusBtn = findViewById(R.id.focus);
        mFocusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.autoFocus(VideoCaptureActivity2.this);
            }
        });
        initSurfaceView();
    }

    // 定义对象
    private TextureView textureView = null;  // SurfaceView对象：(视图组件)视频显示
    private Camera mCamera = null;     // Camera对象，相机预览

    private void initSurfaceView() {
        textureView = (TextureView) this.findViewById(R.id.texture_view);
        textureView.setSurfaceTextureListener(this);
    }

    private void configCamera() {
        Log.i(TAG, "going into initCamera");
        if (bIfPreview) {
            mCamera.stopPreview();//stopCamera();
        }
        if (null != mCamera) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setPictureFormat(ImageFormat.JPEG); //Sets the image format for picture 设定相片格式为JPEG，默认为NV21
                parameters.setPreviewFormat(ImageFormat.NV21); //Sets the image format for preview picture，默认为NV21
                /*【ImageFormat】JPEG/NV16(YCrCb format，used for Video)/NV21(YCrCb format，used for Image)/RGB_565/YUY2/YU12*/
                // 【调试】获取caera支持的PictrueSize，看看能否设置？？
//                List<Camera.Size> pictureSizes = mCamera.getParameters().getSupportedPictureSizes();
//                List<Camera.Size> previewSizes = mCamera.getParameters().getSupportedPreviewSizes();
//                List<Integer> previewFormats = mCamera.getParameters().getSupportedPreviewFormats();
//                List<Integer> previewFrameRates = mCamera.getParameters().getSupportedPreviewFrameRates();
                List<String> focusModes = mCamera.getParameters().getSupportedFocusModes();
                if (focusModes.contains("continuous-video")) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                }
                // 设置拍照和预览图片大小，预览有毛病设置这里
                parameters.setPictureSize(1280, 720); //指定拍照图片的大小
                parameters.setPreviewSize(1280, 720); // 指定preview的大小
                //这两个属性 如果这两个属性设置的和真实手机的不一样时，就会报错
                // 横竖屏镜头自动调整
                if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                    parameters.set("orientation", "portrait"); //
                    parameters.set("rotation", 90); // 镜头角度转90度（默认摄像头是横拍）
                    mCamera.setDisplayOrientation(90); // 在2.2以上可以使用
                } else// 如果是横屏
                {
                    parameters.set("orientation", "landscape"); //
                    mCamera.setDisplayOrientation(0); // 在2.2以上可以使用
                }
                // 设定配置参数并开启预览
                mCamera.setParameters(parameters); // 将Camera.Parameters设定予Camera
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startPreview() {
        mCamera.startPreview(); // 打开预览画面
        bIfPreview = true;

    }

    byte[] mPreviewData;

    @Override
    public void onAutoFocus(boolean success, Camera camera) {

    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        // TODO Auto-generated method stub
        mCamera = Camera.open();// 开启摄像头（2.3版本后支持多摄像头,需传入参数）
        try {
            mCamera.setPreviewTexture(surface);//set the surface to be used for live preview
            configCamera();
            startPreview();
        } catch (Exception ex) {
            if (null != mCamera) {
                mCamera.release();
                mCamera = null;
            }
            Log.i(TAG, "initCamera" + ex.getMessage());
        }

    }


    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        configCamera();
        startPreview();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null); //！！这个必须在前，不然退出出错
            mCamera.stopPreview();
            bIfPreview = false;
            mCamera.release();
            mCamera = null;
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        Log.i(TAG, "onSurfaceTextureUpdated");
    }
}
