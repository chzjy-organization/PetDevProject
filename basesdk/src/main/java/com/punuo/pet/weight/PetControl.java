package com.punuo.pet.weight;

/**
 * Created by han.chen.
 * Date on 2019-09-26.
 **/
public class PetControl {

    public static native void petRight();

    public static native void petStop();

    static {
        System.loadLibrary("petControl");
    }

}
