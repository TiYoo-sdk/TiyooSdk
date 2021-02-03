package com.tiyaogame.sdk.login;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.tiyaogame.sdk.api.data.UserPrefs;

public class TiYaoLoginMgr {
    private static String default_web_client_id;
    private static SdkLoginListener loginListener;
    private static SdkLogoutListener logoutListener;

    public static Context context;

    //获取当前用户类
    public static boolean hasFirebaseUser(){
        return FirebaseAuth.getInstance().getCurrentUser()!=null;
    }
    public static String getFirebaseUserName(){
        return FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    }
    public static String getFirebaseUserEmail(){
        return FirebaseAuth.getInstance().getCurrentUser().getEmail();
    }
    public static String getFirebaseUserId(){
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    //设置回调
    public static void setLogoutListener(SdkLogoutListener outListener) {
        logoutListener = outListener;
    }
    public static void setLoginListener(SdkLoginListener inListener) {
        loginListener = inListener;
    }
    public static SdkLogoutListener getLogoutListener() {
        return logoutListener;
    }
    public static SdkLoginListener getLoginListener() {
        return loginListener;
    }

    //设置default_web_client_id
    public static void setDefaultWebClientId(Context context,String default_web_client_id) {
        TiYaoLoginMgr.default_web_client_id = default_web_client_id;
    }
    public static String getDefaultWebClientId() {
        return TiYaoLoginMgr.default_web_client_id;
    }

    //登录界面入口
    public static void Login(Context context,SdkLoginListener inListener) {
        setLoginListener(inListener);
        TiYaoLoginMgr.context=context;
        if (hasFirebaseUser()) {
            AutoLogin();
        }else{
            Intent intent=new Intent(context, SdkLogin.class);
            context.startActivity(intent);
        }
    }

    //自动登录
    private static void AutoLogin()
    {
        FirebaseAuth.getInstance().getCurrentUser().getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "AutoLogin successful", Toast.LENGTH_SHORT).show();
                    LoginSuccess(UserPrefs.getString(context,UserPrefs.loginType,"1"));
                } else {
                    getLoginListener().onError(-6,"AutoLogin Error:" + task.getException());
                }
            }
        });
    }

    //登出，无论之前是任何登录方式
    public static void Logout(Context context,SdkLogoutListener outListener){
        setLogoutListener(outListener);
        TiYaoLoginMgr.context=context;
        Intent intent=new Intent(context, SdkLogin.class);
        context.startActivity(intent);
    }

    //登录成功
    public static void LoginSuccess(String loginType){
        UserPrefs.setString(context,UserPrefs.user_id,getFirebaseUserId());
        UserPrefs.setString(context,UserPrefs.userEmail,getFirebaseUserEmail());
        UserPrefs.setString(context,UserPrefs.loginType,loginType);
        getLoginListener().onSuccess(getFirebaseUserName(),getFirebaseUserId());
    }
}
