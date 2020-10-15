package com.exo.scomm.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.exo.scomm.R;
import com.google.firebase.auth.FirebaseAuth;


public class MainActivity extends AppCompatActivity {

    private Spinner spinner;
    private EditText phoneEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinner = findViewById(R.id.spinnerCountries);
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, CountryData.countryNames));
        phoneEditText = findViewById(R.id.editTextPhone);
        findViewById(R.id.btn_send).setOnClickListener(v -> {
            String code = CountryData.countryAreaCodes[spinner.getSelectedItemPosition()];
            String number = phoneEditText.getText().toString().trim();
            if (number.isEmpty() || number.length() < 10) {
                phoneEditText.setError("Valid number is required");
                phoneEditText.requestFocus();
                return;
            }

            String phoneNumber = "+" + code + number;
            Intent intent = new Intent(MainActivity.this, OtpVerifyActivity.class);
            intent.putExtra("phonenumber", phoneNumber);
            startActivity(intent);

        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        //else {
//            FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    User user = snapshot.getValue(User.class);
//                    if (user.getUsername() == null) {
//                        startActivity(new Intent(getApplicationContext(), SetupActivity.class));
//                    } else {
//                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        startActivity(intent);
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//
//                }
//            });
//        }
    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}
