package com.tiyoo.sdk.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.tiyoo.sdk.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;


public class FacebookLogin extends AppCompatActivity {

    private static final String TAG = "FBLogin";
    private CallbackManager mCallbackManager;
    private LoginButton loginButton;
    private  FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook_login);

        mCallbackManager = CallbackManager.Factory.create();
        loginButton =  findViewById(R.id.login_button);
        loginButton.setPermissions("email", "public_profile");

        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                // App code
                Log.d(TAG, "facebook:onCancel");
                TiYaoLoginMgr.getLoginListener().onError(-2,"facebook:onCancel");
                //Toast.makeText(FacebookLogin.this, "Facebook Cancel", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                //Log.d(TAG, "facebook:onError: " + exception.getMessage());
                TiYaoLoginMgr.getLoginListener().onError(-1,"facebook:onError: " + exception.getMessage());
                Toast.makeText(FacebookLogin.this, "Facebook Error. Code: -1, Message: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        loginButton.performClick();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        //Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            LoginSuccess();
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(FacebookLogin.this, "signInWithCredential failed. Code: -3, Message: "+ task.getException(), Toast.LENGTH_SHORT).show();
                            TiYaoLoginMgr.getLoginListener().onError(-3,"signInWithCredential:failure."+ task.getException());
                        }
                        finish();
                    }
                });
    }

    private void LoginSuccess(){
        //登录sdk
        Toast.makeText(FacebookLogin.this, "Login successful", Toast.LENGTH_SHORT).show();
        TiYaoLoginMgr.LoginSuccess("2");
    }
}