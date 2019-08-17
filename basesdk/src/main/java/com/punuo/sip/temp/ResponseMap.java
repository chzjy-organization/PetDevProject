package com.punuo.sip.temp;

import java.util.HashMap;

/**
 * Created by han.chen.
 * Date on 2019-08-17.
 **/
public class ResponseMap {
    public static HashMap<String, Class> map = new HashMap<>();
    public static final String DIRECTION_CONTROL="direction_control";
    static {
        map.put(DIRECTION_CONTROL, ControlData.class);
    }

    public static Class getClazz(String key) {
        return map.get(key);
    }
}
