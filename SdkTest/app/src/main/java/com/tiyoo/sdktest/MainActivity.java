package com.tiyoo.sdktest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.tenjin.android.TenjinSDK;
import com.tiyoo.sdktest.R;
import com.tiyoo.sdk.api.data.PayData;
import com.tiyoo.sdk.api.data.UserPrefs;
import com.tiyoo.sdk.api.setting.TiYaoSdkSetting;
import com.tiyoo.sdk.login.SdkLoginListener;
import com.tiyoo.sdk.login.SdkLogoutListener;
import com.tiyoo.sdk.pay.PurchaseInAppListener;
import com.tiyoo.sdk.pay.TiYaoPayMgr;

import java.util.Map;

import static com.tiyoo.sdk.login.TiYaoLoginMgr.*;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Button LogButton;
    private Button PayButton;
    private TextView userNameText;
    private TextView userIdText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LogButton = findViewById(R.id.Log_button);
        PayButton = findViewById(R.id.Pay_Button);
        userNameText = findViewById(R.id.userName_text);
        userIdText = findViewById(R.id.userId_Text);

        //初始化
        TiYaoSdkSetting.getInstance().InitSdk(MainActivity.this,getString(R.string.default_web_client_id), new PurchaseInAppListener() {
            @Override
            public void purchaseInAppSuccess(String purchaseToken,String json) {
                Log.d(TAG, "purchaseInAppSuccess："+"支付商品token："+purchaseToken);

                //统计数据
                reportEvent(json);
                //在此通知游戏服务器发放商品,上传支付token
            }

            @Override
            public void purchaseInAppError(int code, String message) {
                Log.e(TAG, "purchaseInAppError：" + code + "，" + message);
            }
        });

        //登陆
        //SdkLog();
        SdkLogin();

        PayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //支付
                PayData payData = new PayData();
                payData.productID = "com.tiyaogame.sdktest.gp.12";
                payData.productPrice = 15.00;
                payData.currencyCode = "HKD";

                payData.gameOrderId = "";
                TiYaoPayMgr.getInstance().googlePay(MainActivity.this, payData, new PurchaseInAppListener() {
                    @Override
                    public void purchaseInAppSuccess(String purchaseToken,String json) {
                        Log.d(TAG, "purchaseInAppSuccess："+"支付商品token："+purchaseToken);
                        //统计数据
                        reportEvent(json);
                        //在此通知游戏服务器发放商品,上传支付token
                    }

                    @Override
                    public void purchaseInAppError(int code, String message) {
                        Log.e(TAG, "purchaseInAppError："+code+"，"+message);
                    }
                });
            }
        });
    }

    //登录
    private void SdkLog() {
        if (hasFirebaseUser()) {
            SdkLogout();
        } else {
            SdkLogin();
        }
    }

    private void SdkLogin() {
        LogButton.setText("Login");
        userNameText.setText("null");
        userIdText.setText("null");

        LogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //登录
                Login(MainActivity.this,new SdkLoginListener(){

                    @Override
                    public void onSuccess(String useId, String useName) {
                        SdkLog();
                        Log.d(TAG, "LoginSuccess："+"useId："+useId+",useName："+useName);
                    }

                    @Override
                    public void onError(int code, String message) {
                        Log.e(TAG, "LoginError：" + code + "，" + message);
                    }
                });
            }
        });
    }

    private void SdkLogout() {
        LogButton.setText("Logout");
        userNameText.setText(getFirebaseUserName());
        userIdText.setText(getFirebaseUserId());

        LogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //退出
                Logout(MainActivity.this,new SdkLogoutListener(){
                    @Override
                    public void onFinish(int code, String message) {
                        SdkLog();
                    }
                });
            }
        });
    }

    /**
     * 统计数据
     */
    private void reportEvent(String json)
    {
        Map<String,Object> map1=new Gson().fromJson(json,Map.class);
        String productPrice =  map1.get(UserPrefs.productPrice).toString();
        String productID =  map1.get(UserPrefs.productID).toString();
        String gameOrderId =  map1.get(UserPrefs.purchaseOrderId).toString();
        String purchaseSignature =  map1.get(UserPrefs.purchaseSignature).toString();
        String currencyCode =  map1.get(UserPrefs.currencyCode).toString();
        TenjinSDK tenjinSDK = TenjinSDK.getInstance(MainActivity.this,getString(R.string.tenjin_apikey));
        tenjinSDK.transaction(productID, currencyCode, 1,  Double.valueOf(productPrice), gameOrderId, purchaseSignature);
        tenjinSDK.eventWithName(json);
    }

    @Override protected void onResume()
    {
        super.onResume();
        TenjinSDK tenjinSDK = TenjinSDK.getInstance(MainActivity.this, getString(R.string.tenjin_apikey));
        tenjinSDK.connect();
    }
}