package com.exo.scomm.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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
import java.util.Objects;

public class NotificationFragment extends Fragment {
    private RecyclerView mFriendsRecycler;
    private DatabaseReference mNotificationsRef;
    private DatabaseReference mUsersDatabase, mTaskRef;
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
        mRootRef = FirebaseDatabase.getInstance().getReference();

        mNotificationsRef = mRootRef.child("Notifications").child(mCurrentUserId);
        mUsersDatabase = mRootRef.child("Users");
        mTaskRef = mRootRef.child("Tasks");

        mFriendsRecycler.setHasFixedSize(true);
        mFriendsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        clearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNotificationsRef.setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
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
                        .setQuery(mNotificationsRef, Request.class)
                        .build();

        FirebaseRecyclerAdapter<Request, NotificationFragment.FriendsReqViewHolder> adapter =
                new FirebaseRecyclerAdapter<Request, NotificationFragment.FriendsReqViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final NotificationFragment.FriendsReqViewHolder holder, int position, @NonNull final Request model) {

                        final String noteId = getRef(position).getKey();
                        assert noteId != null;
                        mNotificationsRef.child(noteId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                final String req_type = dataSnapshot.child("type").getValue().toString();

                                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh.mm aa");
                                final String dateString = dateFormat.format(model.getDate());
                                switch (req_type) {
                                    case "request_received": {
                                        holder.decline.setEnabled(true);
                                        holder.accept.setEnabled(true);
                                        holder.chat.setEnabled(false);
                                        final String from = dataSnapshot.child("from").getValue().toString();
                                        final String task_id = dataSnapshot.child("task_id").getValue().toString();

                                        mUsersDatabase.child(from).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                final String userName = dataSnapshot.child("username").getValue().toString();
                                                mTaskRef.child(from).child(task_id).addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                        String taskName = Objects.requireNonNull(dataSnapshot.child("title").getValue()).toString();
                                                        String taskDate = Objects.requireNonNull(dataSnapshot.child("date").getValue()).toString();

                                                        String text = userName + " sent you a invite request to task "+taskName+" on "+taskDate+"\n " + dateString;
                                                        holder.setText(text);


                                                        holder.accept.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                String text = userName + "sent you a friend request\n " + dateString;
                                                                holder.setText(text);
                                                                final String currentDate = DateFormat.getDateInstance().format(new Date());

                                                                mRootRef.child("Tasks").child(from).child(task_id).addValueEventListener(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                        if (dataSnapshot.getChildren().equals("")) {
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

                                                                        String recipientNoteKey = mRootRef.child("Notifications").child(from).push().getKey();
                                                                        String senderNoteKey = mRootRef.child("Notifications").child(mCurrentUserId).push().getKey();

                                                                        HashMap<String, String> recipientNote = new HashMap<>();
                                                                        recipientNote.put("from", mCurrentUserId);
                                                                        recipientNote.put("type", "request_accepted");
                                                                        recipientNote.put("task_id", task_id);

                                                                        HashMap<String, String> senderNote = new HashMap<>();
                                                                        senderNote.put("to", from);
                                                                        senderNote.put("type", "request_sent");
                                                                        senderNote.put("task_id", task_id);

                                                                        Map friendsMap = new HashMap();
                                                                        friendsMap.put("TaskCompanions/" + mCurrentUserId + "/" + from + "/date", currentDate);
                                                                        friendsMap.put("TaskCompanions/" + from + "/" + mCurrentUserId + "/date", currentDate);
                                                                        friendsMap.put("Notifications/" + from + "/" + recipientNoteKey, recipientNote);
                                                                        friendsMap.put("Notifications/" + mCurrentUserId + "/" + senderNoteKey, senderNote);
                                                                        friendsMap.put("Tasks/" + mCurrentUserId + "/" + task_id + "/", taskMap);
                                                                        friendsMap.put("TaskInviteRequests/" + mCurrentUserId + "/" + from + "/accepted", "true");
                                                                        friendsMap.put("TaskInviteRequests/" + from + "/" + mCurrentUserId + "/accepted", "true");
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
                                                                transaction.addToBackStack(null).add(R.id.main_container, ChatroomFragment.newInstance(from)).commit();

                                                            }
                                                        });
                                                        holder.decline.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                Map unfriendsMap = new HashMap();
                                                                unfriendsMap.put("TaskCompanions/" + mCurrentUserId + "/" + from, null);
                                                                unfriendsMap.put("TaskCompanions/" + from + "/" + mCurrentUserId, null);
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



                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                                        break;
                                    }
                                    case "request_sent": {
                                        holder.decline.setEnabled(true);
                                        holder.accept.setEnabled(false);
                                        holder.chat.setEnabled(false);

                                        final String toUser = dataSnapshot.child("to").getValue().toString();
                                        final String task_id = dataSnapshot.child("task_id").getValue().toString();

                                        mUsersDatabase.child(toUser).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                final String userName = dataSnapshot.child("username").getValue().toString();
                                                mTaskRef.child(mCurrentUserId).child(task_id).addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        String taskName = Objects.requireNonNull(dataSnapshot.child("title").getValue()).toString();
                                                        String taskDate = Objects.requireNonNull(dataSnapshot.child("date").getValue()).toString();

                                                        String text = "You sent "+userName+" an invite request to task "+taskName+" on "+taskDate+"\n " + dateString;
                                                        holder.setText(text);


                                                        holder.setText(text);
                                                        holder.decline.setEnabled(true);
                                                        holder.decline.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                Map unfriendsMap = new HashMap();
                                                                unfriendsMap.put("TaskCompanions/" + mCurrentUserId + "/" + toUser, null);
                                                                unfriendsMap.put("TaskCompanions/" + toUser + "/" + mCurrentUserId, null);
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

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });




                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                        break;
                                    }
                                    case "request_accepted":
                                        holder.accept.setEnabled(false);
                                        holder.chat.setEnabled(true);


                                        final String userId = dataSnapshot.child("user").getValue().toString();
                                        final String task_id = dataSnapshot.child("task_id").getValue().toString();

                                        mUsersDatabase.child(userId).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                final String userName = dataSnapshot.child("username").getValue().toString();
                                                mTaskRef.child(mCurrentUserId).child(task_id).addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        String taskName = Objects.requireNonNull(dataSnapshot.child("title").getValue()).toString();
                                                        String taskDate = Objects.requireNonNull(dataSnapshot.child("date").getValue()).toString();

                                                        Log.e("TAGG", userId);
                                                        if (userId.equals(mCurrentUserId)){
                                                            String text = userName+" accepted your invitation request to task "+taskName+" on "+taskDate+"\n " + dateString;
                                                            holder.setText(text);
                                                            holder.decline.setEnabled(true);
                                                            holder.decline.setText("Revoke");


                                                        }else {
                                                            String text = "You accepted "+userName+"'s invitation to task "+taskName+" on "+taskDate+" \n " + dateString;
                                                            holder.setText(text);
                                                            holder.decline.setEnabled(false);

                                                        }

                                                        holder.chat.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                                                FragmentTransaction transaction = fragmentManager.beginTransaction();
                                                                new ChatroomFragment();
                                                                transaction.addToBackStack(null).add(R.id.main_container, ChatroomFragment.newInstance(userId)).commit();

                                                            }
                                                        });

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

                                        break;
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
                        View view = LayoutInflater.from(getContext()).inflate(R.layout.notification_item_layout, parent, false);
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
            chat = mView.findViewById(R.id.notification_chat_btn);
            accept = mView.findViewById(R.id.notification_accept_btn);
            decline = mView.findViewById(R.id.notification_decline_btn);
        }

        void setText(String text) {
            TextView user_name = mView.findViewById(R.id.notification_text_message);
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
