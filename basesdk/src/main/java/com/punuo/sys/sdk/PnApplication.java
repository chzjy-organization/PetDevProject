package com.punuo.sys.sdk;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.alibaba.android.arouter.launcher.ARouter;
import com.punuo.sys.sdk.util.DeviceHelper;
import com.tencent.mmkv.MMKV;

/**
 * Created by han.chen.
 * Date on 2019/4/4.
 **/
public class PnApplication extends Application {
    private static PnApplication instance;

    public static PnApplication getInstance() {
        if (instance == null) {
            instance = new PnApplication();
        }
        return instance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
        MMKV.initialize(base);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        if (DeviceHelper.isApkInDebug()) {
            ARouter.openLog();
            ARouter.openDebug();
        }
        ARouter.init(this);
    }
}
