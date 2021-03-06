package com.punuo.sys.app.process;

import android.app.Application;

import com.punuo.sys.sdk.activity.ActivityLifeCycle;
import com.punuo.sys.sdk.httplib.HttpConfig;
import com.punuo.sys.sdk.httplib.HttpManager;
import com.punuo.sys.sdk.httplib.IHttpConfig;
import com.punuo.sys.sdk.util.DebugCrashHandler;
import com.punuo.sys.sdk.util.DeviceHelper;
import com.punuo.sys.sip.config.ISipConfig;
import com.punuo.sys.sip.config.SipConfig;
import com.punuo.sys.sip.SipDevManager;
import com.punuo.sys.sip.thread.SipInitThread;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;

/**
 * Created by han.chen.
 * Date on 2019-06-15.
 **/
public class ProcessTasks {

    public static void commonLaunchTasks(Application app) {
        if (DeviceHelper.isApkInDebug()) {
            DebugCrashHandler.getInstance().init(); //崩溃日志收集
        }
        app.registerActivityLifecycleCallbacks(ActivityLifeCycle.getInstance());
        HttpConfig.init(new IHttpConfig() {
            @Override
            public String getHost() {
                return "pet.qinqingonline.com";
            }

            @Override
            public int getPort() {
                return 0;
            }

            @Override
            public boolean isUseHttps() {
                return false;
            }

            @Override
            public String getUserAgent() {
                return "punuo";
            }

            @Override
            public String getPrefixPath() {
                return "";
            }
        });
        HttpManager.setContext(app);
        HttpManager.init();
        SipConfig.init(new ISipConfig() {
            NameAddress mServerAddress;
            NameAddress mDevNormalAddress;

            @Override
            public String getServerIp() {
                return "39.98.36.250";
            }//101.69.255.134

            @Override
            public int getPort() {
                return 6060;
            }

            @Override
            public String getDevId() {
                return "310023004901920001";//310023001139940001
            }

            @Override
            public NameAddress getServerAddress() {
                if (mServerAddress == null) {
                    SipURL remote = new SipURL(SipConfig.SERVER_ID, SipConfig.getServerIp(), SipConfig.getPort());
                    mServerAddress = new NameAddress(SipConfig.SERVER_NAME, remote);
                }
                return mServerAddress;
            }

            @Override
            public NameAddress getDevNormalAddress() {
                if (mDevNormalAddress == null) {
                    SipURL local = new SipURL(getDevId(), SipConfig.getServerIp(), SipConfig.getPort());
                    mDevNormalAddress = new NameAddress(getDevId(), local);
                }
                return mDevNormalAddress;
            }
        });
        SipDevManager.setContext(app);
        new SipInitThread().start();
    }
}
