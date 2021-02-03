package com.tiyoo.sdk.api.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

public class UserPrefs {
    private static String fileName = "data";
    private static Map baseMap;

    public final static String loginType="loginType";//1.google登录    2.facebook登录

    public final static String PayData = "PayData";
    public final static String user_id = "user_id";
    public final static String userEmail = "user_email";

    public final static String productID = "product_id";
    public final static String productPrice = "product_price";
    public final static String gameOrderId = "gameOrder_id";
    public final static String purchaseOrderId = "purchaseOrderId";
    public final static String purchaseToken = "purchase_token";
    public final static String purchaseSignature = "purchase_signature";
    public final static String currencyCode = "currency_code";

    private static SharedPreferences sharedPreferences;
    private static SharedPreferences getInstance(Context context){
        if (sharedPreferences == null){
            synchronized (UserPrefs.class){
                if (sharedPreferences == null){
                    sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
                }
            }
        }
        return sharedPreferences;
    }
    private UserPrefs(){ }

    public static void setString(Context context,String key, String value) {
        UserPrefs.getInstance(context).edit().putString(key, value).apply();
    }

    public static String getString(Context context,String key, String defValue) {
        return UserPrefs.getInstance(context).getString(key, defValue);
    }

    public static Map getBaseMap(boolean isClear)
    {
        if(baseMap==null){
            baseMap=new HashMap<String,Object>();
        }else{
            if(isClear){
                baseMap.clear();
            }
        }
        return baseMap;
    }
}
