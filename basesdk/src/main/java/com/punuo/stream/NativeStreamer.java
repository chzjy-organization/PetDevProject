package com.punuo.stream;

public class NativeStreamer {

    static {
        System.loadLibrary("native-stream");
    }

    public native int startPublish(String stream, int width, int height);

    public native void stopPublish();

    public native void onPreviewFrame(byte[] yuvData, int width, int height);

}
