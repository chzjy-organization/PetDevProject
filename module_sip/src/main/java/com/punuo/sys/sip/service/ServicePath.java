package com.punuo.sys.sip.service;

/**
 * Created by han.chen.
 * Date on 2019-08-20.
 **/
public class ServicePath {

    public static final String PATH_DIRECTION_CONTROL = "/sip/direction_control";
    public static final String PATH_REGISTER = "/sip/negotiate_response";
    public static final String PATH_LOGIN = "/sip/login_response";

    public static final String PATH_ERROR = "/sip/error";
    public static final String PATH_START_VIDEO ="/sip/start_video";
    public static final String PATH_STOP_VIDEO = "/sip/stop_video";

    //关于称重
    public static final String PATH_WEIGHT="/sip/weight_response";
    //立即出粮
    public static final String PATH_FEED_NOW = "/sip/feed_now_response";
    //喂食计划
    public static final String PATH_FEED_PLAN = "/sip/feed_plan_response";
    //接收WiFi账号密码
    public static final String PATH_WIFI="/sip/set_wifi_response";
    //重置功能
    public static final String PATH_RESET="/sip/dev_reset";
}
