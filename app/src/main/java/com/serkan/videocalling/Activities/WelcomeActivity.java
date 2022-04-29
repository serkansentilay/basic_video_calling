package com.serkan.videocalling.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.serkan.videocalling.R;

public class WelcomeActivity extends AppCompatActivity {
    FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() !=null){
            gotonextActivity();
        }

        findViewById(R.id.getstarted).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotonextActivity();
            }
        });

    }

    void gotonextActivity(){
        startActivity(new Intent(WelcomeActivity.this,LoginActivity.class));
        finish();
    }
}