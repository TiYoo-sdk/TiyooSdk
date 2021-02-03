package com.tiyaogame.sdk.pay;

public interface PurchaseInAppListener {
    void purchaseInAppSuccess(String data,String json);
    void purchaseInAppError(int code ,String message);
}
