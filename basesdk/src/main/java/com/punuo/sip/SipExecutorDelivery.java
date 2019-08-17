package com.punuo.sip;

import android.os.Handler;

import com.punuo.sip.request.BaseSipRequest;

import org.zoolu.sip.message.Message;

import java.util.concurrent.Executor;

/**
 * Created by han.chen.
 * Date on 2019/4/23.
 **/
public class SipExecutorDelivery {
    /**
     * Used for posting responses, typically to the main thread.
     */
    private final Executor mResponsePoster;

    public SipExecutorDelivery(final Handler handler) {
        mResponsePoster = new Executor() {
            @Override
            public void execute(Runnable command) {
                handler.post(command);
            }
        };
    }

    public void postResponse(BaseSipRequest sipRequest, Message message) {
        mResponsePoster.execute(new ResponseDeliveryRunnable(sipRequest, message, null));
    }

    public void postError(BaseSipRequest sipRequest, Message message, Exception error) {
        mResponsePoster.execute(new ResponseDeliveryRunnable(sipRequest, message, error));
    }

    private class ResponseDeliveryRunnable implements Runnable {
        private BaseSipRequest mSipRequest;
        private final Message mMessage;
        private final Exception mException;

        public ResponseDeliveryRunnable(BaseSipRequest sipRequest, Message message, Exception exception) {
            mSipRequest = sipRequest;
            mMessage = message;
            mException = exception;
        }

        @Override
        public void run() {
            if (mException != null) {
                mSipRequest.deliverError(mException);
            } else {
                mSipRequest.deliverResponse(mMessage);
            }
        }
    }
}
