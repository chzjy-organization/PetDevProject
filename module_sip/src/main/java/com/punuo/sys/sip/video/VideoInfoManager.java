package com.punuo.sys.sip.video;

import com.punuo.sys.sip.model.MediaData;

import java.net.DatagramSocket;
import java.net.SocketException;

import jlibrtp.Participant;
import jlibrtp.RTPSession;

/**
 * Created by han.chen.
 * Date on 2019-08-22.
 **/
public class VideoInfoManager {
    public static int width = 320;
    public static int height = 240;
    public static int videoType = 2;
    public static int frameRate = 10;
    public static String rtpIp; //目标ip
    public static int rtpPort; //目标port
    public static byte[] magic;
    public static long sVoiceSrc; //音频同步源
    public static long sVideoSrc; //视频同步源
    private DatagramSocket rtpSocket;
    private DatagramSocket rtcpSocket;
    private RTPSession rtpVideoSession;
    private RTPSession rtpVoiceSession;
    /**
     * 每一个新的NAL设置首包打包状态为false，即没有打包首包
     */
    public static boolean firstPktReceived = false;
    /**
     * 记录打包分片的索引
     */
    public static int pktflag = 0;
    /**
     * 若未打包到末包，则此状态一直为true
     */
    public static boolean status = true;
    /**
     * 打包分片长度
     */
    public static int divideLength = 1000;
    /**
     * 分片标志位
     */
    public static boolean dividingFrame = false;

    private static VideoInfoManager videoInfoManager;

    public static VideoInfoManager getInstance() {
        if (videoInfoManager == null) {
            synchronized (VideoInfoManager.class) {
                if (videoInfoManager == null) {
                    videoInfoManager = new VideoInfoManager();
                }
            }
        }
        return videoInfoManager;
    }

    public void reset() {
        try {
            rtpSocket = new DatagramSocket();
            rtcpSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        Participant participant = new Participant(rtpIp, rtpPort, rtpPort + 1);

        rtpVideoSession = new RTPSession(rtpSocket, rtcpSocket);
        rtpVideoSession.addParticipant(participant);
        rtpVideoSession.setSsrc(sVideoSrc);
        //设置RTP包的负载类型为0x62
        rtpVideoSession.payloadType(0x62);

        rtpVoiceSession = new RTPSession(rtpSocket, rtcpSocket);
        rtpVoiceSession.addParticipant(participant);
        rtpVoiceSession.setSsrc(sVoiceSrc);

    }

    public static void initMediaData(MediaData mediaData) {
        if (mediaData == null) {
            return;
        }
        rtpIp = mediaData.getIp();
        rtpPort = mediaData.getPort();
        magic = mediaData.getMagic();
        sVoiceSrc = generateVoiceSsrc(magic);
        sVideoSrc = generateVideoSsrc(magic);
    }

    private static long generateVoiceSsrc(byte[] magic) {
        return (magic[15] & 0x000000ff)
                | ((magic[14] << 8) & 0x0000ff00)
                | ((magic[13] << 16) & 0x00ff0000)
                | ((magic[12] << 24) & 0xff000000);
    }

    private static long generateVideoSsrc(byte[] magic) {
        byte[] videoMagic = new byte[20];
        videoMagic[0] = 0x00;
        videoMagic[1] = 0x01;
        videoMagic[2] = 0x00;
        videoMagic[3] = 0x10;
        //生成RTP心跳保活包，即在magic之前再加上0x00 0x01 0x00 0x10
        System.arraycopy(magic, 0, videoMagic, 4, 16);
        return (videoMagic[15] & 0x000000ff)
                | ((videoMagic[14] << 8) & 0x0000ff00)
                | ((videoMagic[13] << 16) & 0x00ff0000)
                | ((videoMagic[12] << 24) & 0xff000000);
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
                pktflag = 0;

                while (status) {
                    if (!firstPktReceived) {
                        //首包
                        sendFirstPacket(encodeResult);
                    } else {
                        if (encodeResult.length - pktflag > divideLength) {
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
    public void sendFirstPacket(byte[] encodeResult) {
        rtppkt[0] = (byte) (encodeResult[4] & 0xe0);
        rtppkt[0] = (byte) (rtppkt[4] + 0x1c);
        rtppkt[1] = (byte) (0x80 + (encodeResult[4] & 0x1f));
        try {
            System.arraycopy(encodeResult, 0, rtppkt, 2, divideLength);
        } catch (Exception e) {
            e.printStackTrace();
        }
        pktflag = pktflag + divideLength;
        firstPktReceived = true;
        //发送打包数据
        rtpVideoSession.sendData(rtppkt);
    }

    /**
     * 发送中包
     */
    private void sendMiddlePacket(byte[] encodeResult) {
        rtppkt[0] = (byte) (encodeResult[0] & 0xe0); //获取Nalu单元的前三位
        rtppkt[0] = (byte) (rtppkt[0] + 0x1c); //加上Fu-A的type值28（0x1c）即组成FU indicator
        rtppkt[1] = (byte) ((encodeResult[0] & 0x1f)); //中包的ser为000加上Nalu的type组成 FU header
        try {
            System.arraycopy(encodeResult, pktflag, rtppkt, 2, divideLength);
        } catch (Exception e) {
            e.printStackTrace();
        }
        pktflag = pktflag + divideLength;
        //发送打包数据
        rtpVideoSession.sendData(rtppkt);
    }

    /**
     * 发送末包
     */
    private void sendLastPacket(byte[] encodeResult) {
        byte[] rtppktLast = new byte[encodeResult.length - pktflag + 2];
        rtppktLast[0] = (byte) (encodeResult[0] & 0xe0);
        rtppktLast[0] = (byte) (rtppktLast[0] + 0x1c);
        rtppktLast[1] = (byte) (0x40 + (encodeResult[0] & 0x1f));
        try {
            System.arraycopy(encodeResult, pktflag, rtppktLast, 2, encodeResult.length - pktflag);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //发送打包数据
        rtpVideoSession.sendData(rtppktLast);
        status = false;  //打包组包结束，下一步进行解码
        dividingFrame = false;  //一帧分片打包完毕，时间戳改下一帧
    }

    /**
     * 发送完整包
     */
    private void sendCompletePacket(byte[] encodeResult) {
        //发送打包数据
        rtpVideoSession.sendData(encodeResult);
    }

}
