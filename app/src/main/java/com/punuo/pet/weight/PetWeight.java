package com.punuo.pet.weight;

/**
 * Created by han.chen.
 * Date on 2019-09-26.
 **/
public class PetWeight {

    public static native int getWeight();

    static {
        System.loadLibrary("petWeight");
    }

}
