package com.tiyaogame.sdk.api.setting;

import android.app.Activity;

import com.tiyaogame.sdk.pay.PurchaseInAppListener;
import com.tiyaogame.sdk.pay.TiYaoPayMgr;
import static com.tiyaogame.sdk.login.TiYaoLoginMgr.setDefaultWebClientId;
public class TiYaoSdkSetting {
    private static TiYaoSdkSetting tiYaoSdkSetting = null;
    //单例
    public static TiYaoSdkSetting getInstance(){

        if (tiYaoSdkSetting == null){
            synchronized (TiYaoSdkSetting.class){
                if (tiYaoSdkSetting == null){
                    tiYaoSdkSetting = new TiYaoSdkSetting();
                }
            }
        }
        return tiYaoSdkSetting;
    }

    /**
     * 初始化Sdk
     */
    public void InitSdk(Activity activity,String default_web_client_id,PurchaseInAppListener listener)
    {
        //登录
        setDefaultWebClientId(activity,default_web_client_id);
        //支付
        TiYaoPayMgr.getInstance().InitGooglePay(activity,listener);
    }
}
