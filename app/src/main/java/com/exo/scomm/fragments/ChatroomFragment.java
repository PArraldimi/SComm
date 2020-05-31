package com.exo.scomm.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.exo.scomm.HomeActivity;
import com.exo.scomm.R;
import com.exo.scomm.adapters.MessageAdapter;
import com.exo.scomm.model.GetTimeAgo;
import com.exo.scomm.model.Messages;
import com.exo.scomm.model.User;
import com.exo.scomm.model.Utils;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatroomFragment extends Fragment {
  private RecyclerView mMessagesRecycler;
  private DatabaseReference mFriendsDatabase;
  private DatabaseReference mUsersDatabase, mRootRef;
  private FirebaseAuth mAuth;
  private String mCurrentUserId;
  LinearLayoutManager mLinearLayout;
  private MessageAdapter adapter;
  private List<Messages> messagesList = new ArrayList<>();
  HomeActivity homeActivity;
  private ProgressDialog mProgressDialog;
  private CircleImageView mProfile;
  TextView mUsername, mLastSeen;
  private String mUserId;
  private SwipeRefreshLayout mRefreshLayout;
  ImageButton mSendChat;
  EditText mChatMessage;
  private static final int TOTAL_ITEMS_LOAD = 10;
  private int mCurrentPage = 1;
  private String mLastKey = "";
  private int itemPos = 0;
  private String mPrevKey = "";
  String mChatUser;

  public ChatroomFragment() {
    // Required empty public constructor
  }

  public static ChatroomFragment newInstance(String uid) {
    ChatroomFragment fragment = new ChatroomFragment();
    Bundle args = new Bundle();
    args.putString("uid", uid);
    //args.putString("username", userName);
    fragment.setArguments(args);
    return fragment;
  }

  public static ChatroomFragment newInstance(String uid, String username) {
    ChatroomFragment fragment = new ChatroomFragment();
    Bundle args = new Bundle();
    args.putString("uid", uid);
    args.putString("username", username);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      mUserId = getArguments().getString("uid");
      Log.e("TAG", "" + mUserId);
    }

  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_chatroom, container, false);


    mMessagesRecycler = view.findViewById(R.id.messages_recycler);
    mAuth = FirebaseAuth.getInstance();
    mUsername = view.findViewById(R.id.custom_title_bar_user_name);
    mSendChat = view.findViewById(R.id.chat_send_btn);
    mChatMessage = view.findViewById(R.id.chat_message_view);
    mRefreshLayout = view.findViewById(R.id.swipe_layout_recycler);
    mProfile = view.findViewById(R.id.chat_profile_image);
    homeActivity = (HomeActivity) requireActivity();
    mCurrentUserId = mAuth.getCurrentUser().getUid();
    mRootRef = FirebaseDatabase.getInstance().getReference();
    mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Companions").child(mCurrentUserId);
    mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

      mLinearLayout = new LinearLayoutManager(requireContext());
      mMessagesRecycler.setHasFixedSize(true);
      adapter = new MessageAdapter(messagesList);

      mMessagesRecycler.setLayoutManager(mLinearLayout);
      mMessagesRecycler.setAdapter(adapter);

      User user = Utils.retrieveUser(requireContext());
      mChatUser = user.getUID();
      loadUser(mChatUser);

    loadMessages();

    mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        String online = dataSnapshot.child("online").getValue().toString();
        // String image = dataSnapshot.child("image").getValue().toString();
        if (online.equals("true")) {
          //lastSeen.setText("Online");
        } else {
          GetTimeAgo timeAgo = new GetTimeAgo();
          long lastTime = Long.parseLong(online);
          String lastSeenTime = GetTimeAgo.getTimeAgo(lastTime, getContext());
          //lastSeen.setText(lastSeenTime);
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
          chatUserMap.put("Chats/" + mCurrentUserId + "/" + mChatUser, chatMap);
          chatUserMap.put("Chats/" + mChatUser + "/" + mCurrentUserId, chatMap);
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

    mSendChat.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        sendMessage();
      }
    });
    mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        mCurrentPage++;
        itemPos = 0;
        loadMoreMessages();
      }
    });


    return view;
  }

  private void loadMoreMessages() {
    DatabaseReference messageRef = mRootRef.child("Messages").child(mCurrentUserId).child(mChatUser);
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
        mRefreshLayout.setRefreshing(false);
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

    DatabaseReference messageRef = mRootRef.child("Messages").child(mCurrentUserId).child(mChatUser);

    Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_LOAD);
    messageQuery.addChildEventListener(new ChildEventListener() {
      @Override
      public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        Messages message = dataSnapshot.getValue(Messages.class);
        Log.e("Message", ""+message.getMessage());
        itemPos++;

        if (itemPos == 1) {
          String messageKey = dataSnapshot.getKey();
          mLastKey = messageKey;
          mPrevKey = messageKey;
        }

        messagesList.add(message);
        adapter.notifyDataSetChanged();
        mMessagesRecycler.scrollToPosition(messagesList.size() - 1);
        mRefreshLayout.setRefreshing(false);
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
    String message = mChatMessage.getText().toString();
    if (!TextUtils.isEmpty(message)) {
      String currentUserRef = "Messages/" + mCurrentUserId + "/" + mChatUser;
      String chatUserRef = "Messages/" + mChatUser + "/" + mCurrentUserId;

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

      mChatMessage.setText("");
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

  private void loadUser(String userId) {
    final HomeActivity activity = (HomeActivity) getContext();

    mProgressDialog = new ProgressDialog(requireContext());
    mProgressDialog.setTitle("Loading User data");
    mProgressDialog.setCanceledOnTouchOutside(false);
    mProgressDialog.setMessage("Loading Please wait!!");
    mProgressDialog.show();
    assert activity != null;
    mUsersDatabase.child(userId).addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        mProgressDialog.dismiss();
        String name = Objects.requireNonNull(dataSnapshot.child("username").getValue()).toString();
        String status = Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();
        String image = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();
        mUsername.setText(name);
        if (!image.equals("")) {
          Picasso.get().load(image).placeholder(R.drawable.profile_image).into(mProfile);
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {

      }
    });
  }

  @Override
  public void onStart() {
    super.onStart();
      FirebaseUser currentUser = mAuth.getCurrentUser();
      if (currentUser == null) {

      } else {
          mRootRef.child("Users").child(mChatUser).child("online").setValue("true");

      }
  }


  @Override
  public void onResume() {
    super.onResume();
    User user = Utils.retrieveUser(requireContext());
    Log.e("TAG Resume", "" + user.getUsername() + " " + user.getUID());

  }
    @Override
    public void onStop() {
        super.onStop();
        if (mAuth.getCurrentUser() != null) {
            mRootRef.child("Users").child(mChatUser).child("online").setValue(ServerValue.TIMESTAMP);
        }
    }


  @Override
  public void onDetach() {
    super.onDetach();
  }
}
