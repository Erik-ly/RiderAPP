package com.bophia.erik.ykxrider.websocket.request;

import com.google.gson.annotations.SerializedName;


/**
 *
 * 协议对应的请求实体类Request,
 * 这里还额外添加了一个没有参加序列化的参数reqCount来表示该请求的请求次数,
 * 在后面请求失败情况下对该请求的处理会用上.
 *
 */

public class Request<T> {
    @SerializedName("action")
    private String action;

    @SerializedName("req_event")
    private int reqEvent;

    @SerializedName("seq_id")
    private long seqId;

    @SerializedName("req")
    private T req;

    private transient int reqCount;

    public Request() {
    }


    public Request(String action, int reqEvent, long seqId, T req, int reqCount) {
        this.action = action;
        this.reqEvent = reqEvent;
        this.seqId = seqId;
        this.req = req;
        this.reqCount = reqCount;
    }


    public String getAction() {
        return action;
    }


    public void setAction(String action) {
        this.action = action;
    }


    public int getReqEvent() {
        return reqEvent;
    }


    public void setReqEvent(int reqEvent) {
        this.reqEvent = reqEvent;
    }


    public long getSeqId() {
        return seqId;
    }


    public void setSeqId(long seqId) {
        this.seqId = seqId;
    }


    public T getReq() {
        return req;
    }


    public void setReq(T req) {
        this.req = req;
    }


    public int getReqCount() {
        return reqCount;
    }


    public void setReqCount(int reqCount) {
        this.reqCount = reqCount;
    }


    public static class Builder<T> {
        private String action;
        private int reqEvent;
        private long seqId;
        private T req;
        private int reqCount;

        public Builder action(String action) {
            this.action = action;
            return this;
        }


        public Builder reqEvent(int reqEvent) {
            this.reqEvent = reqEvent;
            return this;
        }


        public Builder seqId(long seqId) {
            this.seqId = seqId;
            return this;
        }


        public Builder req(T req) {
            this.req = req;
            return this;
        }


        public Builder reqCount(int reqCount) {
            this.reqCount = reqCount;
            return this;
        }

        public Request build() {
            return new Request<T>(action, reqEvent, seqId, req, reqCount);
        }

    }
}
