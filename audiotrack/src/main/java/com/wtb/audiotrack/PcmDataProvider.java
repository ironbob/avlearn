package com.wtb.audiotrack;

import android.media.AudioTrack;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by baobaowang on 2018/4/17.
 */
public class PcmDataProvider implements Runnable {
    private static final String TAG = "PcmDataProvider";
    private String mFilePath;
    private int mBuffsize;
    private volatile boolean run;
    private AudioTrack mTrack;
    private Thread mThread;
    private Condition mCondition;
    private ReentrantLock mLock;
    private volatile boolean mPause;

    public PcmDataProvider(AudioTrack track, int buffSize) {
        this.mBuffsize = buffSize;
        this.mTrack = track;
        mLock = new ReentrantLock();
        mCondition = mLock.newCondition();
    }

    public void start(String filePath) {
        this.mFilePath = filePath;
        run = true;
        mThread = new Thread(this);
        mThread.start();
    }

    public void stop() {
        run = false;
    }

    public void pause() {
        mPause = true;
    }

    public void resume() {
        try {
            mLock.lock();
            mPause = false;
            mCondition.signal();
            Log.i(TAG, "resume播放");
        } finally {
            mLock.unlock();
        }
    }

    @Override
    public void run() {
        File file = new File(mFilePath);
        if (!file.exists()) {
            Log.e("PcmDataProvider", "文件不存在");
            return;
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (fis == null) return;
        byte[] buff = new byte[mBuffsize];
        while (run) {
            try {
                mLock.lock();
                while (mPause) {
                    Log.i(TAG, "暂停播放");
                    mCondition.await();
                }
                int ret = fis.read(buff);
                Log.i(TAG, "fis.read ret = " + ret);
                if (ret != -1) {
                    int r = mTrack.write(buff, 0, ret);
                    if (r < 0) {
                        Log.e(TAG, "AudioTrack写入错误 r =" + r);
                        break;
                    }
                } else {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                mLock.unlock();
            }
        }
    }
}
