package com.punuo.sys.sip.video;

import android.util.Log;

import com.punuo.sys.sip.model.MediaData;

import java.net.DatagramSocket;
import java.net.SocketException;

import jlibrtp.Participant;
import jlibrtp.RTPSession;

/**
 * Created by han.chen.
 * Date on 2019-08-22.
 **/
public class MediaRtpSender {
    private static String rtpIp; //目标ip
    private static int rtpPort; //目标port
    private static byte[] magic;
    private static long voicesSrc; //音频同步源
    private static long videosSrc; //视频同步源
    private DatagramSocket rtpSocket;
    private DatagramSocket rtcpSocket;
    private RTPSession rtpVideoSession;
    private RTPSession rtpVoiceSession;
    /**
     * 每一个新的NAL设置首包打包状态为false，即没有打包首包
     */
    private boolean firstPktReceived = false;
    /**
     * 记录打包分片的索引
     */
    private int pktFlag = 0;
    /**
     * 若未打包到末包，则此状态一直为true
     */
    private boolean status = true;
    /**
     * 打包分片长度
     */
    private int divideLength = 1000;
    /**
     * 分片标志位
     */
    private boolean dividingFrame = false;

    private SendActivePacket mSendActivePacket;

    private static MediaRtpSender mediaRtpSender;

    public static MediaRtpSender getInstance() {
        if (mediaRtpSender == null) {
            synchronized (MediaRtpSender.class) {
                if (mediaRtpSender == null) {
                    mediaRtpSender = new MediaRtpSender();
                }
            }
        }
        return mediaRtpSender;
    }

