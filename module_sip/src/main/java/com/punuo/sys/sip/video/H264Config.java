package com.punuo.sys.sip.video;

import com.punuo.sys.sip.model.MediaData;

/**
 * Created by han.chen.
 * Date on 2019-09-18.
 **/
public class H264Config {
    /**
     * Default video width
     */
    public final static int VIDEO_WIDTH = 640;

    /**
     * Default video height
     */
    public final static int VIDEO_HEIGHT = 480;

    /**
     * Default video frame rate
     */
    public final static int FRAME_RATE = 10;
    /**
     * Default video type
     */
    public final static int VIDEO_TYPE = 2;
    /**
     * Default video rtmp address
     */
    public static String RTMP_STREAM = "rtmp://101.69.255.130:1936/hls/live";

    public static String rtpIp = "101.69.255.134";
    public static int rtpPort;
    public static byte[] magic;
    public static long sSrc = 0;
    public static int videoNum = 0; //监控的用户数量

    public static void initMediaData(MediaData mediaData) {
        rtpIp = mediaData.getIp();
        rtpPort = mediaData.getPort();
        magic = mediaData.getMagic();
        sSrc = (magic[15] & 0x000000ff)
                | ((magic[14] << 8) & 0x0000ff00)
                | ((magic[13] << 16) & 0x00ff0000)
                | ((magic[12] << 24) & 0xff000000);
    }

    public static byte[] getMagic() {
        return magic;
    }
}
