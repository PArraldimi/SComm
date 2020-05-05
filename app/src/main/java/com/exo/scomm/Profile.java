package com.exo.scomm;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Profile extends AppCompatActivity {
    ImageView imageView;
    TextView mDisplayName, mDisplayStatus, mFriendsCount;
    Button mSendInviteReqBtn, mDeclineInviteReqBtn;
    private DatabaseReference mDatabaseRef;
    private DatabaseReference mInvitesReqDBRef;
    private DatabaseReference mCompanionsDatabase;
    private DatabaseReference mNotificationsDatabase;
    private ProgressDialog mProgressDialog;
    private FirebaseUser mCurrentUser;
    private int mCurrentState;
    private String mName;
    private DatabaseReference mRootRef;
    String task_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("uid");
        task_id = getIntent().getStringExtra("task_id");
        assert user_id != null;
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mInvitesReqDBRef = FirebaseDatabase.getInstance().getReference().child("Invite_Requests");
        mCompanionsDatabase = FirebaseDatabase.getInstance().getReference().child("Companions");
        mNotificationsDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mRootRef = FirebaseDatabase.getInstance().getReference();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        imageView = findViewById(R.id.profile_image_view);
        mDisplayName = findViewById(R.id.profile_displayName);
        mDisplayStatus = findViewById(R.id.profile_displayStatus);
        mFriendsCount = findViewById(R.id.profile_friendsCount);
        mSendInviteReqBtn = findViewById(R.id.send_freReq_btn);
        mDeclineInviteReqBtn = findViewById(R.id.decline_freReq_btn);

        mCurrentState = 0;


        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setMessage("Please waite while we load the user data");
        mProgressDialog.show();

        mDeclineInviteReqBtn.setEnabled(false);
        mDeclineInviteReqBtn.setVisibility(View.INVISIBLE);

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mName = Objects.requireNonNull(dataSnapshot.child("username").getValue()).toString();
                String status = Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();
                String image = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();

                mDisplayName.setText(mName);
                mDisplayStatus.setText(status);
                Picasso.get().load(image).placeholder(R.drawable.profile_image_placeholder).into(imageView);

                //-------------------------FRIENDS LIST / REQUEST FEATURE--------------
                mInvitesReqDBRef.child(mCurrentUser.getUid()).child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(task_id)) {

                            String req_type = Objects.requireNonNull(dataSnapshot.child(task_id).child("request_type").getValue()).toString();
                            if (req_type.equals("received")) {
                                mCurrentState = 2;
                                mSendInviteReqBtn.setText("Accept Invite Request");

                                mDeclineInviteReqBtn.setVisibility(View.VISIBLE);
                                mDeclineInviteReqBtn.setEnabled(true);

                            } else if (req_type.equals("sent")) {
                                mCurrentState = 1;
                                mSendInviteReqBtn.setText("Cancel Invite Request");

                                mDeclineInviteReqBtn.setVisibility(View.INVISIBLE);
                                mDeclineInviteReqBtn.setEnabled(false);
                            }
                            mProgressDialog.dismiss();

                        } else {
//---------------------------COMPANIONS -----------------
                            mCompanionsDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)) {
                                        mCurrentState = 3;
                                        mSendInviteReqBtn.setText("Unfriend " + mName);

                                        mDeclineInviteReqBtn.setVisibility(View.INVISIBLE);
                                        mDeclineInviteReqBtn.setEnabled(false);
                                    }
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mSendInviteReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //---------------NOT COMPANIONS  STATE ------------
                if (mCurrentState == 0) {
                    mSendInviteReqBtn.setEnabled(false);
                    DatabaseReference newNotificationRef = mRootRef.child("notifications").child(user_id).push();
                    String newNotificationId = newNotificationRef.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrentUser.getUid());
                    notificationData.put("type", "request");
                    notificationData.put("task_id", task_id);
                    notificationData.put("to_user", user_id);

                    Map requestSentData = new HashMap();
                    requestSentData.put("request_type", "sent");
                    requestSentData.put("date", ServerValue.TIMESTAMP);
                    requestSentData.put("accepted", "false");
                    requestSentData.put("task_id", task_id);

                    Map requestReceivedData = new HashMap();
                    requestReceivedData.put("request_type", "received");
                    requestReceivedData.put("date", ServerValue.TIMESTAMP);
                    requestReceivedData.put("accepted", "false");
                    requestReceivedData.put("task_id", task_id);

                    Map requestMap = new HashMap<>();
                    requestMap.put("Invite_Requests/" + mCurrentUser.getUid() + "/" + user_id + "/", requestSentData);
                    requestMap.put("Invite_Requests/" + user_id + "/" + mCurrentUser.getUid() + "/", requestReceivedData);
                    requestMap.put("notifications/" + user_id + "/" + newNotificationId, notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Toast.makeText(Profile.this, "There was some error in sending request", Toast.LENGTH_SHORT).show();
                            }

                            mSendInviteReqBtn.setEnabled(true);
                            mCurrentState = 1;
                            mSendInviteReqBtn.setText("Cancel Friend Request");

                            mDeclineInviteReqBtn.setVisibility(View.INVISIBLE);
                            mDeclineInviteReqBtn.setEnabled(false);
                        }
                    });


                }
                //                ------------------------------CANCEL REQUEST STATE---------------
                if (mCurrentState == 1) {
                    mInvitesReqDBRef.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mInvitesReqDBRef.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mSendInviteReqBtn.setEnabled(true);
                                    mCurrentState = 0;
                                    mSendInviteReqBtn.setText("Send Invite Request");

                                    mDeclineInviteReqBtn.setVisibility(View.INVISIBLE);
                                    mDeclineInviteReqBtn.setEnabled(false);
                                }
                            });

                        }
                    });

                }

                //                ------------------------------REQUEST RECEIVED STATE---------------
                if (mCurrentState == 2) {
                    mRootRef.child("Invite_Requests").child(mCurrentUser.getUid()).child(user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(task_id)) {

                                final String currentDate = DateFormat.getDateInstance().format(new Date());

                                DatabaseReference newNotificationRef = mRootRef.child("notifications").child(mCurrentUser.getUid()).push();
                                String newNotificationId = newNotificationRef.getKey();

                                HashMap<String, String> notificationData = new HashMap<>();
                                notificationData.put("from", mCurrentUser.getUid());
                                notificationData.put("type", "request_accepted");
                                notificationData.put("task_id", task_id);
                                notificationData.put("to_user", user_id);

                                Map taskCompMap = new HashMap();
                                taskCompMap.put("task_id", task_id);
                                taskCompMap.put("date", currentDate);
                                taskCompMap.put("task_owner", user_id);

                                Map friendsMap = new HashMap();
                                friendsMap.put("Companions/" + mCurrentUser.getUid() + "/" + user_id + "/date", currentDate);
                                friendsMap.put("Companions/" + user_id + "/" + mCurrentUser.getUid() + "/date", currentDate);
                                friendsMap.put("task_companions/" + mCurrentUser.getUid() + "/" + task_id + "/", taskCompMap);
                                friendsMap.put("task_companions/" + user_id + "/" + task_id + "/", taskCompMap);
                                friendsMap.put("notifications/" + user_id+ "/" + newNotificationId, notificationData);

                                friendsMap.put("Invite_Requests/" + mCurrentUser.getUid() + "/" + user_id, null);
                                friendsMap.put("Invite_Requests/" + user_id + "/" + mCurrentUser.getUid(), null);

                                mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                        if (databaseError == null) {
                                            mSendInviteReqBtn.setEnabled(true);
                                            mCurrentState = 3;
                                            mSendInviteReqBtn.setText("Uninvite " + mName);

                                            mDeclineInviteReqBtn.setVisibility(View.INVISIBLE);
                                            mDeclineInviteReqBtn.setEnabled(false);
                                        } else {
                                            String error = databaseError.getMessage();
                                            Toast.makeText(Profile.this, error, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }
        });
    }
}
