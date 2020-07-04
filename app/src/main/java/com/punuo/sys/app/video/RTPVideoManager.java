package com.punuo.sys.app.video;

import android.util.Log;

import com.punuo.sys.sip.video.H264Config;

import java.net.DatagramSocket;
import java.net.SocketException;

import jlibrtp.Participant;
import jlibrtp.RTPSession;

/**
 * Created by han.chen.
 * Date on 2020/6/14.
 **/
public class RTPVideoManager {
    private static final String TAG = "RTPVideoManager";

    private static RTPVideoManager rtpVideoManager;

    public static RTPVideoManager getInstance() {
        if (rtpVideoManager == null) {
            synchronized (RTPVideoManager.class) {
                if (rtpVideoManager == null) {
                    rtpVideoManager = new RTPVideoManager();
                }
            }
        }
        return rtpVideoManager;
    }

    public DatagramSocket rtpSocket;
    public DatagramSocket rtcpSocket;
    public RTPSession videoSession;


    public MediaEncoder mediaEncoder;

    private RTPVideoManager() {
        try {
            rtpSocket = new DatagramSocket();
            rtcpSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        videoSession = new RTPSession(rtpSocket, rtcpSocket);

        Participant participant = new Participant(H264Config.rtpIp, H264Config.rtpPort, H264Config.rtpPort + 1);
        videoSession.addParticipant(participant);
        videoSession.setSsrc(H264Config.sSrc);
        mediaEncoder = new MediaEncoder();
    }

    public void start() {
        mediaEncoder.start();
    }

    public void close() {
        mediaEncoder.close();
    }

    public void sendActivePacket() {
        byte[] msg = new byte[20];
        msg[0] = 0x00;
        msg[1] = 0x01;
        msg[2] = 0x00;
        msg[3] = 0x10;
        System.arraycopy(H264Config.getMagic(), 0, msg, 4, 16);
        videoSession.payloadType(0x7a);
        for (int i = 0; i < 2; i++) {
            videoSession.sendData(msg);
        }
    }

    public void onPreviewFrame(byte[] data) {
        byte[] encodeResult = mediaEncoder.offerEncode(data);
        if (encodeResult != null && encodeResult.length > 0) {
            sendActivePacket();
            divideAndSendNal(encodeResult);
        }
    }

    /**
     * 打包分片长度
     */
    public static int DIVIDE_LENGTH = 1000;
    /**
     * 若未打包到末包，则此状态一直为true
     */
    public boolean dividingStatus = true;
    /**
     * 每一个新的NAL设置首包打包状态为false，即没有打包首包
     */
    public boolean firstPktReceived = false;
    /**
     * 记录打包分片的索引
     */
    public int pktIndex = 0;

    private byte[] rtpPkt = new byte[DIVIDE_LENGTH + 2];
    /**
     * 需要去获取，跟设备相关
     */
    private byte[] SPSAndPPS = {
            0x00, 0x00, 0x00, 0x01, 0x67,
            0x42, 0x00, 0x29, (byte) 0x73,
            (byte) 0x73, 0x40, (byte) 0x50,
            0x1e, (byte) 0x30, 0x0f, 0x08,
            (byte) 0x7c, 0x53, (byte) 0x80,
            0x00, 0x00, 0x00, 0x01, 0x68,
            (byte) 0x36, 0x43
    };
    private boolean sendPPSAndSPS = true;

    public void divideAndSendNal(byte[] encodeData) {
        if (encodeData.length <= 0) {
            return;
        }
        if (encodeData.length > DIVIDE_LENGTH) {
            dividingStatus = true;
            firstPktReceived = false;
            pktIndex = 0;
            while (dividingStatus) {
                if (!firstPktReceived) {
                    sendFirstPacket(encodeData);
                } else {
                    if (encodeData.length - pktIndex > DIVIDE_LENGTH) {
                        sendMiddlePacket(encodeData);
                    } else {
                        sendLastPacket(encodeData);
                    }
                }
            }
        } else {
            sendCompletePacket(encodeData);
        }
    }

    public void sendFirstPacket(byte[] encodeData) {
        rtpPkt = new byte[DIVIDE_LENGTH + 2];
        rtpPkt[0] = (byte) (encodeData[0] & 0xe0);
        rtpPkt[0] = (byte) (rtpPkt[0] + 0x1c);
        rtpPkt[1] = (byte) (0x80 + (encodeData[0] & 0x1f));

        if (sendPPSAndSPS) {
            for (int i = 0; i < 3; i++) {
                videoSession.sendData(SPSAndPPS, 936735038);
            }
            sendPPSAndSPS = false;
        }
        try {
            System.arraycopy(encodeData, 0, rtpPkt, 2, DIVIDE_LENGTH);
        } catch (Exception e) {
            e.printStackTrace();
        }
        pktIndex += DIVIDE_LENGTH;
        firstPktReceived = true;
        videoSession.payloadType(0x62);
        videoSession.sendData(rtpPkt);
        Log.d(TAG, "sendFirstPacket length = " + encodeData.length);
    }

    public void sendMiddlePacket(byte[] encodeData) {
        rtpPkt[0] = (byte) (encodeData[0] & 0xe0);
        rtpPkt[0] = (byte) (rtpPkt[0] + 0x1c);
        rtpPkt[1] = (byte) (encodeData[0] & 0x1f);

        try {
            System.arraycopy(encodeData, pktIndex, rtpPkt, 2, DIVIDE_LENGTH);
        } catch (Exception e) {
            e.printStackTrace();
        }
        videoSession.payloadType(0x62);
        videoSession.sendData(rtpPkt);
        pktIndex += DIVIDE_LENGTH;
        Log.d(TAG, "sendMiddlePacket length = " + encodeData.length);
    }

    public void sendLastPacket(byte[] encodeData) {
        rtpPkt = new byte[encodeData.length - pktIndex + 2];
        rtpPkt[0] = (byte) (encodeData[0] & 0xe0);
        rtpPkt[0] = (byte) (rtpPkt[0] + 0x1c);
        rtpPkt[1] = (byte) (0x40 + (encodeData[0] & 0x1f));

        try {
            System.arraycopy(encodeData, pktIndex, rtpPkt, 2, encodeData.length - pktIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }
        videoSession.payloadType(0x62);
        videoSession.sendData(rtpPkt);
        dividingStatus = false;
        Log.d(TAG, "sendLastPacket length = " + encodeData.length);
    }

    public void sendCompletePacket(byte[] encodeData) {
        videoSession.payloadType(0x62);
        videoSession.sendData(encodeData);
        Log.d(TAG, "sendCompletePacket length = " + encodeData.length);
    }

}

