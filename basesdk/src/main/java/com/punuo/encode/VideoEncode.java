package com.punuo.encode;

/**
 * Created by han.chen.
 * Date on 2019-09-27.
 **/
public class VideoEncode {

    public static native byte[] init(int width, int height, int rate);

    public static native byte[] encode(byte[] input);

    public static native int flush();

    public static native int close();

    static {
        System.loadLibrary("avutil-54");
        System.loadLibrary("swresample-1");
        System.loadLibrary("avcodec-56");
        System.loadLibrary("avformat-56");
        System.loadLibrary("swscale-3");
        System.loadLibrary("postproc-53");
        System.loadLibrary("avfilter-5");
        System.loadLibrary("avdevice-56");
        System.loadLibrary("avencoder");
    }
}
