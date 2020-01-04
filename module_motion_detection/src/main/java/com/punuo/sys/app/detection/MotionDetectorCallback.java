package com.punuo.sys.app.detection;

public interface MotionDetectorCallback {
    void onMotionDetected(byte[] bytes);
    void onTooDark();
}
