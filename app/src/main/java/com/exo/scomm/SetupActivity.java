package com.exo.scomm;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.exo.scomm.adapters.DataHolder;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private static final int GALLERY_PICK = 1;

    private CircleImageView setupImage;
    private Uri mainImageURI = null;
    private boolean isChanged = false;
    private TextInputEditText setupName;
    private Button setupBtn;
    private ProgressBar setupProgress;

    private String user_id;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference mDatabase;
    private FirebaseFirestore firebaseFirestore;
    private ProgressDialog mSetupProgress;

    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        /*Toolbar setupToolbar = findViewById(R.id.setupToolbar);
        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle("Account Setup");

         */

        firebaseAuth = FirebaseAuth.getInstance();
        user_id = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseAuth.getCurrentUser().getUid());


        setupImage = findViewById(R.id.setup_image);
        setupName = findViewById(R.id.setup_userName);

        setupBtn = findViewById(R.id.setup_btn);
        setupProgress = findViewById(R.id.setup_progress);
        mSetupProgress = new ProgressDialog(this);

        setupProgress.setVisibility(View.VISIBLE);
        setupBtn.setEnabled(false);

        firebaseFirestore.collection("users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

            @SuppressLint("CheckResult")
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (Objects.requireNonNull(task.getResult()).exists()) {

                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");

                        mainImageURI = Uri.parse(image);
                        setupName.setText(name);

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.color.colorPrimary);


                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderRequest).load(image).into(setupImage);


                    }
                }

                setupProgress.setVisibility(View.INVISIBLE);
                setupBtn.setEnabled(true);
            }
        });


        setupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String user_name = Objects.requireNonNull(setupName.getText()).toString();


                if (!TextUtils.isEmpty(user_name) && mainImageURI != null) {
                    //setupProgress.setVisibility(View.VISIBLE);

                    mSetupProgress.setTitle("Setting Up Account");
                    mSetupProgress.setMessage("Please wait while we setUp your Account");
                    mSetupProgress.setCanceledOnTouchOutside(false);
                    mSetupProgress.show();

                    if (isChanged) {

                        final String user_id = firebaseAuth.getCurrentUser().getUid();

                        final StorageReference image_path = storageReference.child("profile images").child(user_id + ".jpg");

                        Task uploadTask = image_path.putFile(mainImageURI);

                        uploadTask.continueWithTask(new Continuation() {
                            @Override
                            public Object then(@NonNull Task task) throws Exception {

                                if (!task.isSuccessful()) {

                                    throw task.getException();

                                }

                                return image_path.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {

                                if (task.isSuccessful()) {
                                    storeFirestore(task, user_name);
                                } else {

                                    String error = Objects.requireNonNull(task.getException()).getMessage();
                                    Toast.makeText(SetupActivity.this, "IMAGE    Error : " + error, Toast.LENGTH_LONG).show();

                                    //setupProgress.setVisibility(View.INVISIBLE);
                                    mSetupProgress.dismiss();
                                }
                            }
                        });
                    } else {
                        storeFirestore(null, user_name);
                    }
                }
            }
        });


        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                        Toast.makeText(SetupActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                    } else {

                        BringImagePicker();

                    }

                } else {

                    BringImagePicker();

                }

            }

        });


    }

    private void storeFirestore(Task task, final String user_name) {

        final Uri download_uri;

        if (task != null) {

            download_uri = (Uri) task.getResult();

        } else {

            download_uri = mainImageURI;
        }

        Map<String, String> userMap = new HashMap<>();

        userMap.put("name", user_name);
        assert download_uri != null;
        userMap.put("image", download_uri.toString());
        firebaseFirestore.collection("users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    assert firebaseUser != null;
                    final String userid = firebaseUser.getUid();

                    reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                    FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(SetupActivity.this, new OnSuccessListener<InstanceIdResult>() {
                        @Override
                        public void onSuccess(InstanceIdResult instanceIdResult) {
                            String tokenId = instanceIdResult.getToken();
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("device_token", tokenId);
                            hashMap.put("id", userid);
                            hashMap.put("username", user_name);
                            hashMap.put("status", "offline");
                            hashMap.put("search", user_name.toLowerCase());
                            hashMap.put("image", download_uri.toString());
                            hashMap.put("phone", DataHolder.getPhone());

                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        Toast.makeText(SetupActivity.this, "The User Settings are Successfully Updated ", Toast.LENGTH_LONG).show();
                                        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                                        startActivity(mainIntent);
                                        finish();
                                    }
                                }
                            });
                            Toast.makeText(SetupActivity.this, "The User Settings are Successfully Updated ", Toast.LENGTH_LONG).show();
                            Intent mainIntent = new Intent(SetupActivity.this, HomeActivity.class);
                            startActivity(mainIntent);
                        }
                    });

                } else {

                    String error = Objects.requireNonNull(task.getException()).getMessage();
                    Toast.makeText(SetupActivity.this, "FIRESTORE  Error : " + error, Toast.LENGTH_LONG).show();
                }

                setupProgress.setVisibility(View.INVISIBLE);

            }
        });

    }


    private void BringImagePicker() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(SetupActivity.this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
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
                });

            }

        }
    }
}
