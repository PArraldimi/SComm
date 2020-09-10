package com.exo.scomm.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.exo.scomm.R;
import com.exo.scomm.adapters.MessageAdapter;
import com.exo.scomm.data.models.GetTimeAgo;
import com.exo.scomm.data.models.Messages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {
    private static final int GALLERY_PICK = 1;
    private FirebaseAuth mAuth;
    TextView tvUserName, lastSeen;
    private Toolbar mToolbar;
    private DatabaseReference mRootRef;
    private String userName,mCurrentUserId,mChatUser;
    ImageButton chatAdd, sendChat;
    EditText chatMessage;
    RecyclerView mMessagesRecycler;
    private SwipeRefreshLayout refreshLayout;
    LinearLayoutManager mLinearLayout;
    private MessageAdapter adapter;
    private List<Messages> messagesList = new ArrayList<>();
    private static final int TOTAL_ITEMS_LOAD = 10;
    private int mCurrentPage = 1;
    private String mLastKey = "";
    private String mPrevKey = "";
    private int itemPos = 0;
    private StorageReference mImageStorage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mToolbar = findViewById(R.id.chat_app_bar);
        setSupportActionBar(mToolbar);


        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        mChatUser = getIntent().getStringExtra("uid");
        userName = getIntent().getStringExtra("username");
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mImageStorage = FirebaseStorage.getInstance().getReference();


        lastSeen = findViewById(R.id.custom_title_bar_last_seen);
        tvUserName = findViewById(R.id.custom_title_bar_user_name);
        sendChat = findViewById(R.id.chat_send_btn);
        chatMessage = findViewById(R.id.chat_message_view);
        mMessagesRecycler = findViewById(R.id.messages_recycler);
        refreshLayout = findViewById(R.id.swipe_layout_recycler);


        mLinearLayout = new LinearLayoutManager(this);
        mMessagesRecycler.setHasFixedSize(true);
        adapter = new MessageAdapter(messagesList);

        mMessagesRecycler.setLayoutManager(mLinearLayout);
        mMessagesRecycler.setAdapter(adapter);

        loadMessages();
        tvUserName.setText(userName);

        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                if (online.equals("true")) {
                    lastSeen.setText("Online");
                } else {
                    GetTimeAgo timeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(online);
                    String lastSeenTime = timeAgo.getTimeAgo(lastTime, getApplicationContext());
                    lastSeen.setText(lastSeenTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chats").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(mChatUser)) {
                    Map chatMap = new HashMap();
                    chatMap.put("seen ", false);
                    chatMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUser, chatMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserId, chatMap);
                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Log.e("CHAT_LOG", databaseError.getMessage().toString());
                            }
                        }
                    });


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });


        sendChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                itemPos = 0;
                loadMoreMessages();
            }
        });

    }
    private void loadMoreMessages() {
        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);
        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();
                messagesList.add(itemPos++, message);

                if (mPrevKey.equals(messageKey)) {
                    messagesList.add(itemPos++, message);
                } else {
                    mPrevKey = mLastKey;
                }
                if (itemPos == 1) {
                    mLastKey = messageKey;
                }


                adapter.notifyDataSetChanged();
                refreshLayout.setRefreshing(false);
                mLinearLayout.scrollToPositionWithOffset(10, 0);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadMessages() {
        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_LOAD);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                itemPos++;

                if (itemPos == 1) {
                    String messageKey = dataSnapshot.getKey();
                    mLastKey = messageKey;
                    mPrevKey = messageKey;
                }

                messagesList.add(message);
                adapter.notifyDataSetChanged();
                mMessagesRecycler.scrollToPosition(messagesList.size() - 1);
                refreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {
        String message = chatMessage.getText().toString();
        if (!TextUtils.isEmpty(message)) {
            String currentUserRef = "messages/" + mCurrentUserId + "/" + mChatUser;
            String chatUserRef = "messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference userMsgPush = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).push();
            String push_id = userMsgPush.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);


            Map userMessageMap = new HashMap();
            userMessageMap.put(currentUserRef + "/" + push_id, messageMap);
            userMessageMap.put(chatUserRef + "/" + push_id, messageMap);

            chatMessage.setText("");
            mRootRef.updateChildren(userMessageMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.e("CHAT_LOG", databaseError.getMessage().toString());
                    }
                }
            });

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            sendToStart();
        } else {
            mRootRef.child("Users").child(mChatUser).child("online").setValue("true");

        }
    }

    private void sendToStart() {
        startActivity(new Intent(ChatActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuth.getCurrentUser() != null) {
            mRootRef.child("Users").child(mChatUser).child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            final Uri imageUri = data.getData();
            final String currentUserRef = "messages/" + mCurrentUserId + "/" + mChatUser;
            final String chatUserRef = "messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).push();
            final String push_id = user_message_push.getKey();

            final StorageReference filePath = mImageStorage.child("message_images").child(push_id + ".jpg");
            filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {

                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadUrl = uri.toString();
                                Map messageMap = new HashMap();
                                messageMap.put("message", downloadUrl);
                                messageMap.put("seen", false);
                                messageMap.put("type", "image");
                                messageMap.put("time", ServerValue.TIMESTAMP);
                                messageMap.put("from", mCurrentUserId);


                                Map userMessageMap = new HashMap();
                                userMessageMap.put(currentUserRef + "/" + push_id, messageMap);
                                userMessageMap.put(chatUserRef + "/" + push_id, messageMap);

                                chatMessage.setText("");
                                mRootRef.updateChildren(userMessageMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                        if (databaseError != null) {
                                            Log.e("CHAT_LOG", databaseError.getMessage().toString());
                                        }
                                    }
                                });
                            }

                        });
                    }
                }
            });

        }
    }
}
