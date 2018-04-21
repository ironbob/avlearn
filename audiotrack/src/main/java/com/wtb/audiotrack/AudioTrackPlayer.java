package com.wtb.audiotrack;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import static com.wtb.audiotrack.Status.PAUSE;
import static com.wtb.audiotrack.Status.PLAYING;
import static com.wtb.audiotrack.Status.PREPARE;
import static com.wtb.audiotrack.Status.UN_INIT;

/**
 * Created by baobaowang on 2018/4/17.
 */
public class AudioTrackPlayer {
    AudioTrack audioTrack;
    PcmDataProvider pcmDataProvider;
    int status;

    public void play(String filePath) {
        if (status == Status.UN_INIT) {
            int bufferSizeInBytes = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes, AudioTrack.MODE_STREAM);
            pcmDataProvider = new PcmDataProvider(audioTrack, bufferSizeInBytes);
            status = PREPARE;
        }
        if (status == PLAYING) {
            throw new IllegalStateException("正在播放，不能重复点击");
        }
        audioTrack.play();
        pcmDataProvider.start(filePath);
        status = PLAYING;
    }

    public void stop() {
        if (status == PLAYING || status == PAUSE) {
            audioTrack.stop();
            pcmDataProvider.stop();
            audioTrack.release();
            status = UN_INIT;
        }
    }

    public void pause() {
        if (status == PLAYING) {
            audioTrack.pause();
            pcmDataProvider.pause();
            status = PAUSE;
        }
    }

    public void resume() {
        if (status == PAUSE) {
            audioTrack.play();
            pcmDataProvider.resume();
            status = PLAYING;
        }
    }

    public int getStatus() {
        return status;
    }
}
