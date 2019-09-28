package com.bophia.erik.ykxrider;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bophia.erik.ykxrider.UI.distribution.DistributionFragment;
import com.bophia.erik.ykxrider.UI.hall.HallFragment;
import com.bophia.erik.ykxrider.UI.mine.MineFragment;
import com.bophia.erik.ykxrider.UI.neworder.NewOrderFragment;
import com.bophia.erik.ykxrider.service.MainService;
//import com.bophia.erik.ykxrider.websocket.WsManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.os.Process;

import static android.app.Notification.VISIBILITY_SECRET;

/**
 * MainActivity
 */
public class MainActivity extends AppCompatActivity {

    private FragmentManager manager;
    private NewOrderFragment newOrderFragment;
    private DistributionFragment distributionFragment;
    private HallFragment hallFragment;
    private MineFragment mineFragment;

    private TextView tv_actionBarTitle;

    //服务相关
    private MainService mMainService;
    private boolean mIsConnected = false;
    private boolean mIsBind = false;
    private final int NOTIFICATION_ID = 98;
    private boolean mIsForegroundService = false;

    //高德地图需要进行检测的权限数组
    protected String[] needPermissions = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.READ_PHONE_STATE
    };

    private static final int PERMISSON_REQUESTCODE = 0;

    //判断是否需要检测，防止不停的弹框
    private boolean isNeedCheck = true;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mMainService = ((MainService.ServiceHelp)iBinder).getMainService();
            if (mMainService != null){
                MainActivity.this.mIsConnected = true;
            }else{
                new Throwable("服务绑定失败");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            MainActivity.this.mIsConnected = false;
            Log.i("zyq","ServiceConnection:onServiceDisconnected");

        }
    };

    //底部按钮
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentTransaction transaction = manager.beginTransaction();
            switch (item.getItemId()) {
                case R.id.navigation_no_distribution:
                    tv_actionBarTitle.setText(getString(R.string.title_new_order));

//                    if (noDistributionFragment == null){
//                        noDistributionFragment = new NoDistributionFragment();
//                        transaction.add(R.id.container,noDistributionFragment);
//                    }
//
//                    updateFragment(transaction);
//                    transaction.show(noDistributionFragment);
//                    transaction.commit();

                    updateFragment(transaction);
                    newOrderFragment = new NewOrderFragment();
                    transaction.add(R.id.container, newOrderFragment);
                    transaction.show(newOrderFragment);
                    transaction.commit();

                    return true;

                case R.id.navigation_hall:
                    tv_actionBarTitle.setText(getString(R.string.title_hall));

                    updateFragment(transaction);
                    hallFragment = new HallFragment();
                    transaction.add(R.id.container, hallFragment);
                    transaction.show(hallFragment);
                    transaction.commit();

                    return true;

                case R.id.navigation_distribution:
                    tv_actionBarTitle.setText(getString(R.string.title_distribution));

                    if (distributionFragment == null){
                        distributionFragment = new DistributionFragment();
                        transaction.add(R.id.container,distributionFragment);
                    }

                    updateFragment(transaction);
                    transaction.show(distributionFragment);
                    transaction.commit();

                    return true;

                case R.id.navigation_mine:
                    tv_actionBarTitle.setText(getString(R.string.title_mine));

                    if (mineFragment == null){
                        mineFragment = new MineFragment();
                        transaction.add(R.id.container,mineFragment);
                    }

                    updateFragment(transaction);
                    transaction.show(mineFragment);
                    transaction.commit();

                    return true;
            }
            return false;
        }
    };

    //更新fragment方法
    private void updateFragment(FragmentTransaction transaction){
        if (newOrderFragment != null){
            transaction.remove(newOrderFragment);
        }

        if (hallFragment != null){
            transaction.remove(hallFragment);
        }

        if (distributionFragment != null){
            transaction.hide(distributionFragment);
        }

        if (mineFragment != null){
            transaction.hide(mineFragment);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //自定义ActionBar
        setCustomActionBar();
        tv_actionBarTitle.setText(getString(R.string.title_new_order));

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        manager = getSupportFragmentManager();

        FragmentTransaction transaction = manager.beginTransaction();
        if (newOrderFragment == null){
            newOrderFragment = new NewOrderFragment();
        }
        transaction.replace(R.id.container, newOrderFragment);

        //创建“大厅”页面
        if (hallFragment == null){
            hallFragment = new HallFragment();
        }
        transaction.add(R.id.container,hallFragment);
        transaction.hide(hallFragment);

        //创建“配送”页面
//        if (distributionFragment == null){
//            distributionFragment = new DistributionFragment();
//        }
//        transaction.add(R.id.container,distributionFragment);
//        transaction.hide(distributionFragment);

        transaction.commit();


//        if(shouldInit()){
//
//            //建立WebSocket连接
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//
//                    WsManager.getInstance().init();
//                    System.out.println("WebSocket:==================MainActivity:建立WebSocket连接");
//
//                }
//            }).start();
//
//            Boolean init = shouldInit();
//        }


    }

    //设置自定义ActionBar
    private void setCustomActionBar(){

        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        View mActionBarView = LayoutInflater.from(this).inflate(R.layout.actionbar_layout,null);
        tv_actionBarTitle = mActionBarView.findViewById(R.id.tv_actionBarTitle);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setElevation(0);
        actionBar.setCustomView(mActionBarView,lp);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
    }

    //退出前询问
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK){
            AlertDialog.Builder isExit = new AlertDialog.Builder(this);
            isExit.setTitle("退出提醒");
            isExit.setMessage("确定退出吗？");
            isExit.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    MainActivity.this.finish();
                }
            });
            isExit.setNegativeButton("取消",null);
            isExit.show();
        }
        return false;
    }


    //检测是否需要初始化websocket
    private boolean shouldInit() {
        ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = getPackageName();
        int myPid = Process.myPid();

        System.out.println("myPid:======" + myPid);

        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }

    //服务相关
    @Override
    protected void onStart() {
        super.onStart();

        if(null == startService(new Intent(MainActivity.this,MainService.class))){
            new Throwable("无法启动服务");
        }
        mIsBind = bindService(new Intent(MainActivity.this,MainService.class),mConnection, Context.BIND_AUTO_CREATE);

        if(mIsBind && mMainService != null && !mIsForegroundService){
            mMainService.startForeground(NOTIFICATION_ID,getNotification());
            mIsForegroundService = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mIsBind && mMainService != null && !mIsForegroundService){
            mMainService.startForeground(NOTIFICATION_ID,getNotification());
            mIsForegroundService = true;
        }

        //检测是否开启定位服务
        if (Build.VERSION.SDK_INT >= 23 && getApplicationInfo().targetSdkVersion >= 23){
            if (isNeedCheck){
                checkPermissions(needPermissions);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(mIsBind && mMainService != null && !mIsForegroundService){
            mMainService.startForeground(NOTIFICATION_ID,getNotification());
            mIsForegroundService = true;
        }
    }

    //通知栏信息
    private Notification getNotification(){

        Notification notification;
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("channel_id","channel_name",NotificationManager.IMPORTANCE_HIGH);
            AudioAttributes att = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            channel.canBypassDnd();
            channel.setLockscreenVisibility(VISIBILITY_SECRET);

            manager.createNotificationChannel(channel);

            notification = new Notification.Builder(MainActivity.this)
                    .setAutoCancel(true)
                    .setChannelId("channel_id")
                    .setContentTitle("运行状态提示")
                    .setContentText("一刻行骑手正在运行")
                    .setSmallIcon(R.drawable.ic_icon)
                    .build();

        }else {
            notification = new Notification.Builder(MainActivity.this)
                    .setSmallIcon(R.drawable.ic_icon)//设置小图标
                    .setContentTitle("运行状态提示")
                    .setContentText("一刻行骑手正在运行")
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setAutoCancel(true)
                    .build();
        }
        return notification;

    }

    //检验权限
    private void checkPermissions(String... permissions){

        try {
            if (Build.VERSION.SDK_INT >= 23 && getApplicationInfo().targetSdkVersion >= 23){
                List<String> needRequestPermissonList = findDeniedPermissions(permissions);
                if (null != needRequestPermissonList && needRequestPermissonList.size() > 0){
                    String[] array = needRequestPermissonList.toArray(new String[needRequestPermissonList.size()]);
                    Method method = getClass().getMethod("requestPermissions",new Class[]{String[].class,
                            int.class});
                    method.invoke(this,array,PERMISSON_REQUESTCODE);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    //获取权限集中需要申请权限的列表
    private List<String> findDeniedPermissions(String[] permissions){
        List<String> needRequestPermissonList = new ArrayList<String>();
        if (Build.VERSION.SDK_INT >= 23
                && getApplicationInfo().targetSdkVersion >= 23){
            try {
                for (String perm : permissions) {
                    Method checkSelfMethod = getClass().getMethod("checkSelfPermission", String.class);
                    Method shouldShowRequestPermissionRationaleMethod = getClass().getMethod("shouldShowRequestPermissionRationale",
                            String.class);
                    if ((Integer)checkSelfMethod.invoke(this, perm) != PackageManager.PERMISSION_GRANTED
                            || (Boolean)shouldShowRequestPermissionRationaleMethod.invoke(this, perm)) {
                        needRequestPermissonList.add(perm);
                    }
                }
            } catch (Throwable e) {

            }
        }
        return needRequestPermissonList;
    }

    //检测是否所有的权限都已经授权
    private boolean verifyPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @TargetApi(23)
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] paramArrayOfInt) {
        if (requestCode == PERMISSON_REQUESTCODE) {
            if (!verifyPermissions(paramArrayOfInt)) {
                showMissingPermissionDialog();
                isNeedCheck = false;
            }
        }
    }

    //显示提示信息
    private void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.notifyTitle);
        builder.setMessage(R.string.notifyMsg);

        // 拒绝, 退出应用
        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

        builder.setPositiveButton(R.string.setting,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startAppSettings();
                    }
                });

        builder.setCancelable(false);

        builder.show();
    }

    //启动应用的设置
    private void startAppSettings() {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

}
