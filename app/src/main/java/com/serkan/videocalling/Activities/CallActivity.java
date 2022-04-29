package com.serkan.videocalling.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.serkan.videocalling.Models.PeerInterface;
import com.serkan.videocalling.Models.User;
import com.serkan.videocalling.R;
import com.serkan.videocalling.databinding.ActivityCallBinding;

import java.util.UUID;

public class CallActivity extends AppCompatActivity {

    private static boolean isPeerConnected = false;
    ActivityCallBinding binding;
    String uuid = "";
    FirebaseAuth auth;
    String username = "";
    String friendsUsername = "";
  //  boolean isPeerConnected = false;
    DatabaseReference firebaseRefe;
    boolean isAudio = true;
    boolean isVideo = true;
    String createdBy;
    boolean pageExit=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firebaseRefe = FirebaseDatabase.getInstance().getReference().child("users");
        username = getIntent().getStringExtra("username");
        String incoming = getIntent().getStringExtra("incoming");
        createdBy = getIntent().getStringExtra("createdBy");

        /*
        friendsUsername = "";
        if(incoming.equalsIgnoreCase(friendsUsername))
            friendsUsername = incoming;
        */
        friendsUsername = incoming;
        setupWebView();
        binding.micBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAudio = !isAudio;
                callJavascFunction("javascript:toggleAudio(\""+isAudio+"\")");
                if(isAudio){
                    binding.micBtn.setImageResource(R.drawable.btn_unmute_normal);
                }else{
                    binding.micBtn.setImageResource(R.drawable.btn_mute_normal);
                }
            }
        });

        binding.videoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isVideo = !isVideo;
                callJavascFunction("javascript:toggleVideo(\""+isVideo+"\")");
                if(isVideo){
                    binding.videoBtn.setImageResource(R.drawable.btn_video_normal);
                }else{
                    binding.videoBtn.setImageResource(R.drawable.btn_video_muted);
                }
            }
        });

        binding.endCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }



    @SuppressLint("SetJavaScriptEnabled")
    void setupWebView(){
        binding.webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }
        });

        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        binding.webView.addJavascriptInterface(new PeerInterface(this),"Android");
        loadVideoCall();
    }

    public void loadVideoCall(){
        String filePath = "file:android_asset/call.html";
        binding.webView.loadUrl(filePath);
        binding.webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                initilazePeer();
            }
        });
    }

    void callJavascFunction(String function){
            binding.webView.post(new Runnable() {
                @Override
                public void run() {
                        binding.webView.evaluateJavascript(function,null);
                }
            });
    }

    public void initilazePeer(){
            uuid = UUID.randomUUID().toString();
            callJavascFunction("javascript:unit(\""+uuid+"\"");
            if(createdBy.equalsIgnoreCase(username)){
                if(pageExit)
                    return;

                firebaseRefe.child(username).child("connId").setValue(uuid);
                firebaseRefe.child(username).child("isAvailable").setValue(true);
                binding.loadinggroup.setVisibility(View.GONE);
                binding.controls.setVisibility(View.VISIBLE);

                FirebaseDatabase.getInstance().getReference().child("profiles").child(friendsUsername)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User user = snapshot.getValue(User.class);
                                Glide.with(CallActivity.this).load(user.getProfile())
                                        .into(binding.profile);
                                binding.name.setText(user.getName());
                                binding.city.setText(user.getCity());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }else{
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        friendsUsername = createdBy;
                        FirebaseDatabase.getInstance().getReference().child("profiles").child(friendsUsername)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        User user = snapshot.getValue(User.class);
                                        Glide.with(CallActivity.this).load(user.getProfile())
                                                .into(binding.profile);
                                        binding.name.setText(user.getName());
                                        binding.city.setText(user.getCity());
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                        FirebaseDatabase.getInstance().getReference().child("users")
                                .child(friendsUsername)
                                .child("connId")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.getValue()!=null){
                                            sendCallRequest();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }
                },3000);
            }
    }

   public static void onPeerConnected(){
        isPeerConnected=true;
    }

    void sendCallRequest(){
        if(!isPeerConnected){
            Toast.makeText(this,"You are not connected. Please check your Internet.",Toast.LENGTH_SHORT).show();
        return;}
        listenConnId();

    }
    void listenConnId(){
            firebaseRefe.child(friendsUsername).child("connId").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.getValue()==null)
                        return;
                    binding.loadinggroup.setVisibility(View.GONE);
                    binding.controls.setVisibility(View.VISIBLE);
                    String connId = snapshot.getValue(String.class);
                    callJavascFunction("javascript:startCall(\""+connId+"\")");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        pageExit=true;
        firebaseRefe.child(createdBy).setValue(null);
        finish();
    }
}