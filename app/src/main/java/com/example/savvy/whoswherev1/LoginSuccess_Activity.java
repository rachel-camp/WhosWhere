package com.example.savvy.whoswherev1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class LoginSuccess_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_success_);
        pullUsername();
    }

    // Pulls the username input from the previous activity and displays it upon successful login
    protected void pullUsername(){

        TextView loginPrompt = findViewById(R.id.tv_loginSuccess);
        Intent intent = getIntent();
        Bundle b = intent.getExtras();


        if (b!=null){
            String j = (String) b.get("username");
            loginPrompt.setText(j + " you have logged in successfully");
        }

    }
}
