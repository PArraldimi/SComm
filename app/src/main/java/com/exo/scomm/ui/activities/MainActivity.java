package com.exo.scomm.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.exo.scomm.R;


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
            if (isNetworkAvailable()) {
                String phoneNumber = "+" + code + number;
                Intent intent = new Intent(MainActivity.this, OtpVerifyActivity.class);
                intent.putExtra("phonenumber", phoneNumber);
                startActivity(intent);
            } else {
                Toast.makeText(this, "No internet!! Please connect to internet and try again", Toast.LENGTH_SHORT).show();
            }

        });
    }

    public boolean isNetworkAvailable() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }


    @Override
    protected void onStart() {
        super.onStart();
//        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
//            getUser();
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
