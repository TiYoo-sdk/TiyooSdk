package com.tiyoo.sdk.login;

public interface SdkLoginListener {
    void onSuccess(String useName,String useId);
    void onError(int code ,String message);
}
