package com.exo.scomm.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.exo.scomm.R;
import com.exo.scomm.adapters.DataHolder;
import com.exo.scomm.data.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class OtpVerifyActivity extends AppCompatActivity {


    String mPhone;
    private String verificationId;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private EditText editText;
    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                editText.setText(code);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(OtpVerifyActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verify);
        mAuth = FirebaseAuth.getInstance();
        DatabaseReference mUsersDBRef = FirebaseDatabase.getInstance().getReference().child("Users");
        progressBar = findViewById(R.id.progressbar);
        editText = findViewById(R.id.editTextCode);
        TextView changeNo = findViewById(R.id.change_number);
        mPhone = getIntent().getStringExtra("phonenumber");
        sendVerificationCode(mPhone);
        DataHolder.setPhone(mPhone);
        findViewById(R.id.buttonVerify).setOnClickListener(v -> {
            String code = editText.getText().toString().trim();
            if (code.isEmpty() || code.length() < 6) {
                editText.setError("Enter code...");
                editText.requestFocus();
                return;
            }
            verifyCode(code);
        });
        changeNo.setOnClickListener(v -> {
            Intent intent = new Intent(OtpVerifyActivity.this, MainActivity.class);
            startActivity(intent);
        });


    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                       // final String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                        DataHolder.setPhone(mPhone);
                        startActivity(new Intent(getApplicationContext(), SetupActivity.class));
                        OtpVerifyActivity.this.finish();
                    } else {
                        Toast.makeText(OtpVerifyActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendVerificationCode(String number) {
        progressBar.setVisibility(View.VISIBLE);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                120,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallBack
        );

    }
}
