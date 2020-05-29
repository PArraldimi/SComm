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
    TextView mDisplayName, mDisplayStatus, mFriendsCount, mFriendsMessage;
    Button mSendFriendReqBtn, mSendInviteReqBtn, mDeclineFriendReqBtn;
    private DatabaseReference mDatabaseRef;
    private DatabaseReference mInvitesReqDBRef;
    private DatabaseReference mCompanionsDatabase, mUsersRef, mFriendsReqDBRef;
    private DatabaseReference mNotificationsDatabase;
    private ProgressDialog mProgressDialog;
    private FirebaseUser mCurrentUser;
    private int mCurrentState;
    private int mCompanionsState;
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
        mUsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mCompanionsDatabase = FirebaseDatabase.getInstance().getReference().child("Companions");
        mNotificationsDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mFriendsReqDBRef = FirebaseDatabase.getInstance().getReference().child("Friends_Requests");

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        imageView = findViewById(R.id.profile_image_view);
        mDisplayName = findViewById(R.id.profile_displayName);
        mDisplayStatus = findViewById(R.id.profile_displayStatus);
        mFriendsCount = findViewById(R.id.profile_friendsCount);
        mFriendsMessage = findViewById(R.id.profile_friends_msg);
        mSendFriendReqBtn = findViewById(R.id.send_freReq_btn);
        mDeclineFriendReqBtn = findViewById(R.id.decline_freReq_btn);
        mSendInviteReqBtn = findViewById(R.id.send_inviteReq_btn);

        mCurrentState = 0;
        mCompanionsState = 0;
        /*
        mCurrentState = 0 == not friends;
        mCurrentState = 1 == req sent;
        mCurrentState = 2 == req received;
        mCurrentState = 3 == friends;

        * mCompanionsState = 0 == not companions
        * mCompanionsState = 1 == invite req sent
        * mCompanionsState = 2 == invite request received
        * mCompanionsState = 3 == companions
        *
        * */


        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setMessage("Please waite while we load the user data");
        mProgressDialog.show();

        mSendFriendReqBtn.setEnabled(true);
        mFriendsMessage.setText("You are not friends");
        mSendInviteReqBtn.setVisibility(View.INVISIBLE);
        mSendInviteReqBtn.setEnabled(false);
        mDeclineFriendReqBtn.setVisibility(View.INVISIBLE);

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mName = Objects.requireNonNull(dataSnapshot.child("username").getValue()).toString();
                String status = Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();
                String image = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();

                mDisplayName.setText(mName);
                mDisplayStatus.setText(status);
                Picasso.get().load(image).placeholder(R.drawable.profile_image_placeholder).into(imageView);

