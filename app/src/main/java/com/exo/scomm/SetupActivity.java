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
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private CircleImageView setupImage;
    private Uri mainImageURI = null;

    private boolean isChanged = false;

    private TextInputEditText setupName;
    private Button setupBtn;
    private ProgressBar setupProgress;

    private String user_id;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
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

                if (task.isSuccessful()){

                    if (Objects.requireNonNull(task.getResult()).exists()){

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


                if (!TextUtils.isEmpty(user_name)  && mainImageURI != null){
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

                                if (!task.isSuccessful()){

                                    throw task.getException();

                                }
                                return image_path.getDownloadUrl();
                            }
                        })
                                .addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {

                                        if (task.isSuccessful()){


                                            storeFirestore(task, user_name);


                                        }else {

                                            String error = Objects.requireNonNull(task.getException()).getMessage();
                                            Toast.makeText(SetupActivity.this, "IMAGE    Error : " +error, Toast.LENGTH_LONG ).show();

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

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

                    if(ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

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

        if (task != null){

            download_uri = (Uri) task.getResult();

        }else{

            download_uri = mainImageURI;

        }

        Map<String, String> userMap = new HashMap<>();

        userMap.put("name", user_name);
        assert download_uri != null;
        userMap.put("image", download_uri.toString());




        firebaseFirestore.collection("users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){


                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    assert firebaseUser != null;
                    String userid = firebaseUser.getUid();

                    reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("id", userid);
                    hashMap.put("username", user_name);
                    hashMap.put("status", "offline");
                    hashMap.put("search", user_name.toLowerCase());
                    hashMap.put("image", download_uri.toString());

                    reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                Toast.makeText(SetupActivity.this, "The User Settings are Sucesfully Updated " , Toast.LENGTH_LONG ).show();
                                Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                                startActivity(mainIntent);
                                finish();
                            }
                        }
                    });




                    Toast.makeText(SetupActivity.this, "The User Settings are Sucesfully Updated " , Toast.LENGTH_LONG ).show();
                    Intent mainIntent = new Intent(SetupActivity.this, HomeActivity.class);
                    startActivity(mainIntent);


                }else {

                    String error = Objects.requireNonNull(task.getException()).getMessage();
                    Toast.makeText(SetupActivity.this, "FIRESTORE  Error : " +error, Toast.LENGTH_LONG ).show();


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
        CropImage.ActivityResult result = null;
        mainImageURI = result.getUri();
        setupImage.setImageURI(mainImageURI);

        isChanged = true;


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }

    }
}
