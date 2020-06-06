package com.exo.scomm.ui.activities;

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

import com.exo.scomm.R;
import com.exo.scomm.ui.fragments.ChatroomFragment;
import com.exo.scomm.ui.fragments.HomeFragment;
import com.exo.scomm.ui.fragments.NotificationFragment;
import com.exo.scomm.ui.fragments.SettingsFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {
   private FirebaseAuth firebaseAuth;
   private FirebaseFirestore firebaseFirestore;
   public FloatingActionButton add_task;
   public BottomNavigationView mainBottomNav;
   private DatabaseReference mRootRef;
   private FirebaseUser mCurrentUser;

   public HomeFragment homeFragment;
   public NotificationFragment notificationFragment;
   public ChatroomFragment chatroomFragment;
   public SettingsFragment settingsFragment;

   private static final String FRAGMENT_STACK_KEY = "FRAGMENT_STACK_KEY";
   public String username;
   public String uid;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_home);

      firebaseFirestore = FirebaseFirestore.getInstance();
      firebaseAuth = FirebaseAuth.getInstance();
      mRootRef = FirebaseDatabase.getInstance().getReference();
      mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();


      if (firebaseAuth.getCurrentUser() != null) {

         mainBottomNav = findViewById(R.id.bottom_nav);
         add_task = findViewById(R.id.fab);

         // FRAGMENTS
         homeFragment = new HomeFragment();
         notificationFragment = new NotificationFragment();
         chatroomFragment = new ChatroomFragment();
         settingsFragment = new SettingsFragment();

         initializeFragment();

         mainBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
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
                  case R.id.bottom_chat_room:
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
      int index = mainBottomNav.getSelectedItemId();
      FirebaseUser user = firebaseAuth.getCurrentUser();
      //decide what to do
      if (index == R.id.bottom_home || user == null) {
         finish();
      } else {
         mainBottomNav.setSelectedItemId(R.id.bottom_home);
      }
   }

   private void initializeFragment() {

      FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

      fragmentTransaction.add(R.id.main_container, homeFragment);
      fragmentTransaction.add(R.id.main_container, chatroomFragment);
      fragmentTransaction.add(R.id.main_container, notificationFragment);
      fragmentTransaction.add(R.id.main_container, settingsFragment);

      fragmentTransaction.detach(chatroomFragment);
      fragmentTransaction.detach(notificationFragment);
      fragmentTransaction.detach(settingsFragment);
      fragmentTransaction.commit();

   }

   public void replaceFragment(Fragment fragment) {

      FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
      if (fragment == homeFragment) {

         fragmentTransaction.detach(chatroomFragment);
         fragmentTransaction.detach(notificationFragment);
         fragmentTransaction.detach(settingsFragment);
         fragmentTransaction.addToBackStack(null);

      }

      if (fragment == notificationFragment) {
         fragmentTransaction.detach(homeFragment);
         fragmentTransaction.detach(settingsFragment);
         fragmentTransaction.detach(chatroomFragment);
         fragmentTransaction.addToBackStack(null);

      }

      if (fragment == chatroomFragment) {
         fragmentTransaction.detach(homeFragment);
         fragmentTransaction.detach(settingsFragment);
         fragmentTransaction.detach(notificationFragment);
         fragmentTransaction.addToBackStack(null);
      }

      if (fragment == settingsFragment) {
         fragmentTransaction.detach(homeFragment);
         fragmentTransaction.detach(chatroomFragment);
         fragmentTransaction.detach(notificationFragment);
         fragmentTransaction.addToBackStack(null);
      }


      fragmentTransaction.attach(fragment);

      //fragmentTransaction.replace(R.id.main_container, fragment);
      fragmentTransaction.commit();

   }

   protected void onStart() {

      super.onStart();

      FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

      if (currentUser == null) {
         sendToLogin();
      } else {

        mRootRef.child("Users").child(mCurrentUser.getUid()).child("status").setValue("online");

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

   @Override
   protected void onResume() {
      super.onResume();
      Intent i = getIntent();
      String data = i.getStringExtra("fromTaskDetails");
      String data1 = i.getStringExtra("fromCompanions");

      if (data != null && data.contentEquals("1")) {
         username = i.getStringExtra("username");
         uid = i.getStringExtra("userId");

         Log.e("HomeResume", "" + username + " " + uid);

         replaceFragment(chatroomFragment);
         mainBottomNav.setSelectedItemId(R.id.bottom_chat_room);
         add_task.setVisibility(View.GONE);

      }
      if (data1 != null && data1.contentEquals("1")) {
         username = i.getStringExtra("username");
         uid = i.getStringExtra("uid");

         Log.e("HomeResume", "" + username + " " + uid);

         replaceFragment(chatroomFragment);
         mainBottomNav.setSelectedItemId(R.id.bottom_chat_room);
         add_task.setVisibility(View.GONE);

      }
   }

   private void sendToLogin() {
      Intent intent = new Intent(HomeActivity.this, MainActivity.class);
      startActivity(intent);

   }

   @Override
   protected void onStop() {
      super.onStop();
      mRootRef.child("Users").child(mCurrentUser.getUid()).child("online").setValue(ServerValue.TIMESTAMP);
   }
}
