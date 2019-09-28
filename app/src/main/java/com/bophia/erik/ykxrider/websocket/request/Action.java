package com.bophia.erik.ykxrider.websocket.request;

/**
 *
 * 把action、req_event、响应实体统一用一个枚举类Action来存储,调用者只需根据不同请求传入对应Action即可
 *
 */

public enum Action {

    //登录
    LOGIN("login", 1, null),

    //心跳
    HEARTBEAT("heartbeat", 1, null),

    TEST("test",1,null),

    //同步数据
    SYNC("sync", 1, null);



    private String action;
    private int reqEvent;
    private Class respClazz;


    Action(String action, int reqEvent, Class respClazz) {
        this.action = action;
        this.reqEvent = reqEvent;
        this.respClazz = respClazz;
    }


    public String getAction() {
        return action;
    }


    public int getReqEvent() {
        return reqEvent;
    }


    public Class getRespClazz() {
        return respClazz;
    }



}
