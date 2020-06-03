package com.exo.scomm;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
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
    private final static String LOG_TAG = Profile.class.getSimpleName();
    TextView mDisplayName, mDisplayStatus, mFriendsCount;
    Button  mSendInviteReq, mDeclineInviteReq;
    private DatabaseReference mDatabaseRef;
    private DatabaseReference mInvitesReqDBRef;
    private DatabaseReference mCompanionsDatabase, mUsersRef, mFriendsReqDBRef;
    private DatabaseReference mNotificationsDatabase;
    private ProgressDialog mProgressDialog;
    private FirebaseUser mCurrentUser;
    private int mCompanionsState;
    private String mName;
    private DatabaseReference mRootRef;
    String task_id;

    @Override
    protected void onStart() {
        super.onStart();
        mRootRef.child("Users").child(mCurrentUser.getUid()).child("status").setValue("online");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("uid");
        task_id = getIntent().getStringExtra("task_id");
        assert user_id != null;
       Log.e(LOG_TAG, "UserI"+user_id);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mInvitesReqDBRef = FirebaseDatabase.getInstance().getReference().child("TaskInviteRequests");
        mUsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mCompanionsDatabase = FirebaseDatabase.getInstance().getReference().child("TaskCompanions");
        mNotificationsDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        imageView = findViewById(R.id.profile_image_view);
        mDisplayName = findViewById(R.id.profile_displayName);
        mDisplayStatus = findViewById(R.id.profile_displayStatus);

        mDeclineInviteReq = findViewById(R.id.decline_inviteReq);
        mSendInviteReq = findViewById(R.id.send_inviteReq);

        mCompanionsState = 0;
        /*
        * mCompanionsState = 0 == not companions
        * mCompanionsState = 1 == invite req sent
        * mCompanionsState = 2 == invite request received
        * mCompanionsState = 3 == companions
        *
        * */

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setMessage("Loading Please wait!!");
        mProgressDialog.show();

        mSendInviteReq.setVisibility(View.VISIBLE);
        mSendInviteReq.setEnabled(true);
        mDeclineInviteReq.setVisibility(View.INVISIBLE);

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
                        if (task_id != null) {
                        if (dataSnapshot.hasChild(user_id)) {
                            String req_type = Objects.requireNonNull(dataSnapshot.child(task_id).child("request_type").getValue()).toString();
                            if (req_type.equals("received")) {
                                mCompanionsState = 2;

                                mSendInviteReq.setVisibility(View.VISIBLE);
                                mSendInviteReq.setEnabled(true);
                                mSendInviteReq.setText("Accept Invite Request");

                                mDeclineInviteReq.setVisibility(View.VISIBLE);
                                mDeclineInviteReq.setEnabled(true);

                            } else if (req_type.equals("request_sent")) {
                                mCompanionsState = 1;
                                mSendInviteReq.setText("Cancel Invite Request");
                                mSendInviteReq.setVisibility(View.VISIBLE);
                                mSendInviteReq.setEnabled(true);
                                mDeclineInviteReq.setVisibility(View.INVISIBLE);
                                mDeclineInviteReq.setEnabled(false);
                            }
                            mProgressDialog.dismiss();

                        } else {
//              <---------------------------FRIENDS ----------------->
                            mCompanionsDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)) {
                                        mCompanionsState = 3;
                                        mSendInviteReq.setText(String.format("Uniinvite %s", mName));
                                        mSendInviteReq.setVisibility(View.VISIBLE);
                                        mSendInviteReq.setEnabled(true);

                                        mDeclineInviteReq.setVisibility(View.INVISIBLE);
                                        mDeclineInviteReq.setEnabled(false);
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

        mSendInviteReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//  <------------------------------NOT COMPANIONS  STATE ------------------------->
                if (mCompanionsState == 0) {
                    String recipientNoteKey = mRootRef.child("Notifications").child(user_id).push().getKey();
                    String senderNoteKey = mRootRef.child("Notifications").child(mCurrentUser.getUid()).push().getKey();
                    final String currentDate = DateFormat.getDateInstance().format(new Date());


                    HashMap<String, String> recipientNote = new HashMap<>();
                    recipientNote.put("fromUser", mCurrentUser.getUid());
                    recipientNote.put("type", "received");
                    recipientNote.put("task_id", task_id);
                    recipientNote.put("date", currentDate);


                    HashMap<String, String> senderNote = new HashMap<>();
                    senderNote.put("toUser", user_id);
                    senderNote.put("type", "sent");
                    senderNote.put("task_id", task_id);
                    senderNote.put("date", currentDate);

                    Map requestSentData = new HashMap();
                    requestSentData.put("request_type", "sent");
                   requestSentData.put("task_id", task_id);
                   requestSentData.put("date", ServerValue.TIMESTAMP);
                    requestSentData.put("accepted", "false");

                    Map requestReceivedData = new HashMap();
                    requestReceivedData.put("request_type", "received");
                    requestReceivedData.put("date", ServerValue.TIMESTAMP);
                   requestReceivedData.put("task_id", task_id);
                   requestReceivedData.put("accepted", "false");

                    Map requestMap = new HashMap<>();
                    requestMap.put("TaskInviteRequests/" + mCurrentUser.getUid() + "/" + user_id + "/" + task_id, requestSentData);
                    requestMap.put("TaskInviteRequests/" + user_id + "/" + mCurrentUser.getUid() + "/" + task_id, requestReceivedData);
                    requestMap.put("Notifications/" + mCurrentUser.getUid() + "/" + senderNoteKey, senderNote);
                    requestMap.put("Notifications/" + user_id + "/" + recipientNoteKey, recipientNote);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Toast.makeText(Profile.this, "There was some error in sending request", Toast.LENGTH_SHORT).show();
                            }
                            mSendInviteReq.setEnabled(true);
                            mSendInviteReq.setVisibility(View.VISIBLE);
                            mSendInviteReq.setText("Cancel Invite Request");
                            mCompanionsState = 1;

                            mDeclineInviteReq.setVisibility(View.INVISIBLE);
                            mDeclineInviteReq.setEnabled(false);
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
                            mCompanionsState = 0;
                            mSendInviteReq.setText("Send Invite Request");
                            mSendInviteReq.setVisibility(View.VISIBLE);
                            mSendInviteReq.setEnabled(true);

                            mDeclineInviteReq.setVisibility(View.INVISIBLE);
                            mDeclineInviteReq.setEnabled(false);
                        }
                    });


                }
