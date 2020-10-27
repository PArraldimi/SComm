package com.exo.scomm.ui.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.exo.scomm.R;
import com.exo.scomm.adapters.DataHolder;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {
    private static final int GALLERY_PICK = 1;
    private static final String TAG = SetupActivity.class.getSimpleName();
    private CircleImageView setupImage;
    private boolean isChanged = false;
    private TextInputEditText setupName;
    private Button setupBtn;
    private String user_id;
    private Uri filePath;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private ProgressDialog mSetupProgress;
    private DatabaseReference usersRef;

    @Override
    protected void onStart() {
        super.onStart();
        usersRef.child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.child("username").exists()) {
                    startActivity(new Intent(SetupActivity.this, HomeActivity.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        firebaseAuth = FirebaseAuth.getInstance();
        user_id = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        usersRef = FirebaseDatabase.getInstance().getReference("Users").child(user_id);
        setupImage = findViewById(R.id.setup_image);
        setupName = findViewById(R.id.setup_userName);

        setupBtn = findViewById(R.id.setup_btn);
        mSetupProgress = new ProgressDialog(this);

        setupBtn.setEnabled(true);

        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (Objects.requireNonNull(task.getResult()).exists()) {
                    String name = task.getResult().getString("name");
                    String image = task.getResult().getString("image");
                    if (image != null) {
                        filePath = Uri.parse(image);
                        setupName.setText(name);
                        Picasso.get().load(filePath).placeholder(R.drawable.scomm_user_placeholder_white).into(setupImage);
                    }
                }
            }
            mSetupProgress.dismiss();
            setupBtn.setEnabled(true);
        });


        setupBtn.setOnClickListener(v -> {
            final String user_name = Objects.requireNonNull(setupName.getText()).toString();
            if (TextUtils.isEmpty(user_name)) {
                Toast.makeText(getApplicationContext(), "Enter Username", Toast.LENGTH_SHORT).show();
            } else {
                setupBtn.setEnabled(true);
                updateAccount(user_name);
            }
        });

        setupImage.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ///Toast.makeText(SetupActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), GALLERY_PICK);
            }
        });
    }

    private void updateAccount(final String user_name) {
        mSetupProgress.setTitle("Setting Up Account");
        mSetupProgress.setMessage("Please wait while we setUp your Account");
        mSetupProgress.setCanceledOnTouchOutside(false);
        mSetupProgress.show();

        final String user_id = firebaseAuth.getCurrentUser().getUid();
        final StorageReference image_path = storageReference.child("profile_images").child(user_id + ".jpg");

        if (isChanged) {
            Task uploadTask = image_path.putFile(filePath);
            uploadTask.continueWithTask((Continuation) task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return image_path.getDownloadUrl();
            }).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        mSetupProgress.dismiss();
                        Uri download_uri = (Uri) task.getResult();

                        if (download_uri != null) {
                            storeFirestore(download_uri, user_name);
                        } else {
                            download_uri = filePath;
                        }
                        storeFirestore(download_uri, user_name);

                    } else {
                        String error = Objects.requireNonNull(task.getException()).getMessage();
                        Toast.makeText(SetupActivity.this, "IMAGE    Error : " + error, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "" + task.getException().getMessage());
                        mSetupProgress.dismiss();
                    }
                }
            });
        } else {
            storeFirestore(null, user_name);
        }
    }

    private void storeFirestore(final Uri download_uri, final String user_name) {
        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", user_name);
        if (download_uri != null) {
            userMap.put("image", download_uri.toString());
            storeUserDetails(download_uri, user_name, userMap);
        } else {
            storeUserDetails(null, user_name, userMap);

        }
    }

    private void storeUserDetails(final Uri download_uri, final String user_name, Map<String, String> userMap) {
        firebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    mSetupProgress.dismiss();

                    FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(SetupActivity.this, instanceIdResult -> {
                        String token = instanceIdResult.getToken();
                        HashMap<String, String> hashMap = getStringStringHashMap(user_id, user_name, token, download_uri);
                        usersRef.setValue(hashMap).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Toast.makeText(SetupActivity.this, "The User Settings are Successfully Updated ", Toast.LENGTH_LONG).show();
                                Intent mainIntent = new Intent(SetupActivity.this, HomeActivity.class);
                                startActivity(mainIntent);
                                finish();
                            }
                        });

                    });
                } else {
                    mSetupProgress.dismiss();
                    String error = Objects.requireNonNull(task.getException()).getMessage();
                    Toast.makeText(SetupActivity.this, "FIRESTORE  Error : " + error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private HashMap<String, String> getStringStringHashMap(String userid, String user_name, String s, Uri download_uri) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("device_token", s);
        hashMap.put("id", userid);
        hashMap.put("username", user_name);
        hashMap.put("phone", DataHolder.getPhone());
        hashMap.put("status", "offline");
        hashMap.put("search", user_name.toLowerCase());
        if (download_uri != null) {
            hashMap.put("image", download_uri.toString());
        } else {
            hashMap.put("image", null);
        }
        return hashMap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        if (requestCode == GALLERY_PICK
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {
            // Get the Uri of data
            filePath = data.getData();
            try {

                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                getContentResolver(),
                                filePath);
                setupImage.setImageBitmap(bitmap);
                isChanged = true;
            } catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }

//      if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
//         Uri imageUri = data.getData();
//         CropImage.activity(imageUri)
//                 .setAspectRatio(1, 1)
//                 .start(this);
//      }

     /* if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
         CropImage.ActivityResult result = CropImage.getActivityResult(data);
         if (resultCode == RESULT_OK) {
            mSetupProgress = new ProgressDialog(SetupActivity.this);
            mSetupProgress.setTitle("Uploading Image");
            mSetupProgress.setMessage("Please Wait while we upload and process the image");
            mSetupProgress.setCanceledOnTouchOutside(false);
            mSetupProgress.show();

            // Uri resultUri = result.getUri();
            mainImageURI = result.getUri();
            File thumb_file = new File(Objects.requireNonNull(mainImageURI.getPath()));
            String currentUserId = firebaseAuth.getCurrentUser().getUid();
            final StorageReference filePath = storageReference.child("profile_images").child(currentUserId + ".jpg");
            //Bitmap compressedImageFile = new Compressor(this).compressToBitmap(filePath);
            filePath.putFile(mainImageURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
               @Override
               public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                  filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                     @Override
                     public void onSuccess(Uri uri) {
                        String downloadUrl = uri.toString();
                        mDatabase.child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                           @Override
                           public void onComplete(@NonNull Task<Void> task) {
                              if (task.isSuccessful()) {
                                 mSetupProgress.dismiss();
                              }
                           }
                        });
                     }
                  });
               }
            });*/
        // }
        // }
    }
}
