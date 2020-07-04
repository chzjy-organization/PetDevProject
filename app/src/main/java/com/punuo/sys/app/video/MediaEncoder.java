package com.punuo.sys.app.video;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import com.punuo.sys.sip.video.H264Config;

import java.io.IOException;
import java.nio.ByteBuffer;

import tech.shutu.jni.YuvUtils;

/**
 * Created by han.chen.
 * Date on 2020/6/14.
 **/
public class MediaEncoder {
    private static final String TAG = "AvcEncoder";
    private MediaCodec mediaCodec;
    private int count = 0;

    public MediaEncoder() {
    }

    public void start() {
        try {
            mediaCodec = MediaCodec.createEncoderByType("video/avc");
        } catch (IOException e) {
            e.printStackTrace();
        }
        YuvUtils.allocateMemo(H264Config.VIDEO_WIDTH * H264Config.VIDEO_HEIGHT * 3 / 2,
                0, H264Config.VIDEO_WIDTH * H264Config.VIDEO_HEIGHT * 3 / 2);
        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", H264Config.VIDEO_WIDTH, H264Config.VIDEO_HEIGHT);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 1000000);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 20);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mediaCodec.start();
    }

    public byte[] offerEncode(byte[] input) {
        byte[] output = null;
        input = nv21ToI420(input, H264Config.VIDEO_WIDTH, H264Config.VIDEO_HEIGHT);
        try {
            ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
            ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
            int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                inputBuffer.put(input);
                mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length,  count * 1000000 / 15, 0);
                count++;
            }
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo,0);
            ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
            output = new byte[bufferInfo.size];
            outputBuffer.get(output);
            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return output;
    }

    public byte[] nv21ToI420(byte[] data, int width, int height) {
        byte[] ret = new byte[data.length];
        int total = width * height;

        ByteBuffer bufferY = ByteBuffer.wrap(ret, 0, total);
        ByteBuffer bufferU = ByteBuffer.wrap(ret, total, total / 4);
        ByteBuffer bufferV = ByteBuffer.wrap(ret, total + total / 4, total / 4);

        bufferY.put(data, 0, total);
        for (int i=total; i<data.length; i+=2) {
            bufferV.put(data[i]);
            bufferU.put(data[i+1]);
        }

        return ret;
    }

    private byte[] swapYV12toI420(byte[] yv12bytes, int width, int height) {
        byte[] i420bytes = new byte[yv12bytes.length];
        int temp = width * height + (width / 2 * height / 2);
        int value = temp - width * height;
        int temp2 = width * height + 2 * (width / 2 * height / 2);
        if (width * height >= 0) {
            System.arraycopy(yv12bytes, 0, i420bytes, 0, width * height);
        }
        if (value >= 0) {
            System.arraycopy(yv12bytes, value, i420bytes, width * height, value);
        }
        if (temp2 - temp >= 0) {
            System.arraycopy(yv12bytes, value - (width / 2 * height / 2), i420bytes, value,
                    temp2 - width * height + (width / 2 * height / 2));
        }
        return i420bytes;
    }

    public void close() {
        try {
            mediaCodec.stop();
            mediaCodec.release();
            YuvUtils.releaseMemo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