//  <------------------------------COMPANIONS REQUEST RECEIVED STATE-------------->
                if (mCompanionsState == 2) {

                    final String currentDate = DateFormat.getDateInstance().format(new Date());
                    String noteId = mRootRef.child("Notifications").child(user_id).push().getKey();

                    HashMap<String, String> recipientNote = new HashMap<>();
                    recipientNote.put("fromUser", mCurrentUser.getUid());
                    recipientNote.put("type", "accepted");
                    recipientNote.put("task_id", task_id);
                    recipientNote.put("date", currentDate);
                    recipientNote.put("toUser", user_id);


                    HashMap<String, String> senderNote = new HashMap<>();
                    senderNote.put("toUser", user_id);
                    senderNote.put("type", "accepted");
                    senderNote.put("task_id", task_id);
                    senderNote.put("date", currentDate);
                    senderNote.put("fromUser", mCurrentUser.getUid());


                    Map companionsMap = new HashMap();
                    companionsMap.put("TaskCompanions/"+task_id+"/"+mCurrentUser.getUid(), ServerValue.TIMESTAMP);
                    companionsMap.put("TaskCompanions/" +task_id+"/"+user_id, ServerValue.TIMESTAMP);
                    companionsMap.put("TaskInviteRequests/" + mCurrentUser.getUid() + "/" + user_id + "/" + task_id, null);
                    companionsMap.put("TaskInviteRequests/" + user_id + "/" + mCurrentUser.getUid() + "/" + task_id, null);
                    companionsMap.put("Notifications/" + user_id + "/" + noteId, senderNote);
                    companionsMap.put("Notifications/" + mCurrentUser.getUid() + "/" + noteId, recipientNote);

                    mRootRef.updateChildren(companionsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                mCompanionsState = 3;

                                mSendInviteReq.setVisibility(View.VISIBLE);
                                mSendInviteReq.setEnabled(true);
                                mSendInviteReq.setText("Cancel Companion Invite");

                                mDeclineInviteReq.setVisibility(View.INVISIBLE);
                                mDeclineInviteReq.setEnabled(false);
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
                   revokeMap.put("TaskCompanions/"+task_id+"/"+mCurrentUser.getUid(),null);
                   revokeMap.put("TaskCompanions/" +task_id+"/"+user_id, null);
                    mRootRef.updateChildren(revokeMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                mCompanionsState = 0;
                                mSendInviteReq.setText("Send  Invitation Request");
                                mSendInviteReq.setVisibility(View.VISIBLE);
                                mSendInviteReq.setEnabled(true);

                                mDeclineInviteReq.setVisibility(View.INVISIBLE);
                                mDeclineInviteReq.setEnabled(false);
                            } else {
                                String error = databaseError.getMessage();
                                Toast.makeText(Profile.this, error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRootRef.child("Users").child(mCurrentUser.getUid()).child("online").setValue(ServerValue.TIMESTAMP);
    }
}
