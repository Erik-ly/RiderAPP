package com.bophia.erik.ykxrider.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bophia.erik.ykxrider.MainActivity;

/**
 * 广播接收器，用于APP开机自启
 */
public class BootBroadcastReceiver extends BroadcastReceiver {


    static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(ACTION)) {
            Intent mainActivityIntent  = new Intent(context, MainActivity.class);  // 要启动的Activity
            Intent serviceIntent = new Intent(context,MainService.class);   //要启动的服务
            //1.如果自启动APP，参数为需要自动启动的应用包名
//            Intent appIntent = context.getPackageManager().getLaunchIntentForPackage("com.bophia.erik.ykxrider");
            //下面这句话必须加上才能开机自动运行app的界面
            mainActivityIntent .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            appIntent .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //2.如果自启动Activity
            context.startActivity(mainActivityIntent );
            //3.如果自启动服务
            context.startService(serviceIntent);
        }

    }
}
