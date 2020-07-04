package com.punuo.sys.app.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Process;

import com.punuo.sys.sip.video.H264Config;

import java.net.DatagramSocket;
import java.net.SocketException;

import jlibrtp.Participant;
import jlibrtp.RTPSession;

/**
 * Created by han.chen.
 * Date on 2020/6/19.
 **/
public class MediaAudioManager {
    private static final int frameSizeG711 = 160;
    private static MediaAudioManager mediaAudioManager;

    public static MediaAudioManager getInstance() {
        if (mediaAudioManager == null) {
            synchronized (MediaAudioManager.class) {
                if (mediaAudioManager == null) {
                    mediaAudioManager = new MediaAudioManager();
                }
            }
        }
        return mediaAudioManager;
    }

    private RTPSession voiceSession;
    private DatagramSocket rtpSocket;
    private DatagramSocket rtcpSocket;
    private boolean started = false;
    private AudioThread mAudioThread;

    public void init() {
        try {
            rtpSocket = new DatagramSocket();
            rtcpSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        Participant participant = new Participant(H264Config.rtpIp, H264Config.rtpPort, H264Config.rtpPort + 1);
        voiceSession = new RTPSession(rtpSocket, rtcpSocket);
        voiceSession.addParticipant(participant);
        voiceSession.setSsrc(H264Config.sSrc);
    }

    public void startAudioRecord() {
        init();
        started = true;
        mAudioThread = new AudioThread();
        mAudioThread.start();
    }

    public void stopAudioRecord() {
        started = false;
    }

    private AudioRecord getAudioRecord() {
        int sampleRate = 8000;
        int min = AudioRecord.getMinBufferSize(sampleRate,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        AudioRecord record = null;
        record = new AudioRecord(
                MediaRecorder.AudioSource.MIC,//the recording source
                sampleRate, //采样频率，一般为8000hz/s
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                min);
        if (AECManager.isDeviceSupport()) {
            AECManager.getInstance().initAEC(record.getAudioSessionId());
        }
        record.startRecording();
        return record;
    }

    void calc2(short[] lin, int off, int len) {
        int i, j;

        for (i = 0; i < len; i++) {
            j = lin[i + off];
            lin[i + off] = (short) (j >> 1);
        }
    }

    class AudioThread extends Thread {

        @Override
        public void run() {
            super.run();
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
            AudioRecord record = getAudioRecord();
            //int frame_size = 160;
            short[] audioData = new short[frameSizeG711];
            byte[] encodeData = new byte[frameSizeG711];
            int numRead = 0;

            while (started) {
                if (voiceSession == null) {
                    break;
                }
                numRead = record.read(audioData, 0, frameSizeG711);
                if (numRead <= 0) {
                    continue;
                }
                calc2(audioData, 0, numRead);
                //进行pcmu编码
                G711.linear2ulaw(audioData, 0, encodeData, numRead);
                voiceSession.payloadType(0x45);
                voiceSession.sendData(encodeData);
            }
            record.stop();
            record.release();
        }
    }
}
