package com.tiyoo.sdk.pay;

import android.app.Activity;
import android.util.Log;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;

public class BillingClientMgr {

    private static final String TAG = "BillingClientMgr";
    private static BillingClientMgr billingClientMgr = null;
    private BillingClient billingClient;
    private onPurchaseInAppListener purchaseInAppListener;

    //单例
    public static BillingClientMgr getInstance(){

        if (billingClientMgr == null){
            synchronized (BillingClientMgr.class){
                if (billingClientMgr == null){
                    billingClientMgr = new BillingClientMgr();
                }
            }
        }
        return billingClientMgr;
    }

    public interface onBillingConnectionListener{
        void billingConnectionSuccess();
        void billingConnectionError();
    }
    public interface onQuerySkuDetailsListener{
        void querySkuDetailsSuccess(Activity activity,List<SkuDetails> list);
        void querySkuDetailsError(int code ,String message);
    }
    public interface onPurchaseInAppListener{
        void purchaseSuccess(List<Purchase> list);
        void purchaseError(int code ,String message);
    }
    public interface onQueryPurchasesListener{
        void queryPurchasesSuccess(List<Purchase> list);
        void queryPurchasesError(int code ,String message);
    }
    public interface onConsumeListener{
        void consumeSuccess(String purchaseToken,boolean isRealTime);
        void consumeError(int code ,String message,boolean isRealTime);
    }

    /**
     * 初始化支付类
     */
    public void InitBillingClient(Activity activity,onBillingConnectionListener listener) {
        billingClient = BillingClient.newBuilder(activity)
                .setListener(new PurchasesUpdatedListener() {
                    @Override
                    public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
                        int code=billingResult.getResponseCode();
                        if(code==BillingClient.BillingResponseCode.OK&&purchases!=null){
                            purchaseInAppListener.purchaseSuccess(purchases);
                        }
                        else if (code == BillingClient.BillingResponseCode.USER_CANCELED) {
                            //Toast.makeText(activity, "Handle an error caused by a user cancelling the purchase flow", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Handle an error caused by a user cancelling the purchase flow");
                        }
                        else {
                            String message = "";
                            if (code == 4){
                                message  = "Unable to purchase product";
                            }
                            purchaseInAppListener.purchaseError(code,message);
                        }
                    }
                })
                .enablePendingPurchases()
                .build();

        bCConnectGooglePlay(listener);
    }

    /**
     * BillingClient连接GooglePlay
     */
    private void bCConnectGooglePlay(onBillingConnectionListener listener) {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    listener.billingConnectionSuccess();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                //bCConnectGooglePlay(listener);
                listener.billingConnectionError();
            }
        });
    }

    /**
     * 查询内购商品信息
     */
    public void querySkuDetailsAsync(final Activity activity,final ArrayList<String>skuList,final onQuerySkuDetailsListener listener){
        Runnable runnable= new Runnable() {
            @Override
            public void run() {
                SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
                billingClient.querySkuDetailsAsync(params.build(),
                        new SkuDetailsResponseListener() {
                            @Override
                            public void onSkuDetailsResponse(BillingResult billingResult,
                                                             List<SkuDetails> skuDetailsList) {
                                int code = billingResult.getResponseCode();
                                if(code==BillingClient.BillingResponseCode.OK&&skuDetailsList!=null){
                                    listener.querySkuDetailsSuccess(activity,skuDetailsList);
                                }else{
                                    listener.querySkuDetailsError(code,billingResult.getDebugMessage());
                                }
                            }
                        });
            }
        };
        runRequest(activity,runnable);
    }
    private void runRequest(final Activity activity,final Runnable runnable)
    {
        if(billingClient!=null){
            if(billingClient.isReady())
            {
                runnable.run();
            }
            else {
                Log.e(TAG, "BillingClient not Ready");
            }
        }else{
            Log.e(TAG, "BillingClient == null");
        }
    }

    /**
     * 支付商品
     */
    public void purchaseInApp(Activity activity,SkuDetails skuDetails,onPurchaseInAppListener listener){
        purchaseInAppListener=listener;
        if(billingClient.isReady()){
            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails)
                    .build();
            int responseCode = billingClient.launchBillingFlow(activity, billingFlowParams).getResponseCode();
            if(responseCode==BillingClient.BillingResponseCode.OK){
                Log.d(TAG, "launchBillingFlow success");
            }else{
                Log.e(TAG, "launchBillingFlow failed");
            }
        }else{
            listener.purchaseError(-1,"billingClient not connection");
        }
    }

    /**
     * 查询已购买的产品，通常用于补单或购买前校验
     */
    public void queryHasPurchased(onQueryPurchasesListener listener) {
        if(billingClient!=null){
            if (billingClient.isReady()) {
                Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
                if (purchasesResult != null) {
                    int code=purchasesResult.getResponseCode();
                    if (code == BillingClient.BillingResponseCode.OK) {
                        List<Purchase> purchaseList = purchasesResult.getPurchasesList();
                        listener.queryPurchasesSuccess(purchaseList);
                    } else {
                        listener.queryPurchasesError(code,purchasesResult.getBillingResult().getDebugMessage());
                    }
                } else {
                    Log.d(TAG, "purchasesResult==null");
                }
            }
            else{
                Log.d(TAG, "BillingClient not Ready");
            }
        }
        else{
            Log.e(TAG, "BillingClient == null");
        }
    }

    /**
     * 消耗已购买的产品
     */
    public void consumePurchase(String purchaseToken, onConsumeListener listener,boolean isRealTime) {
        if (billingClient != null) {
            ConsumeParams consumeParams =
                    ConsumeParams.newBuilder()
                            .setPurchaseToken(purchaseToken)
                            .build();

            billingClient.consumeAsync(consumeParams, new ConsumeResponseListener() {
                @Override
                public void onConsumeResponse(BillingResult billingResult, String pToken) {
                    int code = billingResult.getResponseCode();
                    if (code == BillingClient.BillingResponseCode.OK) {
                        // Handle the success of the consume operation.
                        listener.consumeSuccess(pToken,isRealTime);
                    } else {
                        listener.consumeError(code, billingResult.getDebugMessage(),isRealTime);
                    }
                }
            });
        }
        else{
            Log.e(TAG, "billingClient == null");
        }
    }
}
