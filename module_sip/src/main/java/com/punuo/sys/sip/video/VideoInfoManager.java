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
    public static int width = 352;
    public static int height = 288;
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
}
