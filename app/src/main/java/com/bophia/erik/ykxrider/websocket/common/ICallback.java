package com.bophia.erik.ykxrider.websocket.common;

/**
 * ui层回调
 */

public interface ICallback<T> {

    void onSuccess(T t);

    void onFail(String msg);

}
