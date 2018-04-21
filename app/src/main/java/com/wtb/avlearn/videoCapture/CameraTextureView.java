package com.wtb.avlearn.videoCapture;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.TextureView;

/**
 * Created by baobaowang on 2018/4/21.
 */
public class CameraTextureView extends TextureView implements TextureView.SurfaceTextureListener
{
    SurfaceTexture texture;
    public CameraTextureView(Context context) {
        super(context);
    }

    public CameraTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        texture = surface;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
