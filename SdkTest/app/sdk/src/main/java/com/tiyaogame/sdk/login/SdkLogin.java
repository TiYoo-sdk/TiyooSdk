package com.tiyaogame.sdk.login;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.tiyaogame.sdk.R;

import static com.tiyaogame.sdk.login.TiYaoLoginMgr.getLogoutListener;
import static com.tiyaogame.sdk.login.TiYaoLoginMgr.hasFirebaseUser;

public class SdkLogin extends AppCompatActivity implements View.OnClickListener{

    private TextView googleBtn;
    private TextView facebookBtn;
    private TextView LogoutBtn;
    private TextView LoginTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sdk_login);

        googleBtn = findViewById(R.id.lg_google);
        facebookBtn = findViewById(R.id.lg_facebook);
        LogoutBtn = findViewById(R.id.logout_button);
        LoginTitle = findViewById(R.id.login_title);

        googleBtn.setOnClickListener(this);
        facebookBtn.setOnClickListener(this);
        LogoutBtn.setOnClickListener(this);

        if (hasFirebaseUser()) {
            googleBtn.setVisibility(View.GONE);
            facebookBtn.setVisibility(View.GONE);
            LogoutBtn.setVisibility(View.VISIBLE);
            LoginTitle.setText("Sign Out");
        }
        else {
            googleBtn.setVisibility(View.VISIBLE);
            facebookBtn.setVisibility(View.VISIBLE);
            LogoutBtn.setVisibility(View.GONE);
            LoginTitle.setText("Sign In");
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId() == R.id.lg_google) {
            intent = new Intent(this,GoogleLogin.class);
            startActivity(intent);
        }else if (view.getId() == R.id.lg_facebook){
            intent =   new Intent(this,FacebookLogin.class);
            startActivity(intent);
        }
        else if (view.getId() ==R.id.logout_button){
            FirebaseAuth.getInstance().signOut();
            LoginManager.getInstance().logOut();
            getLogoutListener().onFinish(0,"Logout Finish!");
        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}