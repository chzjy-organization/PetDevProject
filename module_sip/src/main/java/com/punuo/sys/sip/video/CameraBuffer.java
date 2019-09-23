package com.punuo.sys.sip.video;

/**
 * Created by han.chen.
 * Date on 2019-09-18.
 * Camera buffer
 **/
public class CameraBuffer {
    /**
     * YUV frame where frame size is always (videoWidth*videoHeight*3)/2
     */
    private byte[] frame;

    public CameraBuffer() {
        this.frame = new byte[(H264Config.VIDEO_WIDTH
                * H264Config.VIDEO_HEIGHT * 3) / 2];
    }

    /**
     * Set the last captured frame
     *
     * @param frame Frame
     */
    public void setFrame(byte[] frame) {
        this.frame = frame;
    }

    /**
     * Return the last captured frame
     *
     * @return Frame
     */
    public byte[] getFrame() {
        return frame;
    }
}
