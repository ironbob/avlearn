package com.wtb.audiorecord;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import static com.wtb.audiorecord.Status.PREPARE;
import static com.wtb.audiorecord.Status.RECORDING;
import static com.wtb.audiorecord.Status.STOPPED;
import static com.wtb.audiorecord.Status.UN_INIT;

/**
 * Created by baobaowang on 2018/4/16.
 */
public class AudioRecorder {

    // 音频源：音频输入-麦克风
    private final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;
    // 采样率
    // 44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    // 采样频率一般共分为22.05KHz、44.1KHz、48KHz三个等级
    private final static int AUDIO_SAMPLE_RATE = 44100;
    // 音频通道 单声道
    private final static int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    // 音频格式：PCM编码
    private final static int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    // 缓冲区大小：缓冲区字节大小
    private int bufferSizeInBytes = 0;
    // 录音对象
    private AudioRecord audioRecord;
    // 录音状态
    private int mStatus = UN_INIT;

    private PCMDataSaver mDataSaver;
    private PCMDataSaver.Callback mCallback;

    public AudioRecorder(PCMDataSaver.Callback callback) {
        mCallback = callback;
    }

    public void init() {
        // 获得缓冲区字节大小
        bufferSizeInBytes = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE,
                AUDIO_CHANNEL, AUDIO_ENCODING);
        audioRecord = new AudioRecord(AUDIO_INPUT, AUDIO_SAMPLE_RATE, AUDIO_CHANNEL, AUDIO_ENCODING, bufferSizeInBytes);
        mDataSaver = new PCMDataSaver(audioRecord, bufferSizeInBytes, mCallback);
    }

    public void start(String fileName) {
        if (mStatus == UN_INIT) {
            init();
            mStatus = PREPARE;
        }
        mDataSaver.setFileName(fileName);
        if (mStatus != PREPARE) {
            return;
        }
        realStart();
    }

    private void realStart() {
        audioRecord.startRecording();
        mStatus = RECORDING;
        mDataSaver.start();

    }

    public void stop() {
        if (mStatus == RECORDING) {
            audioRecord.stop();
            mDataSaver.stop();
            mStatus = STOPPED;
        }
    }

    public void release() {
        if (mStatus != UN_INIT) {
            audioRecord.release();
            mStatus = UN_INIT;
        }
    }


}