    public void init() {
        try {
            rtpSocket = new DatagramSocket();
            rtcpSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        Participant participant = new Participant(rtpIp, rtpPort, rtpPort + 1);

        rtpVideoSession = new RTPSession(rtpSocket, rtcpSocket);
        rtpVideoSession.addParticipant(participant);
        rtpVideoSession.setSsrc(videosSrc);

        rtpVoiceSession = new RTPSession(rtpSocket, rtcpSocket);
        rtpVoiceSession.addParticipant(participant);
        rtpVoiceSession.setSsrc(voicesSrc);
        mSendActivePacket = new SendActivePacket();
        mSendActivePacket.startThread();
    }

    public void onDestory() {
        if (mSendActivePacket != null) {
            mSendActivePacket.stopThread();
        }
    }

    public static void initMediaData(MediaData mediaData) {
        if (mediaData == null) {
            return;
        }
        rtpIp = mediaData.getIp();
        rtpPort = mediaData.getPort();
        magic = mediaData.getMagic();
        voicesSrc = generateVoiceSsrc();
        videosSrc = generateVideoSsrc();
    }

    private static long generateVoiceSsrc() {
        return (magic[15] & 0x000000ff)
                | ((magic[14] << 8) & 0x0000ff00)
                | ((magic[13] << 16) & 0x00ff0000)
                | ((magic[12] << 24) & 0xff000000);
    }

    private static long generateVideoSsrc() {
        return (magic[15] & 0x000000ff)
                | ((magic[14] << 8) & 0x0000ff00)
                | ((magic[13] << 16) & 0x00ff0000)
                | ((magic[12] << 24) & 0xff000000);
    }

    private void sendActivePacket(byte[] msg) {
        rtpVideoSession.payloadType(0x7a);
        for (int i = 0; i < 2; i++) {
            rtpVideoSession.sendData(msg);
        }
    }

    /**
     * 分片、发送方法
     */
    public void divideAndSendNal(byte[] encodeResult) {
        if (encodeResult.length > 0) {  //有数据才进行分片发送操作
            if (encodeResult.length > divideLength) {
                dividingFrame = true;
                status = true;
                firstPktReceived = false;
                pktFlag = 0;

                while (status) {
                    if (!firstPktReceived) {
                        //首包
                        sendFirstPacket(encodeResult);
                    } else {
                        if (encodeResult.length - pktFlag > divideLength) {
                            //中包
                            sendMiddlePacket(encodeResult);
                        } else {
                            //末包
                            sendLastPacket(encodeResult);
                        }
                    }
                }
            } else {
                //完整包
                sendCompletePacket(encodeResult);
            }
        }
    }

    /**
     * 打包发送的数组大小定义
     */
    private byte[] rtppkt = new byte[divideLength + 2];

    /**
     * 发送首包
     */
    private void sendFirstPacket(byte[] encodeResult) {
        Log.i("MediaRtpSender", "sendFirstPacket: 首包");
        rtppkt[0] = (byte) (encodeResult[4] & 0xe0);
        rtppkt[0] = (byte) (rtppkt[4] + 0x1c);
        rtppkt[1] = (byte) (0x80 + (encodeResult[4] & 0x1f));
        try {
            System.arraycopy(encodeResult, 0, rtppkt, 2, divideLength);
        } catch (Exception e) {
            e.printStackTrace();
        }
        pktFlag = pktFlag + divideLength;
        firstPktReceived = true;
        //设置RTP包的负载类型为0x62
        rtpVideoSession.payloadType(0x62);
        //发送打包数据
        rtpVideoSession.sendData(rtppkt);
    }

    /**
     * 发送中包
     */
    private void sendMiddlePacket(byte[] encodeResult) {
        Log.i("MediaRtpSender", "sendMiddlePacket: 中包");
        rtppkt[0] = (byte) (encodeResult[0] & 0xe0); //获取Nalu单元的前三位
        rtppkt[0] = (byte) (rtppkt[0] + 0x1c); //加上Fu-A的type值28（0x1c）即组成FU indicator
        rtppkt[1] = (byte) ((encodeResult[0] & 0x1f)); //中包的ser为000加上Nalu的type组成 FU header
        try {
            System.arraycopy(encodeResult, pktFlag, rtppkt, 2, divideLength);
        } catch (Exception e) {
            e.printStackTrace();
        }
        pktFlag = pktFlag + divideLength;
        //设置RTP包的负载类型为0x62
        rtpVideoSession.payloadType(0x62);
        //发送打包数据
        rtpVideoSession.sendData(rtppkt);
    }

    /**
     * 发送末包
     */
    private void sendLastPacket(byte[] encodeResult) {
        Log.i("MediaRtpSender", "sendLastPacket: 末包");
        byte[] rtppktLast = new byte[encodeResult.length - pktFlag + 2];
        rtppktLast[0] = (byte) (encodeResult[0] & 0xe0);
        rtppktLast[0] = (byte) (rtppktLast[0] + 0x1c);
        rtppktLast[1] = (byte) (0x40 + (encodeResult[0] & 0x1f));
        try {
            System.arraycopy(encodeResult, pktFlag, rtppktLast, 2, encodeResult.length - pktFlag);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //设置RTP包的负载类型为0x62
        rtpVideoSession.payloadType(0x62);
        //发送打包数据
        rtpVideoSession.sendData(rtppktLast);
        status = false;  //打包组包结束，下一步进行解码
        dividingFrame = false;  //一帧分片打包完毕，时间戳改下一帧
    }

    /**
     * 发送完整包
     */
    private void sendCompletePacket(byte[] encodeResult) {
        Log.i("MediaRtpSender", "sendCompletePacket: 整包");
        //设置RTP包的负载类型为0x62
        rtpVideoSession.payloadType(0x62);
        //发送打包数据
        rtpVideoSession.sendData(encodeResult);
    }

    public class SendActivePacket extends Thread {
        private boolean running = false;
        private byte[] msg = new byte[20];

        public SendActivePacket() {
            msg[0] = 0x00;
            msg[1] = 0x01;
            msg[2] = 0x00;
            msg[3] = 0x10;
            System.arraycopy(magic, 0, msg, 4, 16);
        }

        @Override
        public void run() {
            while (running) {
                try {
                    Log.d("SendActivePacket", "run: " + Thread.currentThread().getId());
                    sendActivePacket(msg);
                    sleep(20 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void startThread() {
            running = true;
            super.start();
        }

        public void stopThread() {
            running = false;
        }
    }

}
