package com.bophia.erik.ykxrider.websocket.common;

import com.bophia.erik.ykxrider.websocket.request.Action;
import com.bophia.erik.ykxrider.websocket.request.Request;

/**
 *
 * onSuccess与普通的成功回调一样,onError和onTimeout回调中有Request与Action是为了方便后续再次请求操作.
 *
 * Created by zly on 2017/7/23.
 */

public interface IWsCallback<T> {
    void onSuccess(T t);
    void onError(String msg, Request request, Action action);
    void onTimeout(Request request, Action action);
}
