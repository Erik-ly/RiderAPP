package com.bophia.erik.ykxrider.websocket;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.bophia.erik.ykxrider.MyApplication;
//import com.example.msi.websocketdemo.websocket.WsManager;
import com.bophia.erik.ykxrider.service.MainService;
import com.orhanobut.logger.Logger;

/**
 * 可用网络的切换时，通过广播来监听实现重连
 */

public class NetStatusReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {

            // 获取网络连接管理器
            ConnectivityManager connectivityManager
                = (ConnectivityManager) MyApplication.getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
            // 获取当前网络状态信息
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();

            if (info != null && info.isAvailable()) {
                Logger.t("WsManager").d("监听到可用网络切换,调用重连方法");
                MainService.getInstance().reconnect();//wify 4g切换重连websocket
            }

        }
    }
}
