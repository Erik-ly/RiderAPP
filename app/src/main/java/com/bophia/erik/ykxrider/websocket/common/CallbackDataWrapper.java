package com.bophia.erik.ykxrider.websocket.common;

//import com.example.msi.websocketdemo.websocket.common.ICallback;

/**
 * Created by zly on 2017/7/23.
 */

public class CallbackDataWrapper<T> {

    private ICallback<T> callback;
    private Object data;

    public CallbackDataWrapper(ICallback<T> callback, Object data) {
        this.callback = callback;
        this.data = data;
    }

    public ICallback<T> getCallback() {
        return callback;
    }


    public void setCallback(ICallback<T> callback) {
        this.callback = callback;
    }


    public Object getData() {
        return data;
    }


    public void setData(Object data) {
        this.data = data;
    }
}
