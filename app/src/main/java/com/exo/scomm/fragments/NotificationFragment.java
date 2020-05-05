package com.exo.scomm.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.exo.scomm.R;
import com.exo.scomm.model.Request;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NotificationFragment extends Fragment {
    private RecyclerView mFriendsRecycler;
    private DatabaseReference mReqDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    Button clearAll;
    private String mCurrentUserId;
    private DatabaseReference mRootRef;

    public NotificationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View mMainView = inflater.inflate(R.layout.fragment_notification, container, false);
        mFriendsRecycler = mMainView.findViewById(R.id.notification_recycler);
        clearAll = mMainView.findViewById(R.id.action_clear_all);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mReqDatabase = FirebaseDatabase.getInstance().getReference().child("Invite_Requests").child(mCurrentUserId);
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mFriendsRecycler.setHasFixedSize(true);
        mFriendsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        clearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mReqDatabase.setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(getContext(), "Notifications cleared successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Request> options =
                new FirebaseRecyclerOptions.Builder<Request>()
                        .setQuery(mReqDatabase, Request.class)
                        .build();

        FirebaseRecyclerAdapter<Request, NotificationFragment.FriendsReqViewHolder> adapter =
                new FirebaseRecyclerAdapter<Request, NotificationFragment.FriendsReqViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final NotificationFragment.FriendsReqViewHolder holder, int position, @NonNull final Request model) {

                        final String uid = getRef(position).getKey();
                        assert uid != null;

                        mReqDatabase.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                final String req_type = dataSnapshot.child("request_type").getValue().toString();
                                final String accepted = dataSnapshot.child("accepted").getValue().toString();
                                final String task_id = dataSnapshot.child("task_id").getValue().toString();

                                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh.mm aa");
                                final String dateString = dateFormat.format(model.getDate());

                                if (req_type.equals("received")) {
                                    mUsersDatabase.child(uid).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            final String userName = dataSnapshot.child("username").getValue().toString();
                                            String text = userName + " sent you a friend request\n " + dateString;
                                            holder.setText(text);
                                            if (accepted.equals("true")) {
                                                holder.setText(text);
                                                holder.decline.setEnabled(false);
                                                holder.accept.setEnabled(false);
                                                holder.chat.setEnabled(true);
                                            } else {
                                                holder.setText(text);
                                                holder.chat.setEnabled(false);
                                            }

                                            holder.accept.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    String text = userName + "sent you a friend request\n " + dateString;
                                                    holder.setText(text);
                                                    final String currentDate = DateFormat.getDateInstance().format(new Date());

                                                    mRootRef.child("Tasks").child(uid).child(task_id).addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            if (dataSnapshot.getChildren().equals("")){
                                                                Toast.makeText(getContext(), "Unable to complete request", Toast.LENGTH_SHORT).show();
                                                            }

                                                            String title = dataSnapshot.child("title").getValue().toString();
                                                            String description = dataSnapshot.child("description").getValue().toString();
                                                            String type = dataSnapshot.child("type").getValue().toString();
                                                            String date = dataSnapshot.child("date").getValue().toString();
                                                            String taskOwn = dataSnapshot.child("taskOwner").getValue().toString();

                                                            final Map<String, String> taskMap = new HashMap<>();
                                                            taskMap.put("taskOwner", taskOwn);
                                                            taskMap.put("title", title);
                                                            taskMap.put("description", description);
                                                            taskMap.put("type", type);
                                                            taskMap.put("date", date);

                                                            Map taskCompMap = new HashMap();
                                                            taskCompMap.put("taskOwner", taskOwn);
                                                            taskCompMap.put("task_id", task_id);
                                                            taskCompMap.put("date", currentDate);

                                                            DatabaseReference newNotificationRef = mRootRef.child("notifications").child(mCurrentUserId).push();
                                                            String newNotificationId = newNotificationRef.getKey();

                                                            HashMap<String, String> notificationData = new HashMap<>();
                                                            notificationData.put("from", mCurrentUserId);
                                                            notificationData.put("type", "request_accepted");
                                                            notificationData.put("task_id", task_id);
                                                            notificationData.put("to_user", uid);

                                                            Map friendsMap = new HashMap();
                                                            friendsMap.put("Companions/" + mCurrentUserId + "/" + uid + "/date", currentDate);
                                                            friendsMap.put("Companions/" + uid + "/" + mCurrentUserId + "/date", currentDate);
                                                            friendsMap.put("task_companions/" + mCurrentUserId + "/" + task_id + "/", taskCompMap);
                                                            friendsMap.put("task_companions/" + uid + "/" + task_id + "/", taskCompMap);
                                                            friendsMap.put("notifications/" + uid + "/" + newNotificationId, notificationData);
                                                            friendsMap.put("Tasks/" + mCurrentUserId + "/" + task_id + "/", taskMap);
                                                            friendsMap.put("Invite_Requests/" + mCurrentUserId + "/" + uid + "/accepted", "true");
                                                            friendsMap.put("Invite_Requests/" + uid + "/" + mCurrentUserId + "/accepted", "true");
                                                            mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                                                                @Override
                                                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                                    if (databaseError == null) {

                                                                        holder.decline.setEnabled(false);
                                                                        holder.accept.setEnabled(false);
                                                                        holder.chat.setEnabled(true);
                                                                    } else {
                                                                        String error = databaseError.getMessage();
                                                                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }

                                                            });

                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });
                                                }
                                            });
                                            holder.chat.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                                                    new ChatroomFragment();
                                                    transaction.addToBackStack(null).add(R.id.main_container, ChatroomFragment.newInstance(uid)).commit();

                                                }
                                            });
                                            holder.decline.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Map unfriendsMap = new HashMap();
                                                    unfriendsMap.put("Companions/" + mCurrentUserId + "/" + uid, null);
                                                    unfriendsMap.put("Companions/" + uid + "/" + mCurrentUserId, null);
                                                    mRootRef.updateChildren(unfriendsMap, new DatabaseReference.CompletionListener() {
                                                        @Override
                                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                            if (databaseError == null) {
                                                                holder.decline.setEnabled(false);
                                                                holder.accept.setEnabled(false);
                                                                holder.chat.setEnabled(false);
                                                            } else {
                                                                String error = databaseError.getMessage();
                                                                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                } else if (req_type.equals("sent")) {

                                    mUsersDatabase.child(uid).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            final String userName = dataSnapshot.child("username").getValue().toString();

                                            if (accepted.equals("true")) {
                                                String text = "You sent you a friend request to " + userName + "\n " + dateString;
                                                holder.setText(text);
                                                holder.decline.setEnabled(false);
                                                holder.accept.setEnabled(false);
                                                holder.chat.setEnabled(true);
                                            } else {
                                                String text = "You sent you a friend request to " + userName + "\n " + dateString;
                                                holder.setText(text);
                                                holder.decline.setEnabled(true);
                                                holder.decline.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        Map unfriendsMap = new HashMap();
                                                        unfriendsMap.put("Companions/" + mCurrentUserId + "/" + uid, null);
                                                        unfriendsMap.put("Companions/" + uid + "/" + mCurrentUserId, null);
                                                        mRootRef.updateChildren(unfriendsMap, new DatabaseReference.CompletionListener() {
                                                            @Override
                                                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                                if (databaseError == null) {
                                                                    holder.decline.setEnabled(false);
                                                                    holder.accept.setEnabled(false);
                                                                    holder.chat.setEnabled(false);
                                                                } else {
                                                                    String error = databaseError.getMessage();
                                                                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                                                                }

                                                            }
                                                        });
                                                    }
                                                });
                                                holder.accept.setEnabled(false);
                                                holder.chat.setEnabled(false);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public NotificationFragment.FriendsReqViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(getContext()).inflate(R.layout.request_item_layout, parent, false);
                        return new NotificationFragment.FriendsReqViewHolder(view);
                    }
                };
        mFriendsRecycler.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FriendsReqViewHolder extends RecyclerView.ViewHolder {
        View mView;
        Button accept, decline, chat;

        FriendsReqViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            chat = mView.findViewById(R.id.request_chat_btn);
            accept = mView.findViewById(R.id.request_accept_btn);
            decline = mView.findViewById(R.id.request_decline_btn);
        }

        void setText(String text) {
            TextView user_name = mView.findViewById(R.id.request_text_message);
            user_name.setText(text);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
