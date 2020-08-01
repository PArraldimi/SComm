package com.exo.scomm.ui.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.exo.scomm.R;

public class SplashActivity extends AppCompatActivity {

    private final int SPLASH_DELAY = 2500;
    private ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

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
        new Handler().postDelayed(()->{

            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, SPLASH_DELAY);
    }

}