//                <-------------------------FRIENDS LIST / REQUEST FEATURE-------------->
                mInvitesReqDBRef.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user_id)) {

                            String req_type = Objects.requireNonNull(dataSnapshot.child(user_id).child("request_type").getValue()).toString();
                            if (req_type.equals("received")) {
                                mCurrentState = 2;
                                mSendFriendReqBtn.setText("Accept Friend Request");

                                mSendInviteReqBtn.setVisibility(View.INVISIBLE);
                                mSendInviteReqBtn.setEnabled(false);
                                mDeclineFriendReqBtn.setVisibility(View.VISIBLE);
                                mDeclineFriendReqBtn.setEnabled(true);

                            } else if (req_type.equals("sent")) {
                                mCurrentState = 1;
                                mSendFriendReqBtn.setText("Cancel Friend Request");

                                mSendInviteReqBtn.setVisibility(View.INVISIBLE);
                                mSendInviteReqBtn.setEnabled(false);
                                mDeclineFriendReqBtn.setVisibility(View.INVISIBLE);
                                mDeclineFriendReqBtn.setEnabled(false);
                            }
                            mProgressDialog.dismiss();

                        } else {
//              <---------------------------FRIENDS ----------------->
                            mCompanionsDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)) {
                                        mCurrentState = 3;
                                        mFriendsMessage.setText("You are friends");
                                        mSendFriendReqBtn.setText("Unfriend " + mName);

                                        mSendInviteReqBtn.setVisibility(View.VISIBLE);
                                        mSendInviteReqBtn.setEnabled(true);
                                        mSendInviteReqBtn.setText("Send Invitation Request");

                                        mDeclineFriendReqBtn.setVisibility(View.INVISIBLE);
                                        mDeclineFriendReqBtn.setEnabled(false);
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

        mSendFriendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //---------------NOT FRIENDS  STATE ------------
                if (mCurrentState == 0) {
                    mSendFriendReqBtn.setEnabled(false);
                    DatabaseReference newNotificationRef = mRootRef.child("Notifications").child(user_id).push();
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

                    Map requestReceivedData = new HashMap();
                    requestReceivedData.put("request_type", "received");
                    requestReceivedData.put("date", ServerValue.TIMESTAMP);
                    requestReceivedData.put("accepted", "false");

                    Map requestMap = new HashMap<>();
                    requestMap.put("Friends_Requests/" + mCurrentUser.getUid() + "/" + user_id + "/", requestSentData);
                    requestMap.put("Friends_Requests/" + user_id + "/" + mCurrentUser.getUid() + "/", requestReceivedData);
                    requestMap.put("Notifications/" + user_id + "/" + newNotificationId, notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Toast.makeText(Profile.this, "There was some error in sending request", Toast.LENGTH_SHORT).show();
                            }

                            mSendFriendReqBtn.setEnabled(true);
                            mCurrentState = 1;
                            mSendFriendReqBtn.setText("Cancel Friend Request");

                            mSendInviteReqBtn.setVisibility(View.INVISIBLE);
                            mSendInviteReqBtn.setEnabled(false);

                            mDeclineFriendReqBtn.setVisibility(View.INVISIBLE);
                            mDeclineFriendReqBtn.setEnabled(false);
                        }
                    });


                }
                //                ------------------------------CANCEL FRIENDS REQUEST STATE---------------
                if (mCurrentState == 1) {

                    Map requestMap = new HashMap<>();
                    requestMap.put("Friends_Requests/" + mCurrentUser.getUid() + "/" + user_id + "/", null);
                    requestMap.put("Friends_Requests/" + user_id + "/" + mCurrentUser.getUid() + "/", null);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Toast.makeText(Profile.this, "There was some error in sending request", Toast.LENGTH_SHORT).show();
                            }
                            mSendFriendReqBtn.setEnabled(true);
                            mCurrentState = 0;
                            mSendFriendReqBtn.setText("Send Friend Request");

                            mSendInviteReqBtn.setVisibility(View.INVISIBLE);
                            mSendInviteReqBtn.setEnabled(false);

                            mDeclineFriendReqBtn.setVisibility(View.INVISIBLE);
                            mDeclineFriendReqBtn.setEnabled(false);
                        }
                    });

                    /*mFriendsReqDBRef.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendsReqDBRef.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mSendFriendReqBtn.setEnabled(true);
                                    mCurrentState = 0;
                                    mSendFriendReqBtn.setText("Send Friend Request");

                                    mDeclineFriendReqBtn.setVisibility(View.INVISIBLE);
                                    mDeclineFriendReqBtn.setEnabled(false);
                                }
                            });

                        }
                    });*/

                }

                //                ------------------------------FRIENDS REQUEST RECEIVED STATE---------------
                if (mCurrentState == 2) {
                    final String currentDate = DateFormat.getDateInstance().format(new Date());
                    DatabaseReference newNotificationRef = mRootRef.child("Notifications").child(mCurrentUser.getUid()).push();
                    String newNotificationId = newNotificationRef.getKey();
                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrentUser.getUid());
                    notificationData.put("type", "request_accepted");
                    notificationData.put("to_user", user_id);

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id + "/date", currentDate);
                    friendsMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid() + "/date", currentDate);
                    friendsMap.put("Friends_Requests/" + mCurrentUser.getUid() + "/" + user_id, null);
                    friendsMap.put("Friends_Requests/" + user_id + "/" + mCurrentUser.getUid(), null);
                    friendsMap.put("Notifications/" + user_id + "/" + newNotificationId, notificationData);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                mSendFriendReqBtn.setEnabled(true);
                                mCurrentState = 3;
                                mSendFriendReqBtn.setText("Unfriend " + mName);

                                mSendInviteReqBtn.setVisibility(View.VISIBLE);
                                mSendInviteReqBtn.setEnabled(true);
                                mSendInviteReqBtn.setText("Send Invitation Request");

                                mDeclineFriendReqBtn.setVisibility(View.INVISIBLE);
                                mDeclineFriendReqBtn.setEnabled(false);
                            } else {
                                String error = databaseError.getMessage();
                                Toast.makeText(Profile.this, error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                }
                //----------------------------UNFRIENDS------------------
                if (mCurrentState == 3) {
                    Map unfriendsMap = new HashMap();
                    unfriendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id, null);
                    unfriendsMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid(), null);
                    mRootRef.updateChildren(unfriendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null) {

                                mCurrentState = 0;
                                mSendFriendReqBtn.setText("Send  Friend Request");

                                mSendInviteReqBtn.setVisibility(View.INVISIBLE);
                                mSendInviteReqBtn.setEnabled(false);

                                mDeclineFriendReqBtn.setVisibility(View.INVISIBLE);
                                mDeclineFriendReqBtn.setEnabled(false);
                            } else {
                                String error = databaseError.getMessage();
                                Toast.makeText(Profile.this, error, Toast.LENGTH_SHORT).show();
                            }
                            mSendFriendReqBtn.setEnabled(true);
                        }
                    });
                }
            }

        });


        mSendInviteReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//  <------------------------------NOT COMPANIONS  STATE ------------------------->
                if (mCompanionsState == 0) {

                    DatabaseReference newNotificationRef = mRootRef.child("Notifications").child(user_id).push();
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

                    Map requestReceivedData = new HashMap();
                    requestReceivedData.put("request_type", "received");
                    requestReceivedData.put("date", ServerValue.TIMESTAMP);
                    requestReceivedData.put("accepted", "false");

                    Map requestMap = new HashMap<>();
                    requestMap.put("TaskInviteRequests/" + mCurrentUser.getUid() + "/" + user_id + "/" + task_id, requestSentData);
                    requestMap.put("TaskInviteRequests/" + user_id + "/" + mCurrentUser.getUid() + "/" + task_id, requestReceivedData);
                    requestMap.put("Notifications/" + user_id + "/" + newNotificationId, notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Toast.makeText(Profile.this, "There was some error in sending request", Toast.LENGTH_SHORT).show();
                            }

                            mSendFriendReqBtn.setEnabled(false);
                            mSendInviteReqBtn.setVisibility(View.INVISIBLE);
                            mSendFriendReqBtn.setText("Cancel Friend Request");

                            mSendInviteReqBtn.setVisibility(View.INVISIBLE);
                            mSendInviteReqBtn.setEnabled(true);
                            mCompanionsState = 1;
                            mSendInviteReqBtn.setText("Cancel Invite Request");

                            mDeclineFriendReqBtn.setVisibility(View.INVISIBLE);
                            mDeclineFriendReqBtn.setEnabled(false);
                        }
                    });


                }
