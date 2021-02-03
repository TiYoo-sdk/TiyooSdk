package com.tiyoo.sdk.pay;

import android.app.Activity;

import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import com.google.gson.Gson;
import com.tiyoo.sdk.api.data.PayData;
import com.tiyoo.sdk.api.data.UserPrefs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TiYaoPayMgr implements BillingClientMgr.onQuerySkuDetailsListener, BillingClientMgr.onPurchaseInAppListener,
        BillingClientMgr.onQueryPurchasesListener, BillingClientMgr.onConsumeListener,
        BillingClientMgr.onBillingConnectionListener{
    private static TiYaoPayMgr tiYaoPayMgr = null;
    private static final String TAG = "TiYaoPayMgr";
    private static PurchaseInAppListener realTimePurchaseInAppListener;
    private static PurchaseInAppListener queryPurchaseInAppListener;

    private Activity curActivity;
    //单例
    public static TiYaoPayMgr getInstance(){

        if (tiYaoPayMgr == null){
            synchronized (TiYaoPayMgr.class){
                if (tiYaoPayMgr == null){
                    tiYaoPayMgr = new TiYaoPayMgr();
                }
            }
        }
        return tiYaoPayMgr;
    }

    /**
     * 初始化谷歌原生支付
     */
    public void InitGooglePay(Activity activity,PurchaseInAppListener Listener){
        curActivity=activity;
        queryPurchaseInAppListener=Listener;
        BillingClientMgr.getInstance().InitBillingClient(activity,TiYaoPayMgr.this);
    }

    /**
     * 谷歌原生支付
     */
    public void googlePay(Activity activity, PayData payData,PurchaseInAppListener Listener){
        if(payData!=null){
            Map<String,Object> map=UserPrefs.getBaseMap(true);
            map.put(UserPrefs.user_id,UserPrefs.getString(curActivity,UserPrefs.user_id,""));
            map.put(UserPrefs.userEmail,UserPrefs.getString(curActivity,UserPrefs.userEmail,""));
            map.put(UserPrefs.productID,payData.productID);
            map.put(UserPrefs.productPrice,payData.productPrice);
            map.put(UserPrefs.currencyCode,payData.currencyCode);
            map.put(UserPrefs.gameOrderId,payData.gameOrderId);

            realTimePurchaseInAppListener=Listener;
            ArrayList<String> list = new ArrayList<>();
            list.add(payData.productID);
            BillingClientMgr.getInstance().querySkuDetailsAsync(activity,list,TiYaoPayMgr.this);
        }
        else{
            realTimePurchaseInAppListener.purchaseInAppError(-20211,"realTimePayData == null");
        }
    }

    /**
     * 查询是否有已支付未消耗的商品
     */
    public void queryHasPurchased(Activity activity,PurchaseInAppListener Listener){
        queryPurchaseInAppListener=Listener;
        BillingClientMgr.getInstance().queryHasPurchased(TiYaoPayMgr.this);
    }

    /**
     * ///////////////////////////////////////////////////////////////////////////////////////////回调//////////////////////////////////////////////////////
     */

    /**
     * 查询商品SkuDetails回调
     */
    @Override
    public void querySkuDetailsSuccess(Activity activity,List<SkuDetails> list) {
        if(!list.isEmpty()){
            BillingClientMgr.getInstance().purchaseInApp(activity,list.get(0),TiYaoPayMgr.this);
        }else{
            realTimePurchaseInAppListener.purchaseInAppError(-20212,"querySkuDetailsSuccess: The product is empty");
        }
    }
    @Override
    public void querySkuDetailsError(int code, String message) {
        realTimePurchaseInAppListener.purchaseInAppError(-20213,"querySkuDetailsError："+code+"，"+message);
    }

    /**
     * 支付商品回调
     */
    @Override
    public void purchaseSuccess(List<Purchase> list) {
        for (int i=0; i<list.size();i++){
            if(list.get(i).getPurchaseState()== Purchase.PurchaseState.PURCHASED){
                //保存订单
                Map<String,Object> map=UserPrefs.getBaseMap(false);
                map.put(UserPrefs.purchaseOrderId,list.get(i).getOrderId());
                map.put(UserPrefs.purchaseToken,list.get(i).getPurchaseToken());
                map.put(UserPrefs.purchaseSignature,list.get(i).getSignature());
                UserPrefs.setString(curActivity,UserPrefs.PayData,new Gson().toJson(map));
                BillingClientMgr.getInstance().consumePurchase(list.get(i).getPurchaseToken(),this,true);
            }
            else {
                //PurchaseState：1:已购买  2:待处理  3:未定义状态
                realTimePurchaseInAppListener.purchaseInAppError(-20217,"realTimePurchaseState："+list.get(i).getPurchaseState());
            }
        }
    }
    @Override
    public void purchaseError(int code, String message) {
        realTimePurchaseInAppListener.purchaseInAppError(-20214,"realTimePurchaseError："+code+"，"+message);
    }

    /**
     * 查询已支付未消耗商品回调
     */
    @Override
    public void queryPurchasesSuccess(List<Purchase> list) {
        for (int i=0; i<list.size();i++){
            if(list.get(i).getPurchaseState()== Purchase.PurchaseState.PURCHASED){
                BillingClientMgr.getInstance().consumePurchase(list.get(i).getPurchaseToken(),this,false);
            }
            else {
                queryPurchaseInAppListener.purchaseInAppError(-20217,"queryPurchaseState："+list.get(i).getPurchaseState());
            }
        }
    }
    @Override
    public void queryPurchasesError(int code, String message) {
        queryPurchaseInAppListener.purchaseInAppError(-20215,"queryPurchasesError："+code+"，"+message);
    }

    /**
     * 消耗商品回调
     */
    @Override
    public void consumeSuccess(String purchaseToken,boolean isRealTime) {
        //Log.d(TAG, "consumeSuccess: "+purchaseToken);
        String json=UserPrefs.getString(curActivity,UserPrefs.PayData,"");
        if(purchaseToken!=null&&!purchaseToken.isEmpty()){
            if(isRealTime){
                realTimePurchaseInAppListener.purchaseInAppSuccess(purchaseToken,json);
            }else {
                queryPurchaseInAppListener.purchaseInAppSuccess(purchaseToken,json);
            }
        }
        else {
            if(isRealTime){
                realTimePurchaseInAppListener.purchaseInAppError(-20219,"realTime:purchaseToken does not exist");
            }else {
                queryPurchaseInAppListener.purchaseInAppError(-20219,"query:purchaseToken does not exist");
            }
        }
    }
    @Override
    public void consumeError(int code, String message,boolean isRealTime) {
        if(isRealTime){
            realTimePurchaseInAppListener.purchaseInAppError(-20216,"realTimeConsumeError: "+code+"，"+message);
        }else {
            queryPurchaseInAppListener.purchaseInAppError(-20216,"queryConsumeError: "+code+"，"+message);
        }
    }

    /**
     * BillingClient连接GooglePlay回调
     */
    @Override
    public void billingConnectionSuccess() {
        //Log.d(TAG, "BillingClient connection success");
        BillingClientMgr.getInstance().queryHasPurchased(TiYaoPayMgr.this);
    }
    @Override
    public void billingConnectionError() {
        queryPurchaseInAppListener.purchaseInAppError(-20218,"BillingClient connection error,Reconnecting");
    }
}
