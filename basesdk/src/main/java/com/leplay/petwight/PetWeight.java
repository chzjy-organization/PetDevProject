package com.leplay.petwight;

/**
 * Created by han.chen.
 * Date on 2019-10-15.
 **/
public class PetWeight {
    public PetWeight() {
    }

    public native int getWeight();

    public native boolean reset();

    public native boolean turnOnCustom1Light();

    public native boolean turnOnCustom2Light();

    public native boolean turnOffCustom1Light();

    public native boolean turnOffCustom2Light();

    static {
        System.loadLibrary("native-lib");
    }
}