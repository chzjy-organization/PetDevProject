package com.punuo.sip;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.punuo.sip.request.BaseSipRequest;
import com.punuo.sys.sdk.httplib.ErrorTipException;

import org.zoolu.sip.message.BaseSipResponses;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.Transport;
import org.zoolu.sip.provider.TransportConnId;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by han.chen.
 * Date on 2019-08-12.
 **/
public class SipUserManager extends SipProvider {
    private static final String TAG = "SipUserManager";
    private static String[] PROTOCOLS = {"udp"};
    private static Context sContext;
    private ExecutorService mExecutorService;
    private static volatile SipUserManager sSipUserManager;
    private static HashMap<TransportConnId, BaseSipRequest> mRequestMap;

    public static SipUserManager getInstance() {
        if (sContext == null) {
            throw new RuntimeException("context is null, please set context");
        }
        if (sSipUserManager == null) {
            synchronized (SipUserManager.class) {
                if (sSipUserManager == null) {
                    int hostPort = new Random().nextInt(5000) + 2000;
                    sSipUserManager = new SipUserManager(hostPort);
                }
            }
        }
        return sSipUserManager;
    }

    public static void setContext(Context context) {
        sContext = context.getApplicationContext();
        mRequestMap = new HashMap<>();
    }

    private SipUserManager(int host_port) {
        super(null, host_port, PROTOCOLS, null);
        mExecutorService = Executors.newFixedThreadPool(3);
    }

    public void addRequest(BaseSipRequest sipRequest) {
        if (sipRequest == null) {
            return;
        }
        Message message = sipRequest.build();
        if (message != null) {
            TransportConnId id = sendMessage(message);
            mRequestMap.put(id, sipRequest);
        } else {
            Log.w(TAG, "build message is null");
        }
    }

    @Override
    public TransportConnId sendMessage(Message msg) {
        return sendMessage(msg, SipConfig.getServerIp(), SipConfig.getPort());
    }

    public TransportConnId sendMessage(final Message msg, final String destAddr, final int destPort) {
        Log.v(TAG, "<----------send sip message---------->");
        Log.v(TAG, msg.toString());
        TransportConnId id = null;
        try {
            id = mExecutorService.submit(new Callable<TransportConnId>() {
                @Override
                public TransportConnId call() throws Exception {
                    return sendMessage(msg, "udp", destAddr, destPort, 0);
                }
            }).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return id;
    }

    @Override
    public synchronized void onReceivedMessage(Transport transport, Message msg) {
        Log.v(TAG, "<----------received sip message---------->");
        Log.v(TAG, msg.toString());
        TransportConnId id = msg.getTransportConnId();
        BaseSipRequest sipRequest = mRequestMap.get(id);
        if (sipRequest != null) {
            handleResponseMessage(sipRequest, msg);
            mRequestMap.remove(id);
        }
    }

    private SipExecutorDelivery mSipExecutorDelivery = new SipExecutorDelivery(new Handler(Looper
            .getMainLooper()));

    private void handleResponseMessage(BaseSipRequest sipRequest, Message message) {
        int code = message.getStatusLine().getCode();
        switch (code) {
            case 200:
                mSipExecutorDelivery.postResponse(sipRequest, message);
                break;
            default:
                mSipExecutorDelivery.postError(sipRequest, message, new ErrorTipException(BaseSipResponses.reasonOf(code)));
                break;
        }
    }
}
