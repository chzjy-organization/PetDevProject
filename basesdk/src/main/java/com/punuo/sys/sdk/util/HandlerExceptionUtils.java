package com.punuo.sys.sdk.util;

import android.text.TextUtils;

import com.google.gson.JsonParseException;
import com.punuo.sys.sdk.R;
import com.punuo.sys.sdk.httplib.ErrorTipException;

import org.apache.http.conn.ConnectTimeoutException;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class HandlerExceptionUtils {

    public static void handleException(Exception exception) {
        int err;
        try {
            throw exception;
        } catch (UnknownHostException e) {
            err = R.string.error_no_net;
        } catch (SocketTimeoutException | ConnectTimeoutException e) {
            err = R.string.error_timeout;
        } catch (JsonParseException e) {
            err = R.string.error_json_parse_exception;
        } catch (NullPointerException e) {
            err = R.string.error_json;
        } catch (ErrorTipException e) {
            String message = e.getMessage();
            if (TextUtils.equals(message, "Unauthorized")) {
                ToastUtils.showToast("设备登陆密码错误,临时提醒,快捷登陆的账号如果没有设置过密码的 会sip注册失败,后续改成密码是固定的即可。");
            } else if (TextUtils.equals(message, "Payment Required")) {
                ToastUtils.showToast("账号不存在");
            } else if (TextUtils.equals(message, "Internal Server Error")) {
                ToastUtils.showToast("服务器内部错误");
            }
            return;
        } catch (Exception e) {
            String message = e.getMessage();
            if (!TextUtils.isEmpty(message) && (message.contains("Socket closed") || message.contains("Canceled"))) {
                return;
            }
            err = R.string.error_unknown;
        }
        ToastUtils.showToast(err);
    }
}
