package com.bophia.erik.ykxrider.service;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;

import com.bophia.erik.ykxrider.UI.hall.HallFragment;
import com.bophia.erik.ykxrider.MyApplication;
import com.bophia.erik.ykxrider.UI.neworder.NewOrderFragment;
import com.bophia.erik.ykxrider.UI.neworder.OrderInfoActivity;
import com.bophia.erik.ykxrider.R;
import com.bophia.erik.ykxrider.entity.Order;
import com.bophia.erik.ykxrider.utils.HttpRequest;
import com.bophia.erik.ykxrider.utils.MapUtils;
import com.bophia.erik.ykxrider.utils.PlaySoundUtils;
import com.bophia.erik.ykxrider.websocket.common.CallbackDataWrapper;
import com.bophia.erik.ykxrider.websocket.common.CallbackWrapper;
import com.bophia.erik.ykxrider.websocket.common.ICallback;
import com.bophia.erik.ykxrider.websocket.common.IWsCallback;
import com.bophia.erik.ykxrider.websocket.common.WsStatus;
import com.bophia.erik.ykxrider.websocket.request.Action;
import com.bophia.erik.ykxrider.websocket.request.Request;
import com.google.gson.Gson;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketState;
import com.orhanobut.logger.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static android.app.Notification.VISIBILITY_SECRET;

public class MainService extends Service implements MediaPlayer.OnCompletionListener{

    private static MainService mainServiceInstance;

    private int a = 0;//累加计算

    private List<Order> lists = new ArrayList<Order>();//存放新订单信息
    private List<String> orderIdLists = new ArrayList<String>();//存放大厅订单id信息
    private List<String> throwOutOrderIdList = new ArrayList<>();//抛出订单id信息

    private Long nowTimeMill;//当前时间毫秒数
    private Long time;//当前时间与最新订单的时间差
    private MediaPlayer mediaPlayer;//音乐播放器
    private static int newOrderId = -1;//最新订单ID

    private ServiceHelp mHelper = new ServiceHelp();

    private SharedPreferences sp;
    private int relationSalepoint;
    private int distributorId;
    private String distributorPhone;

    //高德定位
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    private AMapLocation last_AMapLocation = null;

    public static List<String> newOrderList;

    //webSocket相关
    private WebSocket ws;
    private WsListener mListener;
    private WsStatus mStatus;
    private AtomicLong seqId = new AtomicLong(SystemClock.uptimeMillis());//每个请求的唯一标识
    private Map<Long, CallbackWrapper> callbacks = new HashMap<>();
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final String TAG = this.getClass().getSimpleName();
    private String webSocketURL;

    private static final int CONNECT_TIMEOUT = 5000;
    private static final int FRAME_QUEUE_SIZE = 5;
    private static final long HEARTBEAT_INTERVAL = 30000;//心跳间隔
    private static final int REQUEST_TIMEOUT = 10000;//请求超时时间
    private final int SUCCESS_HANDLE = 0x01;
    private final int ERROR_HANDLE = 0x02;
    //webSocket相关

    //播放完成
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

