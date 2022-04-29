package com.serkan.videocalling.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.serkan.videocalling.Models.User;
import com.serkan.videocalling.R;
import com.serkan.videocalling.databinding.ActivityMainBinding;

import io.github.rupinderjeet.kprogresshud.KProgressHUD;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    long havecoins=0;
    String[] permissions = new String[] {Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
    private int requestCode =1;
    User user;
    KProgressHUD progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        progress = KProgressHUD.create(this);
        progress.show();

        database.getReference().child("profiles").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progress.dismiss();
                user =snapshot.getValue(User.class);
                havecoins = user.getCoins();
                binding.havecoins.setText("You have: "+havecoins);


                Glide.with(MainActivity.this).load(currentUser.getPhotoUrl()).into(binding.profileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        binding.findbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPermissionsGranted()){
                    if(havecoins >5){
                        havecoins -=5;
                        database.getReference().child("profiles").child(currentUser.getUid())
                                .child("coins").setValue(havecoins);
                        Intent intent = new Intent(MainActivity.this,ConnectingActivity.class);
                        intent.putExtra("profile",user.getProfile());
                       startActivity(intent);
                    }else{
                        Toast.makeText(MainActivity.this, "Insufficient Coins", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    askPermissions();
                }
            }
        });


        binding.rewardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,RewardedActivity.class));
            }
        });

    }

    void askPermissions(){
        ActivityCompat.requestPermissions(this,permissions,requestCode);
    }


    private boolean isPermissionsGranted(){
        for(String permissions:permissions){
            if(ActivityCompat.checkSelfPermission(this,permissions)!= PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

}