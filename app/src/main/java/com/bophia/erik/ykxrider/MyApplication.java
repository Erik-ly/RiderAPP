package com.bophia.erik.ykxrider;

import android.app.Application;
import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.bophia.erik.ykxrider.service.MainService;
import com.bophia.erik.ykxrider.websocket.ForegroundCallbacks;
//import com.bophia.erik.ykxrider.websocket.WsManager;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Application
 */
public class MyApplication extends Application{

    private static Application context;

    //存放新订单id
    public List<String> newOrderList = new ArrayList<String>();

    //存放WebSocket通知的订单
    public List<String> wsOrderList = new ArrayList<>();

    //定位
    public AMapLocation appMapLocation = null;

    //余额
    public String myBalance = null;

    @Override
    public void onCreate() {
        super.onCreate();

        context = this;

        initLog();

//        initAppStatusListener();

//
//        //建立WebSocket连接
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                WsManager.getInstance().init();
//                System.out.println("WebSocket:==================MainActivity:建立WebSocket连接");
//
//            }
//        }).start();

    }

    /**
     * 初始化日志配置
     */
    private void initLog() {
        Logger.addLogAdapter(new AndroidLogAdapter());
    }


    /**
     * 初始化应用前后台状态监听
     */
    private void initAppStatusListener() {
        ForegroundCallbacks.init(this).addListener(new ForegroundCallbacks.Listener() {
            @Override
            public void onBecameForeground() {
                Logger.t("WsManager").d("应用回到前台调用重连方法");
                MainService.getInstance().reconnect();//重连
            }

            @Override
            public void onBecameBackground() {

            }
        });
    }

    public static Context getContext(){
        return context;
    }

}
