package com.wtb.avlearn.videoCapture;

import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
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
import java.util.concurrent.locks.Lock;

/**
 * Created by baobaowang on 2018/4/21.
 */
public class VideoCaptureActivity extends BasePermissionActivity implements SurfaceHolder.Callback, Camera.AutoFocusCallback {
    private static final String TAG = "VideoCaptureActivity";
    private boolean bIfPreview;
    private int mPreviewWidth;
    private int mPreviewHeight;
    private Button mFocusBtn;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_capture);
        mFocusBtn = findViewById(R.id.focus);
        mFocusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.autoFocus(VideoCaptureActivity.this);
            }
        });
        initSurfaceView();
    }

    // 定义对象
    private SurfaceView mSurfaceview = null;  // SurfaceView对象：(视图组件)视频显示
    private SurfaceHolder mSurfaceHolder = null;  // SurfaceHolder对象：(抽象接口)SurfaceView支持类
    private Camera mCamera = null;     // Camera对象，相机预览

    // InitSurfaceView
    private void initSurfaceView() {
        mSurfaceview = (SurfaceView) this.findViewById(R.id.surface_view);
        mSurfaceHolder = mSurfaceview.getHolder(); // 绑定SurfaceView，取得SurfaceHolder对象
        mSurfaceHolder.addCallback(VideoCaptureActivity.this); // SurfaceHolder加入回调接口
        // mSurfaceHolder.setFixedSize(176, 144); // 预览大小設置
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// 設置顯示器類型，setType必须设置
    }

    /*【SurfaceHolder.Callback 回调函数】*/
    public void surfaceCreated(SurfaceHolder holder)
    // SurfaceView启动时/初次实例化，预览界面被创建时，该方法被调用。
    {
        // TODO Auto-generated method stub
        mCamera = Camera.open();// 开启摄像头（2.3版本后支持多摄像头,需传入参数）
        try {
            Log.i(TAG, "SurfaceHolder.Callback：surface Created");
            mCamera.setPreviewDisplay(mSurfaceHolder);//set the surface to be used for live preview

        } catch (Exception ex) {
            if (null != mCamera) {
                mCamera.release();
                mCamera = null;
            }
            Log.i(TAG, "initCamera" + ex.getMessage());
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    // 当SurfaceView/预览界面的格式和大小发生改变时，该方法被调用
    {
        // TODO Auto-generated method stub
        Log.i(TAG, "SurfaceHolder.Callback：Surface Changed");
        mPreviewHeight = height;
        mPreviewWidth = width;
        initCamera();
    }

    public void surfaceDestroyed(SurfaceHolder holder)
    // SurfaceView销毁时，该方法被调用
    {
        // TODO Auto-generated method stub
        Log.i(TAG, "SurfaceHolder.Callback：Surface Destroyed");
        if (null != mCamera) {
            mCamera.setPreviewCallback(null); //！！这个必须在前，不然退出出错
            mCamera.stopPreview();
            bIfPreview = false;
            mCamera.release();
            mCamera = null;
        }
    }

    /*【2】【相机预览】*/
    private void initCamera()//surfaceChanged中调用
    {
        Log.i(TAG, "going into initCamera");
        if (bIfPreview) {
            mCamera.stopPreview();//stopCamera();
        }
        if (null != mCamera) {
            try {
                /* Camera Service settings*/
                Camera.Parameters parameters = mCamera.getParameters();
                // parameters.setFlashMode("off"); // 无闪光灯
                parameters.setPictureFormat(ImageFormat.JPEG); //Sets the image format for picture 设定相片格式为JPEG，默认为NV21
                parameters.setPreviewFormat(ImageFormat.NV21); //Sets the image format for preview picture，默认为NV21
                /*【ImageFormat】JPEG/NV16(YCrCb format，used for Video)/NV21(YCrCb format，used for Image)/RGB_565/YUY2/YU12*/

                // 【调试】获取caera支持的PictrueSize，看看能否设置？？
                List<Camera.Size> pictureSizes = mCamera.getParameters().getSupportedPictureSizes();
                List<Camera.Size> previewSizes = mCamera.getParameters().getSupportedPreviewSizes();
                List<Integer> previewFormats = mCamera.getParameters().getSupportedPreviewFormats();
                List<Integer> previewFrameRates = mCamera.getParameters().getSupportedPreviewFrameRates();
                Log.i(TAG, "cyy support parameters is ");
                Camera.Size psize = null;
                for (int i = 0; i < pictureSizes.size(); i++) {
                    psize = pictureSizes.get(i);
                    Log.i(TAG, "PictrueSize,width: " + psize.width + " height" + psize.height);
                }
                for (int i = 0; i < previewSizes.size(); i++) {
                    psize = previewSizes.get(i);
                    Log.i(TAG, "PreviewSize,width: " + psize.width + " height" + psize.height);
                }
                Integer pf = null;
                for (int i = 0; i < previewFormats.size(); i++) {
                    pf = previewFormats.get(i);
                    Log.i(TAG, "previewformates:" + pf);
                }
                List<String> focusModes = mCamera.getParameters().getSupportedFocusModes();
                if(focusModes.contains("continuous-video")){
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                }
                // 设置拍照和预览图片大小
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
                /* 视频流编码处理 */
                //添加对视频流处理函数
                mCamera.setPreviewCallback(mJpegPreviewCallback);

// 设定配置参数并开启预览
                mCamera.setParameters(parameters); // 将Camera.Parameters设定予Camera
                mCamera.startPreview(); // 打开预览画面
                bIfPreview = true;

                // 【调试】设置后的图片大小和预览大小以及帧率
                Camera.Size csize = mCamera.getParameters().getPreviewSize();
                mPreviewHeight = csize.height; //
                mPreviewWidth = csize.width;
                Log.i(TAG, "after setting, previewSize:width: " + csize.width + " height: " + csize.height);
                csize = mCamera.getParameters().getPictureSize();
                Log.i(TAG, "after setting, pictruesize:width: " + csize.width + " height: " + csize.height);
                Log.i(TAG, "after setting, previewformate is " + mCamera.getParameters().getPreviewFormat());
                Log.i(TAG, "after setting, previewframetate is " + mCamera.getParameters().getPreviewFrameRate());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    Semaphore mYuvBufferlock;
    int yuvLength;
    byte[] mPreviewData;
    TextureView mTextureView;
    // 【获取视频预览帧的接口】
    Camera.PreviewCallback mJpegPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            mPreviewData = data;
            //传递进来的data,默认是YUV420SP的
//// TODO Auto-generated method stub
//            try {
//                Log.i(TAG, "going into onPreviewFrame");
//                //mYUV420sp = data;   // 获取原生的YUV420SP数据
//                yuvLength = data.length;
//
//                // 拷贝原生yuv420sp数据
//                mYuvBufferlock.acquire();
//                System.arraycopy(data, 0, mYUV420SPSendBuffer, 0, data.length);
//                //System.arraycopy(data, 0, mWrtieBuffer, 0, data.length);
//                mYuvBufferlock.release();
//
//                // 开启编码线程，如开启PEG编码方式线程
//                mSendThread1.start();
//
//            } catch (Exception e) {
//                Log.v("System.out", e.toString());
//            }// endtry
        }// endonPriview
    };

    @Override
    public void onAutoFocus(boolean success, Camera camera) {

    }
}
