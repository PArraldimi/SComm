package com.exo.scomm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.exo.scomm.fragments.ChatroomFragment;
import com.exo.scomm.fragments.HomeFragment;
import com.exo.scomm.fragments.NotificationFragment;
import com.exo.scomm.fragments.SettingsFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FloatingActionButton add_task;
    BottomNavigationView mainbottomNav;

    private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private ChatroomFragment chatroomFragment;
    private SettingsFragment settingsFragment;

    private static final String FRAGMENT_STACK_KEY = "FRAGMENT_STACK_KEY";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {

            mainbottomNav = findViewById(R.id.bottom_nav);
            add_task = findViewById(R.id.fab);

            // FRAGMENTS
            homeFragment = new HomeFragment();
            notificationFragment = new NotificationFragment();
            chatroomFragment = new ChatroomFragment();
            settingsFragment = new SettingsFragment();

            initializeFragment();

            mainbottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.bottom_home:
                            replaceFragment(homeFragment);
                            add_task.setVisibility(View.VISIBLE);
                            return true;
                        case R.id.bottom_notification:
                            replaceFragment(notificationFragment);
                            add_task.setVisibility(View.GONE);
                            return true;
                        case R.id.bottom_Chatroom:
                            replaceFragment(chatroomFragment);
                            add_task.setVisibility(View.GONE);
                            return true;
                        case R.id.bottom_settings:
                            replaceFragment(settingsFragment);
                            add_task.setVisibility(View.GONE);
                            return true;
                        default:
                            return false;
                    }
                }
            });

            add_task.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeActivity.this, AddTaskActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        //get current tab index.
        int index = mainbottomNav.getSelectedItemId();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        //decide what to do
        if (index == R.id.bottom_home || user==null) {
            finish();
        } else {
            mainbottomNav.setSelectedItemId(R.id.bottom_home);
        }
    }

    private void initializeFragment() {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        fragmentTransaction.add(R.id.main_container, homeFragment);
        fragmentTransaction.add(R.id.main_container, chatroomFragment);
        fragmentTransaction.add(R.id.main_container, notificationFragment);
        fragmentTransaction.add(R.id.main_container, settingsFragment);


        fragmentTransaction.hide(chatroomFragment);
        fragmentTransaction.hide(notificationFragment);
        fragmentTransaction.hide(settingsFragment);


        fragmentTransaction.commit();


    }

    private void replaceFragment(Fragment fragment) {


        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (fragment == homeFragment) {


            fragmentTransaction.hide(chatroomFragment);
            fragmentTransaction.hide(notificationFragment);
            fragmentTransaction.hide(settingsFragment);
            fragmentTransaction.addToBackStack(null);

        }

        if (fragment == notificationFragment) {

            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(settingsFragment);
            fragmentTransaction.hide(chatroomFragment);
            fragmentTransaction.addToBackStack(null);

        }

        if (fragment == chatroomFragment) {

            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(settingsFragment);
            fragmentTransaction.hide(notificationFragment);
            fragmentTransaction.addToBackStack(null);


        }

        if (fragment == settingsFragment) {

            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(chatroomFragment);
            fragmentTransaction.hide(notificationFragment);
            fragmentTransaction.addToBackStack(null);

        }


        fragmentTransaction.show(fragment);

        //fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();

    }

    protected void onStart() {

        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {

            sendToLogin();
        } else {

            String current_user_id = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

            firebaseFirestore.collection("users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()) {

                        if (!Objects.requireNonNull(task.getResult()).exists()) {

                            Intent setupIntent = new Intent(HomeActivity.this, SetupActivity.class);
                            startActivity(setupIntent);
                            finish();

                        }

                    } else {
                        String errorMessage = Objects.requireNonNull(task.getException()).getMessage();
                        Toast.makeText(HomeActivity.this, "Error : " + errorMessage, Toast.LENGTH_LONG).show();
                        Log.e("Error", errorMessage);
                    }

                }
            });
        }
    }


    private void sendToLogin() {

        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(intent);

    }
}
