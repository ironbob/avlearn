package com.wtb.audiorecord;

import android.media.AudioRecord;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by baobaowang on 2018/4/16.
 * ffplay -f s16le -ar 48000 -ac 2 d:\lei.pcm
 */
public class PCMDataSaver implements Runnable {
    private volatile boolean run = true;
    private AudioRecord mAudioRecord;
    private String mFilename;
    private int mBuffSize;
    private Thread mThread;
    private Callback mCallback;

    private long mTotalWritted;

    public PCMDataSaver(AudioRecord audioRecord, int buffSize, Callback callback) {
        this.mAudioRecord = audioRecord;
        this.mBuffSize = buffSize;
        mCallback = callback;
    }

    public void setFileName(String fileName) {
        this.mFilename = fileName;
    }

    public void start() {
        run = true;
        mThread = new Thread(this);
        mThread.start();
    }

    public void stop() {
        run = false;
    }

    @Override
    public void run() {
        File file = new File(mFilename);
        FileOutputStream fos = null;
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();

            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (fos == null) {
            return;
        }
        byte[] buff = new byte[mBuffSize];
        mTotalWritted = 0;
        while (run) {
            int ret = mAudioRecord.read(buff, 0, mBuffSize);
            mTotalWritted += ret;
            if (ret != AudioRecord.ERROR_INVALID_OPERATION) {
                try {
                    fos.write(buff, 0, ret);
                    mCallback.onWrite(ret, mTotalWritted);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    interface Callback {
        void onWrite(long length, long totalWritted);
    }
}