//  <------------------------------CANCEL COMPANIONS STATE------------------------>
                if (mCompanionsState == 1) {


                    Map requestMap = new HashMap<>();
                    requestMap.put("TaskInviteRequests/" + mCurrentUser.getUid() + "/" + user_id + "/" + task_id, null);
                    requestMap.put("TaskInviteRequests/" + user_id + "/" + mCurrentUser.getUid() + "/" + task_id, null);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Toast.makeText(Profile.this, "There was some error in sending request", Toast.LENGTH_SHORT).show();
                            }
                            mSendFriendReqBtn.setEnabled(false);
                            mSendFriendReqBtn.setVisibility(View.INVISIBLE);
                            mCompanionsState = 0;

                            mSendInviteReqBtn.setVisibility(View.VISIBLE);
                            mSendInviteReqBtn.setEnabled(true);

                            mDeclineFriendReqBtn.setVisibility(View.INVISIBLE);
                            mDeclineFriendReqBtn.setEnabled(false);
                        }
                    });


                }
//  <------------------------------COMPANIONS REQUEST RECEIVED STATE-------------->
                if (mCompanionsState == 2) {

                    final String currentDate = DateFormat.getDateInstance().format(new Date());
                    DatabaseReference newNotificationRef = mRootRef.child("Notifications").child(mCurrentUser.getUid()).push();
                    String newNotificationId = newNotificationRef.getKey();
                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", user_id);
                    notificationData.put("type", "request_accepted");
                    notificationData.put("to_user", mCurrentUser.getUid());

                    Map companionsMap = new HashMap();
                    companionsMap.put("TaskCompanions/" + mCurrentUser.getUid() + "/" + user_id + "/date", currentDate);
                    companionsMap.put("TaskCompanions/" + user_id + "/" + mCurrentUser.getUid() + "/date", currentDate);
                    companionsMap.put("TaskInviteRequests/" + mCurrentUser.getUid() + "/" + user_id + "/" + task_id, null);
                    companionsMap.put("TaskInviteRequests/" + user_id + "/" + mCurrentUser.getUid() + "/" + task_id, null);
                    companionsMap.put("Notifications/" + user_id + "/" + newNotificationId, notificationData);

                    mRootRef.updateChildren(companionsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                mSendFriendReqBtn.setEnabled(true);
                                mCompanionsState = 3;
                                mSendFriendReqBtn.setText("Unfriend " + mName);

                                mSendInviteReqBtn.setVisibility(View.VISIBLE);
                                mSendInviteReqBtn.setEnabled(true);
                                mSendInviteReqBtn.setText("Cancel Invitation Request");

                                mDeclineFriendReqBtn.setVisibility(View.INVISIBLE);
                                mDeclineFriendReqBtn.setEnabled(false);
                            } else {
                                String error = databaseError.getMessage();
                                Toast.makeText(Profile.this, error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }


//  < ----------------------------REVOKE COMPANION-------------------------------->
                if (mCompanionsState == 3) {
                    Map revokeMap = new HashMap();
                    revokeMap.put("TaskCompanions/" + mCurrentUser.getUid() + "/" + task_id + "/", null);
                    revokeMap.put("TaskCompanions/" + user_id + "/" + task_id + "/", null);
                    mRootRef.updateChildren(revokeMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null) {

                                mCompanionsState = 0;
                                mSendFriendReqBtn.setVisibility(View.INVISIBLE);

                                mSendInviteReqBtn.setVisibility(View.VISIBLE);
                                mSendInviteReqBtn.setEnabled(true);

                                mDeclineFriendReqBtn.setVisibility(View.INVISIBLE);
                                mDeclineFriendReqBtn.setEnabled(false);
                            } else {
                                String error = databaseError.getMessage();
                                Toast.makeText(Profile.this, error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }
        });


        mSendInviteReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //---------------NOT FRIENDS  STATE ------------
                if (mCompanionsState == 0) {
                    mSendInviteReqBtn.setEnabled(false);

                    DatabaseReference newNotificationRef = mRootRef.child("Notifications").child(user_id).push();
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
                    requestMap.put("InviteRequests/" + mCurrentUser.getUid() + "/" + user_id + "/", requestSentData);
                    requestMap.put("InviteRequests/" + user_id + "/" + mCurrentUser.getUid() + "/", requestReceivedData);
                    requestMap.put("Notifications/" + user_id + "/" + newNotificationId, notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Toast.makeText(Profile.this, "There was some error in sending request", Toast.LENGTH_SHORT).show();
                            }

                            mSendFriendReqBtn.setEnabled(true);
                            mCurrentState = 1;
                            mSendFriendReqBtn.setText("Cancel Friend Request");

                            mDeclineFriendReqBtn.setVisibility(View.INVISIBLE);
                            mDeclineFriendReqBtn.setEnabled(false);
                        }
                    });


                }
                //                ------------------------------CANCEL FRIENDS REQUEST STATE---------------
                if (mCurrentState == 1) {
                    mInvitesReqDBRef.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mInvitesReqDBRef.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mSendFriendReqBtn.setEnabled(true);
                                    mCurrentState = 0;
                                    mSendFriendReqBtn.setText("Send Friend Request");

                                    mDeclineFriendReqBtn.setVisibility(View.INVISIBLE);
                                    mDeclineFriendReqBtn.setEnabled(false);
                                }
                            });

                        }
                    });

                }

                //                ------------------------------FRIENDS REQUEST RECEIVED STATE---------------
                if (mCurrentState == 2) {
                    mRootRef.child("InviteRequests").child(mCurrentUser.getUid()).child(user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(task_id)) {

                                final String currentDate = DateFormat.getDateInstance().format(new Date());

                                DatabaseReference newNotificationRef = mRootRef.child("Notifications").child(mCurrentUser.getUid()).push();
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
                                friendsMap.put("TaskCompanions/" + mCurrentUser.getUid() + "/" + task_id + "/", taskCompMap);
                                friendsMap.put("TaskCompanions/" + user_id + "/" + task_id + "/", taskCompMap);
                                friendsMap.put("Notifications/" + user_id + "/" + newNotificationId, notificationData);

                                friendsMap.put("InviteRequests/" + mCurrentUser.getUid() + "/" + user_id, null);
                                friendsMap.put("InviteRequests/" + user_id + "/" + mCurrentUser.getUid(), null);

                                mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                        if (databaseError == null) {
                                            mSendFriendReqBtn.setEnabled(true);
                                            mCurrentState = 3;
                                            mSendFriendReqBtn.setText("Unfriend " + mName);

                                            mDeclineFriendReqBtn.setVisibility(View.INVISIBLE);
                                            mDeclineFriendReqBtn.setEnabled(false);
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
                //----------------------------UNFRIENDS------------------
                if (mCurrentState == 3) {
                    Map unfriendsMap = new HashMap();
                    unfriendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id, null);
                    unfriendsMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid(), null);
                    mRootRef.updateChildren(unfriendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null) {

                                mCurrentState = 0;
                                mSendFriendReqBtn.setText("Send  Friend Request");

                                mDeclineFriendReqBtn.setVisibility(View.INVISIBLE);
                                mDeclineFriendReqBtn.setEnabled(false);
                            } else {
                                String error = databaseError.getMessage();
                                Toast.makeText(Profile.this, error, Toast.LENGTH_SHORT).show();
                            }
                            mSendFriendReqBtn.setEnabled(true);
                        }
                    });
                }
            }

        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mCurrentUser != null) {
            mUsersRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }
}
