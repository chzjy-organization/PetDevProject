package com.punuo.sys.app;

import android.os.Bundle;

import com.punuo.sys.app.process.ProcessTasks;
import com.punuo.sys.sdk.PnApplication;
import com.punuo.sys.sdk.activity.BaseActivity;

/**
 * Created by han.chen.
 * Date on 2019-08-17.
 **/
public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ProcessTasks.commonLaunchTasks(PnApplication.getInstance());
        init();
    }

    private void init() {
        //TODO 开启蓝牙监听线程
    }
}