        stopSelf();
    }

    public class ServiceHelp extends Binder {
        public MainService getMainService(){
            return MainService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mHelper;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //服务实例
        mainServiceInstance = this;

        newOrderList = new ArrayList<>();

        //获取配送员信息
        sp = getSharedPreferences("editor", Context.MODE_PRIVATE);
        relationSalepoint = sp.getInt("relationSalepoint",-1);
        distributorId = sp.getInt("id",-1);
        distributorPhone = sp.getString("phone","NULL");

        //音乐播放器
        mediaPlayer = MediaPlayer.create(this,R.raw.threemin);
        mediaPlayer.setOnCompletionListener(this);

        //轮询线程
        PollThread pollThread = new PollThread();
        pollThread.setDaemon(true);
        pollThread.start();

        //累加线程
        ServiceThread thread = new ServiceThread();
        thread.setDaemon(true);
        thread.start();

        /****************************************WebSocket***************************************************************/

        //初始化webSocket
        initWebSocket();

        /****************************************WebSocket***************************************************************/


    }

    //获取 MainService 实例
    public static MainService getInstance(){
        return mainServiceInstance;
    }

    /****************************************WebSocket***************************************************************/

    //初始化webSocket
    public void initWebSocket() {

        try {

            webSocketURL = getResources().getString(R.string.webSocketURL);
            ws = new WebSocketFactory().createSocket(webSocketURL, CONNECT_TIMEOUT)
                    .setFrameQueueSize(FRAME_QUEUE_SIZE)//设置帧队列最大值为5
                    .setMissingCloseFrameAllowed(false)//设置不允许服务端关闭连接却未发送关闭帧
                    .addListener(mListener = new WsListener())//添加回调监听
                    .connectAsynchronously();//异步连接
            setStatus(WsStatus.CONNECTING);

            Logger.t(TAG).d("第一次连接");

            System.out.println("WebSocket:==================MainService:第一次连接");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 继承默认的监听空实现WebSocketAdapter,重写我们需要的方法
     * onTextMessage 收到文字信息
     * onConnected 连接成功
     * onConnectError 连接失败
     * onDisconnected 连接关闭
     */
    class WsListener extends WebSocketAdapter {

        @Override
        public void onTextMessage(WebSocket websocket, String text) throws Exception {
            super.onTextMessage(websocket, text);
            Logger.t(TAG).d("receiverMsg:%s", text);

            System.out.println("WebSocket:==================MainService:接收到消息：" + text);

            //解析消息
            JSONObject msgObject = new JSONObject(text);

            String type = msgObject.optString("type");//信息类型，现有类型：Register：注册sessionId；NewOrder：新订单；HallOrder：大厅订单
            String msg = msgObject.getString("msg");//信息

            System.out.println("WebSocket:==================MainService:type：" + type + " msg:" + msg);

            //注册本次会话
            if (type.equals("Register")){

                doAuth(msg);//授权

            //新订单或大厅订单
            }else if (type.equals("NewOrder") || type.equals("HallOrder")){

                //获取MyApplication
                MyApplication myApplication = (MyApplication)getApplicationContext();

                //判断msg是否已经在wsOrderList中
                if (!myApplication.wsOrderList.contains(msg)){

                    //消息通知
                    wsNotification(MainService.this,msg,type);

                    //将WebSocket通知过的订单放在wsOrderList中
                    myApplication.wsOrderList.add(msg);

                }

                //当wsOrderList中的订单超过1000个时清空
                if (myApplication.wsOrderList.size()>1000){
                    myApplication.wsOrderList.clear();
                }
            }
        }

        @Override
        public void onConnected(WebSocket websocket, Map<String, List<String>> headers)
                throws Exception {
            super.onConnected(websocket, headers);
            Logger.t(TAG).d("连接成功");
            setStatus(WsStatus.CONNECT_SUCCESS);
            cancelReconnect();//连接成功的时候取消重连,初始化连接次数

            System.out.println("WebSocket:==================MainService:onConnected：连接成功");

        }

        @Override
        public void onConnectError(WebSocket websocket, WebSocketException exception)
                throws Exception {
            super.onConnectError(websocket, exception);
            Logger.t(TAG).d("连接错误");
            setStatus(WsStatus.CONNECT_FAIL);
            reconnect();//连接错误的时候调用重连方法
        }

        @Override
        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer)
                throws Exception {
            super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
            Logger.t(TAG).d("断开连接");
            setStatus(WsStatus.CONNECT_FAIL);
            reconnect();//连接断开的时候调用重连方法
        }
    }

    //授权，添加sessionId
    private void doAuth(final String sessionId) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                String result = HttpRequest.sendGet(getString(R.string.httpsUrl) + "AddSessionId","staffId=" + distributorId + "&sessionId=" + sessionId);

                int state = -1;
                String message = null;
                try {
                    JSONObject updateDisOrderReceipt = new JSONObject(result);

                    state = updateDisOrderReceipt.optInt("state");
                    message = updateDisOrderReceipt.optString("message");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (state == 0){

                    System.out.println("WebSocket:==================MainService:doAuth：注册成功");

                    //开始心跳
                    startHeartbeat();

                }else {
                    System.out.println("WebSocket:==================MainService:doAuth:注册失败：" + message);

                    /**
                     * 播放声音提示
                     */

                    //断开连接
                    disconnect();

                    //重新初始化WebSocket
                    initWebSocket();

                }
            }
        }).start();

    }

    //关闭WebSocket连接
    public void disconnect() {
        if (ws != null) {
            ws.disconnect();
        }
    }

    //超时和回调的处理
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS_HANDLE:
                    CallbackDataWrapper successWrapper = (CallbackDataWrapper) msg.obj;
                    successWrapper.getCallback().onSuccess(successWrapper.getData());
                    break;
                case ERROR_HANDLE:
                    CallbackDataWrapper errorWrapper = (CallbackDataWrapper) msg.obj;
                    errorWrapper.getCallback().onFail((String) errorWrapper.getData());
                    break;
            }
        }
    };

    //WsStatus的get、set方法
    private void setStatus(WsStatus status) {
        this.mStatus = status;
    }

    private WsStatus getStatus() {
        return mStatus;
    }

    /**
     * 心跳
     *
     * 授权成功后开始心跳,心跳连续失败三次开始重连,在重连时候会取消心跳.
     * 当重连成功的时候在连接成功回调会再次进行授权然后授权成功后会再次开启心跳就形成了一个循环.
     *
     */
    private int reconnectCount = 0;//重连次数
    private long minInterval = 3000;//重连最小时间间隔
    private long maxInterval = 60000;//重连最大时间间隔

    private int heartbeatFailCount = 0;
    private Runnable heartbeatTask = new Runnable() {
        @Override
        public void run() {

            //ping
            WebSocketState state = ws.sendPing().getState();
//            System.out.println("WebSocket:==================MainService:heartbeatTask：WebSocketState:" + state);

            if (state == WebSocketState.OPEN){
                heartbeatFailCount = 0;
                Logger.t(TAG).d("心跳成功！");
//                System.out.println("WebSocket:==================MainService:heartbeatTask：心跳成功");
            }else {
                heartbeatFailCount++;
                if (heartbeatFailCount >= 3) {

                    //失败3次后重连
                    reconnect();

                    Logger.t(TAG).d("心跳失败：重新连接");
//                    System.out.println("WebSocket:==================MainService:heartbeatTask：心跳失败：重新连接");
                }
            }

            mHandler.postDelayed(this, HEARTBEAT_INTERVAL);
        }
    };

    //开始心跳
    private void startHeartbeat() {

        Logger.t(TAG).d("开启心跳");
//        System.out.println("WebSocket:==================MainService:heartbeatTask：开始心跳");

        mHandler.postDelayed(heartbeatTask, HEARTBEAT_INTERVAL);
    }

    //结束心跳
    private void cancelHeartbeat() {
        heartbeatFailCount = 0;
        mHandler.removeCallbacks(heartbeatTask);
    }

    private Runnable mReconnectTask = new Runnable() {

        @Override
        public void run() {
            try {
                ws = new WebSocketFactory().createSocket(webSocketURL, CONNECT_TIMEOUT)
                        .setFrameQueueSize(FRAME_QUEUE_SIZE)//设置帧队列最大值为5
                        .setMissingCloseFrameAllowed(false)//设置不允许服务端关闭连接却未发送关闭帧
                        .addListener(mListener = new WsListener())//添加回调监听
                        .connectAsynchronously();//异步连接
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    //重连
    public void reconnect() {
        if (!isNetConnect()) {
            reconnectCount = 0;
            Logger.t(TAG).d("重连失败网络不可用");
            return;
        }

        //这里其实应该还有个用户是否登录了的判断 因为当连接成功后我们需要发送用户信息到服务端进行校验
        if (ws != null &&
                !ws.isOpen() &&//当前连接断开了
                getStatus() != WsStatus.CONNECTING) {//不是正在重连状态

            reconnectCount++;
            setStatus(WsStatus.CONNECTING);
            cancelHeartbeat();

            long reconnectTime = minInterval;
            if (reconnectCount > 3) {
                long temp = minInterval * (reconnectCount - 2);
                reconnectTime = temp > maxInterval ? maxInterval : temp;
            }

            Logger.t(TAG).d("准备开始第%d次重连,重连间隔%d -- url:%s", reconnectCount, reconnectTime, webSocketURL);
            mHandler.postDelayed(mReconnectTask, reconnectTime);
        }
    }

    //取消重连
    private void cancelReconnect() {
        reconnectCount = 0;
        mHandler.removeCallbacks(mReconnectTask);
    }

    //检测网络连接状态
    private boolean isNetConnect() {
        ConnectivityManager connectivity = (ConnectivityManager) MyApplication.getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // 当前所连接的网络可用
                    Logger.t(TAG).d("当前所连接的网络可用");
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 发送消息
     */
    public void sendReq(Action action, Object req, ICallback callback) {
        sendReq(action, req, callback, REQUEST_TIMEOUT);
    }

    public void sendReq(Action action, Object req, ICallback callback, long timeout) {
        sendReq(action, req, callback, timeout, 1);
    }

    /**
     * @param action   Action
     * @param req      请求参数
     * @param callback 回调
     * @param timeout  超时时间
     * @param reqCount 请求次数
     */
    @SuppressWarnings("unchecked")
    private <T> void sendReq(Action action, T req, final ICallback callback, final long timeout, int reqCount) {
        if (!isNetConnect()) {
            callback.onFail("网络不可用");
            return;
        }

        if (WsStatus.AUTH_SUCCESS.equals(getStatus()) || Action.LOGIN.equals(action)) {
            Request request = new Request.Builder<T>()
                    .action(action.getAction())
                    .reqEvent(action.getReqEvent())
                    .seqId(seqId.getAndIncrement())
                    .reqCount(reqCount)
                    .req(req)
                    .build();

            ScheduledFuture timeoutTask = enqueueTimeout(request.getSeqId(), timeout);//添加超时任务

            IWsCallback tempCallback = new IWsCallback() {

                @Override
                public void onSuccess(Object o) {
                    mHandler.obtainMessage(SUCCESS_HANDLE, new CallbackDataWrapper(callback, o))
                            .sendToTarget();
                }


                @Override
                public void onError(String msg, Request request, Action action) {
                    mHandler.obtainMessage(ERROR_HANDLE, new CallbackDataWrapper(callback, msg))
                            .sendToTarget();
                }


                @Override
                public void onTimeout(Request request, Action action) {
                    timeoutHandle(request, action, callback, timeout);
                }
            };

            callbacks.put(request.getSeqId(),
                    new CallbackWrapper(tempCallback, timeoutTask, action, request));

            Logger.t(TAG).d("send text : %s", new Gson().toJson(request));
            ws.sendText(new Gson().toJson(request));
        } else {
            callback.onFail("用户授权失败");
        }
    }

    /**
     * 添加超时任务
     */
    private ScheduledFuture enqueueTimeout(final long seqId, long timeout) {
        return executor.schedule(new Runnable() {
            @Override
            public void run() {
                CallbackWrapper wrapper = callbacks.remove(seqId);
                if (wrapper != null) {
                    Logger.t(TAG).d("(action:%s)第%d次请求超时", wrapper.getAction().getAction(), wrapper.getRequest().getReqCount());
                    wrapper.getTempCallback().onTimeout(wrapper.getRequest(), wrapper.getAction());
                }
            }
        }, timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * 超时处理
     */
    private void timeoutHandle(Request request, Action action, ICallback callback, long timeout) {
        if (request.getReqCount() > 3) {
            Logger.t(TAG).d("(action:%s)连续3次请求超时 执行http请求", action.getAction());
            //走http请求
        } else {
            sendReq(action, request.getReq(), callback, timeout, request.getReqCount() + 1);
            Logger.t(TAG).d("(action:%s)发起第%d次请求", action.getAction(), request.getReqCount());
        }
    }

    /****************************************WebSocket***************************************************************/

    //轮询线程
    public class PollThread extends Thread{

        public void run(){
            while (true){

                queryNewOrder();

                try {
                    sleep(15000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

//                System.out.println("MainService:==========a=轮询服务正在运行");

            }
        }
    }

    //查询新订单方法
    public void queryNewOrder() {

//        System.out.println("MainService:==========轮询新订单");

        lists.clear();

        new Thread(new Runnable() {
            @Override
            public void run() {

                synchronized (MainService.this){

                    //获取所有与该销售点相关的未配送订单
                    String httpsUrl = getString(R.string.httpsUrl);
                    String url = httpsUrl + "GetAllNoDisOrderByRelationSP";

                    String result = HttpRequest.sendGet(url,"relationSalepoint=" + relationSalepoint + "&distributorPhone=" +distributorPhone);

                    //判断是否联网正常
                    if (result.equals("404")){
                        String uri = "android.resource://" + "com.bophia.erik.ykxrider" + "/" + R.raw.networkerror;
                        playSound(uri);
                        return;
                    }

                    try {

                        JSONObject NoDisOrder = new JSONObject(result);

                        JSONArray content = NoDisOrder.optJSONArray("content");
                        JSONArray hallOrder = NoDisOrder.optJSONArray("hallOrder");

                        for (int i=0; i<content.length(); i++){
                            JSONObject orderObject = content.getJSONObject(i);

                            Order order = new Order();
                            order.setId(orderObject.optInt("id"));
                            order.setOrderId(orderObject.optString("orderId"));
                            order.setTime(orderObject.optString("time"));
                            order.setPaymentTime(orderObject.optString("paymentTime"));
                            order.setBooking(orderObject.optString("booking"));

                            lists.add(order);
                        }

                        //如果有数据
                        if (lists.size()>=1){

                            //获取当前时间
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date date = new Date(System.currentTimeMillis());
                            String nowTime = simpleDateFormat.format(date);

                            //第一个订单的（最新订单）的时间
//                            String orderTime = lists.get(0).getTime();
                            String orderTime = lists.get(0).getPaymentTime();
                            String booking = lists.get(0).getBooking();

                            //计算时间差
                            try {
                                nowTimeMill = simpleDateFormat.parse(nowTime).getTime();

                                if (nowTimeMill == null){
                                    return;
                                }else {
                                    Long orderTimeMill = simpleDateFormat.parse(orderTime).getTime();
                                    time = nowTimeMill - orderTimeMill;

//                                    System.out.println("MainService:==========新订单时间: nowTimeMill" + nowTimeMill + " orderTimeMill:" + orderTimeMill + " time:" + time);

                                }

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            //响铃
                            //第一次响铃，通过订单的自增id进行判断
                            if (newOrderId != lists.get(0).getId()){

//                                MyApplication myApplication = (MyApplication) getApplicationContext();
//
//                                //判断是否已经有通知
//                                if (!myApplication.newOrderList.contains(newOrderId)){
//                                    myApplication.newOrderList.add(lists.get(0).getOrderId());
//
//                                    if (myApplication.newOrderList.size() >= 100){
//                                        myApplication.newOrderList.clear();
//                                    }
//
//                                    screenOn();
//                                    showNotification(MainService.this,"您有新的订单信息",lists.get(0).getOrderId());
////                                String uri = "android.resource://" + "com.bophia.erik.ykxrider" + "/" + R.raw.neworder;
//                                    String uri = "android.resource://" + "com.bophia.erik.ykxrider" + "/" + R.raw.nodistributionorder;
//                                    playSound(uri);
//
//                                    newOrderId = lists.get(0).getId();//将id修改为最新的
//                                }


//                                MyApplication myApplication = (MyApplication) getApplicationContext();
//                                String orderId = lists.get(0).getOrderId();
//                                if (!myApplication.wsOrderList.contains(orderId)){
//
//                                    screenOn();
//                                    showNotification(MainService.this,"您有新的订单信息",lists.get(0).getOrderId());
//                                    String uri = "android.resource://" + "com.bophia.erik.ykxrider" + "/" + R.raw.neworder;
//                                    playSound(uri);
//
//                                    myApplication.wsOrderList.add(orderId);
//
//                                }

                                screenOn();
                                showNotification(MainService.this,"您有新的订单信息",lists.get(0).getOrderId());
                                String uri = "android.resource://" + "com.bophia.erik.ykxrider" + "/" + R.raw.neworder;
                                playSound(uri);

                                newOrderId = lists.get(0).getId();//将id修改为最新的

                                //2分钟
                            }else if ((time>=105000) && (time<=120000) && booking.equals("尽快送达")){
                                screenOn();
                                showNotification(MainService.this,"新的订单已经超过两分钟",lists.get(0).getOrderId());
                                String uri = "android.resource://" + "com.bophia.erik.ykxrider" + "/" + R.raw.neworder2min;
                                playSound(uri);

                                //4分钟
                            }else if ((time>=225000) && (time<=240000) && booking.equals("尽快送达")){
                                screenOn();
                                showNotification(MainService.this,"新的订单已经超过4分钟",lists.get(0).getOrderId());
                                String uri = "android.resource://" + "com.bophia.erik.ykxrider" + "/" + R.raw.neworder4min;
                                playSound(uri);

                                //6分钟
                            }else if ((time>=345000) && (time<=360000) && booking.equals("尽快送达")){
                                screenOn();
                                showNotification(MainService.this,"新的订单已经超过6分钟",lists.get(0).getOrderId());
                                String uri = "android.resource://" + "com.bophia.erik.ykxrider" + "/" + R.raw.neworder6min;
                                playSound(uri);

                                //8分钟
                            }else if ((time>=465000) && (time<=480000) && booking.equals("尽快送达")){
                                screenOn();
                                showNotification(MainService.this,"新的订单已经超过8分钟",lists.get(0).getOrderId());
                                String uri = "android.resource://" + "com.bophia.erik.ykxrider" + "/" + R.raw.neworder8min;
                                playSound(uri);

                                //10分钟
                            }else if ((time>=585000) && (time<=600000) && booking.equals("尽快送达")){
                                screenOn();
                                showNotification(MainService.this,"新的订单已经超过10分钟",lists.get(0).getOrderId());
                                String uri = "android.resource://" + "com.bophia.erik.ykxrider" + "/" + R.raw.neworder10min;
                                playSound(uri);

                                //12分钟
                            }else if ((time>=705000) && (time<=720000) && booking.equals("尽快送达")){
                                screenOn();
                                showNotification(MainService.this,"新的订单已经超过12分钟",lists.get(0).getOrderId());
                                String uri = "android.resource://" + "com.bophia.erik.ykxrider" + "/" + R.raw.neworder12min;
                                playSound(uri);


                                //14分钟
                            }else if ((time>=825000) && (time<=840000) && booking.equals("尽快送达")){
                                screenOn();
                                showNotification(MainService.this,"新的订单已经超过14分钟",lists.get(0).getOrderId());
                                String uri = "android.resource://" + "com.bophia.erik.ykxrider" + "/" + R.raw.neworder14min;
                                playSound(uri);

                            }
                        }

//                        if (NewOrderFragment.getInstance().getActivity() == null){
//                            return;
//                        }
//
//                        //“新订单”界面同刷新
//                        NewOrderFragment.getInstance().getActivity().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                NewOrderFragment.getInstance().onResume();
//
//                                System.out.println("MainService:========================刷新新订单界面");
//
//                            }
//                        });

                        if (NewOrderFragment.getInstance().getActivity() != null){
                            //“新订单”界面同刷新
                            NewOrderFragment.getInstance().getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    NewOrderFragment.getInstance().onResume();

                                    System.out.println("MainService:========================刷新新订单界面");

                                }
                            });
                        }

                        //“大厅”订单响铃
                        if (hallOrder != null && hallOrder.length() > 0){

                            for (int i=0; i<hallOrder.length(); i++){
                                JSONObject hallOrderObject = hallOrder.getJSONObject(i);

                                String orderId = hallOrderObject.optString("orderId");
                                String disPhone = hallOrderObject.optString("distributorPhone");

//                                //过滤掉自己抛出的订单
//                                if (!disPhone.equals(distributorPhone) && !orderIdLists.contains(orderId)){
//
//                                    orderIdLists.add(orderId);
//
//                                    screenOn();
//                                    showNotification(MainService.this,"一刻行骑手大厅来新订单了",orderId);
//                                    PlaySoundUtils.playSound(MainService.this,R.raw.hallorder);
//
//                                }

                                if (!orderIdLists.contains(orderId)){
                                    screenOn();
                                    showNotification(MainService.this,"一刻行骑手大厅来新订单了",orderId);
                                    PlaySoundUtils.playSound(MainService.this,R.raw.hallorder);
                                }

                                orderIdLists.add(orderId);

                            }
                        }

//                        if (HallFragment.getInstance().getActivity() == null){
//                            return;
//                        }
//
//                        //刷新“大厅”
//                        HallFragment.getInstance().getActivity().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                HallFragment.getInstance().onResume();
//
//                                System.out.println("MainService:==========刷新大厅");
//
//                            }
//                        });

                        if (HallFragment.getInstance().getActivity() != null){
                            //刷新“大厅”
                            HallFragment.getInstance().getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    HallFragment.getInstance().onResume();

                                    System.out.println("MainService:==========刷新大厅");

                                }
                            });


                        }

                        //当orderIdLists大于100时，清空
                        if (orderIdLists.size() >= 100){
                            orderIdLists.clear();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    //亮屏
    public void screenOn(){
        KeyguardManager km = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        if (km.inKeyguardRestrictedInputMode()){

            //获取电源管理器对象
            PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
            if (!pm.isScreenOn()){
                @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK,"bright");
                wl.acquire();//点亮屏幕
            }
        }
    }

    //弹出消息
    public void showNotification(Context context,String text,String orderid){
        Intent perIntent = new Intent(context,OrderInfoActivity.class);//跳转界面
        perIntent.putExtra("orderId", orderid);
        perIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//如果MainActivity已经启动了，就不会产生新的activity
        PendingIntent pendingIntent = PendingIntent.getActivities(context,0, new Intent[]{perIntent},PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("channel_id","channel_name",NotificationManager.IMPORTANCE_HIGH);
            AudioAttributes att = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();

            channel.canBypassDnd();//可否绕过请勿打扰模式
            channel.setLockscreenVisibility(VISIBILITY_SECRET);//锁屏显示通知

            manager.createNotificationChannel(channel);

            Notification notification = new Notification.Builder(context)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setChannelId("channel_id")
                    .setContentTitle("订单信息")
                    .setContentText(text)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .build();

            manager.notify(1,notification);

        }else {
            Notification notification = new Notification.Builder(context)
                    .setSmallIcon(R.drawable.ic_icon)//设置小图标
                    .setContentTitle("订单信息")
                    .setContentText(text)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();

            manager.notify(0, notification);
        }
    }

    //播放声音
    public synchronized void playSound(String uri){
        Uri soundUri = Uri.parse(uri);

//        System.out.println("MainService:==============playSound");

        Ringtone mRingtone = RingtoneManager.getRingtone(MainService.this,soundUri);
        if (!mRingtone.isPlaying()){
            mRingtone.play();
        }
    }


    //自定义消息通知
    private void wsNotification(Context context,String orderId,String description){

        String title = "";

        if (description.equals("NewOrder")){
            title = "新订单";
        }else if (description.equals("HallOrder")){
            title = "大厅订单";
        }

        Intent perIntent = new Intent(context,OrderInfoActivity.class);//跳转界面
        perIntent.putExtra("orderId", orderId);
        PendingIntent pendingIntent = PendingIntent.getActivities(context,0, new Intent[]{perIntent},PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            NotificationChannel channel = new NotificationChannel("channel_id","channel_name",NotificationManager.IMPORTANCE_HIGH);
            AudioAttributes att = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();

            channel.canBypassDnd();//可否绕过请勿打扰模式
            channel.setLockscreenVisibility(VISIBILITY_SECRET);//锁屏显示通知

            manager.createNotificationChannel(channel);

            Notification notification = new Notification.Builder(context)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setChannelId("channel_id")
                    .setContentTitle(title)
                    .setContentText("订单号："+orderId)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .build();

            manager.notify(1,notification);

            if (description.equals("NewOrder")){

                PlaySoundUtils.playSound(context,R.raw.neworder);

            }else if (description.equals("HallOrder")){
                PlaySoundUtils.playSound(context,R.raw.hallorder);
            }

        }else {

            int sound = 0;

            if (description.equals("NewOrder")){

                sound = R.raw.neworder;

            }else if (description.equals("HallOrder")){
                sound = R.raw.hallorder;
            }

            Notification notification = new Notification.Builder(context)
                    .setSmallIcon(R.drawable.ic_icon)//设置小图标
                    .setContentTitle(title)
                    .setContentText("订单号："+orderId)
                    .setSound(Uri.parse("android.resource://" + "com.bophia.erik.ykxrider" + "/" + sound))
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();

            manager.notify(0, notification);

        }

        //管理锁屏的一个服务
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        if (km.inKeyguardRestrictedInputMode()){

            //获取电源管理器对象
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (!pm.isScreenOn()){
                @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK,"bright");
                wl.acquire();//点亮屏幕
                wl.release();//任务结束后释放
            }
        }

    }




    //累加线程
    public class ServiceThread extends Thread{
        public volatile boolean exit = false;

        public void run(){
            while (!exit){

                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                a ++;
                if (a == 100){
                    a = 0;
                }

                if (!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                    mediaPlayer.setLooping(true);

//                    System.out.println("a=音乐重启");
                }

//                System.out.println("a=======================" + a);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //音乐播放器
        super.onStartCommand(intent,flags,startId);
        if (!mediaPlayer.isPlaying()){
            mediaPlayer.start();
            mediaPlayer.setLooping(true);
        }

        //高德定位
        initLocation();
        locationClient.startLocation();//启动定位

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

/****************************************WebSocket***************************************************************/
    //断开WebSocket连接
    disconnect();

/****************************************WebSocket***************************************************************/

    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


    /**
     * 初始化定位
     */
    private void initLocation(){

        //初始化client
        if (locationClient == null){
            locationClient = new AMapLocationClient(getApplicationContext());
        }

        //初始化定位参数
        initLocationOption();

        //设置定位参数
        locationClient.setLocationOption(locationOption);

        // 设置定位监听
        locationClient.setLocationListener(locationListener);

    }

    /**
     * 初始化定位参数
     *
     */
    private void initLocationOption(){

        if (locationOption == null){
            locationOption = new AMapLocationClientOption();
        }

        //定位精度:高精度模式
        locationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        locationOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        locationOption.setInterval(15000);//可选，设置定位间隔。默认为2秒,单位：毫秒
        locationOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        locationOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false不使用设备传感器
        locationOption.setLocationCacheEnable(true);//可选，设置是否使用缓存定位，默认为true

    }

    /**
     * 定位监听
     */
    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {

            if (aMapLocation != null){

                if (aMapLocation.getErrorCode() == 0){

//                    System.out.println("定位成功：====================latitude:"
//                            + aMapLocation.getLatitude() + " Longitude:" + aMapLocation.getLongitude() + " aMapLocation:" + aMapLocation);

                    //获取aMapLocation
                    MyApplication myApplication = (MyApplication) getApplicationContext();
                    myApplication.appMapLocation = aMapLocation;

                    //上传定位
                    upLocation(aMapLocation);

                }else {

                    if(aMapLocation.getErrorCode() == 12){

//                        System.out.println("定位失败：================缺少定位权限===========ErrorInfo：" + aMapLocation.getErrorInfo());
                    }

//                    System.out.println("定位失败：===========================" + aMapLocation.getErrorInfo());
                }

            }else {
//                System.out.println("定位失败：=======================aMapLocation为空");
            }
        }
    };


    /**
     * 上传位置信息
     */
    private void upLocation(final AMapLocation aMapLocation){

//        System.out.println("last_AMapLocation_1：=======================" + last_AMapLocation);

        final String url = getString(R.string.httpsUrl) + "UpdateDistributorLocation";

        //如果last_AMapLocation为空，说明是第一次定位
        if (last_AMapLocation == null){
            last_AMapLocation = aMapLocation;
//            System.out.println("last_AMapLocation_2：=======================" + last_AMapLocation);
            return;
        }

        //如果last_AMapLocation不为空，先比较两次定位的距离，如果定位距离大于20米才上传坐标
        if (last_AMapLocation != null && aMapLocation != null){

            double distance = MapUtils.getDistance(last_AMapLocation.getLongitude(),last_AMapLocation.getLatitude(),aMapLocation.getLongitude(),aMapLocation.getLatitude());

//            System.out.println("distance：=======================" + distance);

//            if (distance > 20.0){

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        double longitude = aMapLocation.getLongitude();
                        double latitude = aMapLocation.getLatitude();

                        String result = HttpRequest.sendGet(url,"staffId=" + distributorId + "&longitude=" + longitude + "&latitude=" + latitude);

                        try {
                            JSONObject resultObject = new JSONObject(result);

                            if (resultObject.optInt("state") == 0){
                                last_AMapLocation = aMapLocation;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
//            }
        }

    }



}

