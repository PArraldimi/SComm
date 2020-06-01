package com.exo.scomm.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.exo.scomm.MainActivity;
import com.exo.scomm.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class SettingsFragment extends Fragment {
    private FirebaseAuth firebaseAuth;
    private String currentUserID;
    private DatabaseReference RootRef;

    private Button UpdateAccountSettings;
    private EditText userName;
    private CircleImageView userProfileImage;

    private static final int GalleryPick = 1;
    private StorageReference UserProfileImagesRef;
    private ProgressDialog loadingBar;


    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("image");

        UpdateAccountSettings = (Button) view.findViewById(R.id.btn_update_profile);
        userName = (EditText) view.findViewById(R.id.edit_username);
        userProfileImage = (CircleImageView) view.findViewById(R.id.profile_image);


        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSettings();
            }
        });

        RetrieveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent galleryIntent = new Intent();
//                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
//                galleryIntent.setType("image/*");
//                startActivityForResult(galleryIntent, GalleryPick);

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(requireContext(),SettingsFragment.this);
            }
        });


        Button logoutbtn = view.findViewById(R.id.btn_logout);

        logoutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout(getContext());
            }
        });


//         Inflate the layout for this fragment
        return view;


    }



    private void RetrieveUserInfo() {
        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("username") && (dataSnapshot.hasChild("image"))))
                        {
                            String retrieveUserName = dataSnapshot.child("username").getValue().toString();
                            String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                            userName.setText(retrieveUserName);
                            Picasso.get().load(retrieveProfileImage).into(userProfileImage);
                        }
                        else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("username")))
                        {
                            String retrieveUserName = dataSnapshot.child("username").getValue().toString();

                            userName.setText(retrieveUserName);
                        }
                        else
                        {
                            userName.setVisibility(View.VISIBLE);
                            //Toast.makeText(getContext(), "Please set & update your profile information...", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void UpdateSettings() {
        String setUserName = userName.getText().toString();

        if (TextUtils.isEmpty(setUserName)) {
            Toast.makeText(getContext(), "Please write your user name first....", Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("id", currentUserID);
            profileMap.put("username", setUserName);
            RootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                startActivity(new Intent(getContext(), MainActivity.class));
                                Toast.makeText(getContext(), "", Toast.LENGTH_SHORT).show();
                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }

    private void logout (Context context) {
        firebaseAuth.signOut();
        startActivity(new Intent(context, MainActivity.class));
    }


}