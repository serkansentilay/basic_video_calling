package com.serkan.videocalling.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.serkan.videocalling.Models.User;
import com.serkan.videocalling.R;

public class LoginActivity extends AppCompatActivity {

    GoogleSignInClient mGoogleSignClient;
    FirebaseAuth mauth;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mauth=FirebaseAuth.getInstance();
        if(mauth.getCurrentUser() !=null){
            gotonextActivity();
        }
        database = FirebaseDatabase.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignClient = GoogleSignIn.getClient(this,gso);





        findViewById(R.id.loginBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent intent = mGoogleSignClient.getSignInIntent();
               startActivityForResult(intent,1);

            }
        });


    }
    void gotonextActivity(){
        startActivity(new Intent(LoginActivity.this,MainActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            GoogleSignInAccount account = task.getResult();
            authwithGoogle(account.getIdToken());

        }
    }

    void authwithGoogle(String idtoken){
        AuthCredential credential = GoogleAuthProvider.getCredential(idtoken,null);
        mauth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user = mauth.getCurrentUser();
                    User firebaseUser = new User(
                            user.getUid(),user.getDisplayName(),user.getPhotoUrl().toString(),"Unknow",500);
                    database.getReference().child("profiles").child(user.getUid()).setValue(firebaseUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                startActivity(new Intent(LoginActivity.this,MainActivity.class));
                                finishAffinity();
                            }
                            else{
                                Toast.makeText(LoginActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                }
                else{
                    Log.e("err",task.getException().getLocalizedMessage());
                }
            }
        });
    }
}