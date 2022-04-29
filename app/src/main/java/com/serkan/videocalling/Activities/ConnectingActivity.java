package com.serkan.videocalling.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.serkan.videocalling.R;
import com.serkan.videocalling.databinding.ActivityConnectingBinding;

import java.util.HashMap;

public class ConnectingActivity extends AppCompatActivity {

    ActivityConnectingBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    boolean isOkay=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConnectingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        String profile = getIntent().getStringExtra("profile");
        Glide.with(this).load(profile).into(binding.profile);

        String username = auth.getUid();
        database.getReference().child("users").orderByChild("status").equalTo(0).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getChildrenCount()>0){
                        isOkay=true;
                    for(DataSnapshot childSnap : snapshot.getChildren()){
                        database.getReference().child("users").child(childSnap.getKey()).child("incoming").setValue(username);
                        database.getReference().child("users").child(childSnap.getKey()).child("status").setValue(1);

                        Intent intent = new Intent(ConnectingActivity.this,CallActivity.class);
                        intent.putExtra("username",username);
                        intent.putExtra("incoming",childSnap.child("incoming").getValue(String.class));
                        intent.putExtra("createdBy",childSnap.child("createdBy").getValue(String.class));
                        intent.putExtra("isAvailable",childSnap.child("isAvailable").getValue(Boolean.class));


                        startActivity(intent);
                        finish();

                    }


                }else{
                    HashMap<String,Object> room = new HashMap<>();
                    room.put("incoming",username);
                    room.put("createdBy",username);
                    room.put("isAvailable",true);
                    room.put("status",0);

                    database.getReference().child("users").child(username).setValue(room).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            database.getReference().child("users").child(username).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {



                                    if(snapshot.child("status").exists()){
                                        if(snapshot.child("status").getValue(Integer.class)==1){

                                            if(isOkay)
                                                return;

                                            isOkay=true;
                                            Intent intent = new Intent(ConnectingActivity.this,CallActivity.class);
                                            intent.putExtra("username",username);
                                            intent.putExtra("incoming",snapshot.child("incoming").getValue(String.class));
                                            intent.putExtra("createdBy",snapshot.child("createdBy").getValue(String.class));
                                            intent.putExtra("isAvailable",snapshot.child("isAvailable").getValue(Boolean.class));


                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}