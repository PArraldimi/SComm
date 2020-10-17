package com.exo.scomm.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.exo.scomm.R;
import com.exo.scomm.data.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    private final int SPLASH_DELAY = 2500;
    private ImageView imageView;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();

        getWindow().setBackgroundDrawable(null);
        initializeView();
        animateLogo();
        goToMainActivity();
    }

    private void initializeView() {
        imageView = findViewById(R.id.imageView2);
    }

    private void animateLogo() {

        Animation fadingInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadingInAnimation.setDuration(SPLASH_DELAY);

        imageView.startAnimation(fadingInAnimation);
    }

    private void goToMainActivity() {
        //            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        new Handler().postDelayed(this::getUser, SPLASH_DELAY);
    }
    private void getUser(){

        if (mAuth.getCurrentUser() != null) {
            FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChild("username")) {
                        User mUser = snapshot.getValue(User.class);
                        if (mUser != null) {
                            if (!mUser.getUsername().equals("")) {
                                startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                            } else {
                                startActivity(new Intent(SplashActivity.this, SetupActivity.class));
                            }
                            finish();
                        } else {

                        }
                    } else {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        }


    }

}
