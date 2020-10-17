package com.exo.scomm.ui.fragments;

import android.content.Context;
import android.content.Intent;
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
import com.exo.scomm.data.models.Notification;
import com.exo.scomm.data.models.Task;
import com.exo.scomm.data.models.User;
import com.exo.scomm.ui.activities.Companions;
import com.exo.scomm.ui.activities.HomeActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NotificationFragment extends Fragment {
    Button clearAll;
    private RecyclerView mFriendsRecycler;
    private DatabaseReference mNotificationsRef;
    private DatabaseReference mUsersDatabase, mTaskRef;
    private FirebaseUser mCurrentUser;
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
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert mCurrentUser != null;
        mCurrentUserId = mCurrentUser.getUid();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mNotificationsRef = mRootRef.child("Notifications").child(mCurrentUserId);
        mUsersDatabase = mRootRef.child("Users");
        mTaskRef = mRootRef.child("Tasks");
        mFriendsRecycler.setHasFixedSize(true);
        mFriendsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        clearAll.setOnClickListener(view -> mNotificationsRef.setValue(null).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Notifications cleared successfully", Toast.LENGTH_SHORT).show();
            }
        }));
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Notification> options =
                new FirebaseRecyclerOptions.Builder<Notification>()
                        .setQuery(mNotificationsRef, Notification.class)
                        .build();

        FirebaseRecyclerAdapter<Notification, NotificationFragment.FriendsReqViewHolder> adapter =
                new FirebaseRecyclerAdapter<Notification, NotificationFragment.FriendsReqViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final NotificationFragment.FriendsReqViewHolder holder, int position, @NonNull final Notification model) {

                        final String noteKey = getRef(position).getKey();
                        final String req_type = model.getType();
                        final String task_id = model.getTask_id();
                        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
                        final String date = dateFormat.format(model.getDate());
                        holder.mViewSchommers.setOnClickListener(v -> {
                            Intent usersIntent = new Intent(getActivity(), Companions.class);
                            startActivity(usersIntent);
                        });

                        switch (req_type) {

                            case "invite": {
                                assert noteKey != null;
                                mUsersDatabase.child(model.getFromUser()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        final User user = dataSnapshot.getValue(User.class);
                                        mTaskRef.child(user.getId()).child(task_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChildren()) {
                                                    //No notification layout
                                                    String taskName = Objects.requireNonNull(dataSnapshot.child("title").getValue()).toString();
                                                    String taskDate = Objects.requireNonNull(dataSnapshot.child("date").getValue()).toString();
                                                    String text = user.getUsername() + " has invited you to task " + taskName + " to be scommed on " + taskDate;
                                                    holder.setText(text);
                                                    holder.setDate(date);
                                                    holder.decline.setEnabled(true);
                                                    holder.accept.setEnabled(true);
                                                    holder.chat.setEnabled(false);
                                                    holder.decline.setOnClickListener(v -> declineInvite(user.getId(), holder));
                                                    holder.accept.setOnClickListener(view -> acceptInvite(user.getId(), task_id));
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
                                break;
                            }
                            case "received": {
                                assert noteKey != null;
                                mNotificationsRef.child(noteKey).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        final String user_id = dataSnapshot.child("fromUser").getValue().toString();
                                        mRootRef.child("TaskInviteRequests").child(mCurrentUserId).child(user_id).child(task_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                if (dataSnapshot.hasChild("accepted")) {
                                                    String accepted = dataSnapshot.child("accepted").getValue().toString();
                                                    if (accepted.equals("true")) {
                                                        holder.decline.setEnabled(false);
                                                        holder.accept.setEnabled(false);
                                                        holder.chat.setEnabled(true);
                                                        holder.mViewSchommers.setEnabled(true);
                                                    } else {
                                                        holder.decline.setEnabled(true);
                                                        holder.accept.setEnabled(true);
                                                        holder.chat.setEnabled(false);
                                                        holder.mViewSchommers.setEnabled(true);
                                                    }

                                                }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                        mUsersDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                final String userName = dataSnapshot.child("username").getValue().toString();
                                                mTaskRef.child(user_id).child(task_id).addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.hasChildren()) {
                                                            final String taskName = Objects.requireNonNull(dataSnapshot.child("title").getValue()).toString();
                                                            final String taskDate = Objects.requireNonNull(dataSnapshot.child("date").getValue()).toString();
                                                            String text = userName + " has invited you to accompany them to task " + taskName + " to be scommed on " + taskDate;
                                                            holder.setText(text);
                                                            holder.setDate(date);
                                                            holder.accept.setOnClickListener(view -> {
                                                                holder.decline.setEnabled(false);
                                                                holder.accept.setEnabled(false);
                                                                holder.chat.setEnabled(true);
                                                                acceptInvite(user_id, task_id);


                                                            });
                                                            holder.chat.setOnClickListener(v -> openChat(user_id));
                                                            holder.decline.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    revokeInvite(user_id, task_id, holder);
                                                                    holder.decline.setEnabled(false);
                                                                    holder.accept.setEnabled(false);
                                                                    holder.chat.setEnabled(false);
                                                                    holder.mViewSchommers.setEnabled(false);
                                                                    holder.mViewSchommers.setText("Declined");
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
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                                break;
                            }
                            case "accepted":
                                holder.accept.setEnabled(false);
                                holder.chat.setEnabled(true);
                                assert noteKey != null;
                                mNotificationsRef.child(noteKey).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        final String user_id = dataSnapshot.child("user").getValue().toString();
                                        mUsersDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                final String userName = dataSnapshot.child("username").getValue().toString();
                                                mTaskRef.child(mCurrentUserId).child(task_id).addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        String taskName = Objects.requireNonNull(dataSnapshot.child("title").getValue()).toString();
                                                        String taskDate = Objects.requireNonNull(dataSnapshot.child("date").getValue()).toString();
                                                        if (dataSnapshot.hasChildren()) {
                                                            if (user_id.equals(mCurrentUserId)) {
                                                                holder.decline.setEnabled(false);
                                                                holder.accept.setEnabled(false);
                                                                holder.chat.setEnabled(true);
                                                                holder.mViewSchommers.setEnabled(true);
                                                                String text = userName + " accepted your invitation request to task " + taskName + " to be scommed on " + taskDate;
                                                                holder.setText(text);
                                                                holder.setDate(date);
                                                            } else {
                                                                Toast.makeText(getContext(), " Task added successfully to your schedule", Toast.LENGTH_SHORT).show();
                                                            }
                                                        } else {
                                                            Toast.makeText(getContext(), "The task " + taskName + " was deleted by owner", Toast.LENGTH_SHORT).show();
                                                        }

                                                        holder.chat.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                final HomeActivity activity = (HomeActivity) getContext();
                                                                assert activity != null;
                                                                activity.uid = user_id;
                                                                activity.username = userName;
                                                                activity.mainBottomNav.setSelectedItemId(R.id.bottom_chat_room);
                                                                activity.add_task.setVisibility(View.GONE);
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
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                break;
                            case "deleteTask":
                                assert noteKey != null;
                                mNotificationsRef.child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        final String user_id = Objects.requireNonNull(dataSnapshot.child("fromUser").getValue()).toString();
                                        mUsersDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChildren()) {
                                                    final String userName = Objects.requireNonNull(dataSnapshot.child("username").getValue()).toString();
                                                    if (user_id.equals(mCurrentUserId)) {
                                                        //No notification layout
                                                    } else {
                                                        holder.decline.setEnabled(false);
                                                        holder.accept.setEnabled(false);
                                                        holder.chat.setEnabled(false);
                                                        holder.mViewSchommers.setEnabled(false);
                                                        holder.mViewSchommers.setText("Deleted");
                                                        String text = userName + " deleted this task. ";
                                                        holder.setText(text);
                                                        holder.setDate(date);
                                                    }
                                                } else {
                                                    Toast.makeText(getContext(), "Error while performing task", Toast.LENGTH_SHORT).show();
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
                        }
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

    private void declineInvite(String user_id, @NonNull final FriendsReqViewHolder holder) {
        Map declineInviteMap = new HashMap();
        declineInviteMap.put("TaskCompanions/" + mCurrentUserId + "/" + user_id + "/" + "task_id", null);
        declineInviteMap.put("TaskCompanions/" + user_id + "/" + mCurrentUserId + "/" + "task_id", null);

        mRootRef.updateChildren(declineInviteMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    holder.decline.setEnabled(true);
                    holder.accept.setEnabled(false);
                    holder.chat.setEnabled(false);
                } else {
                    String error = databaseError.getMessage();
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void revokeInvite(String user_id, String task_id, @NonNull final FriendsReqViewHolder holder) {
        Map unfriendsMap = new HashMap();
        unfriendsMap.put("TaskInviteRequests/" + mCurrentUserId + "/" + user_id + "/" + task_id, null);
        unfriendsMap.put("TaskInviteRequests/" + user_id + "/" + mCurrentUserId + "/" + task_id, null);
        unfriendsMap.put("TaskCompanions/" + mCurrentUserId + "/" + user_id + "/" + "task_id", null);
        unfriendsMap.put("TaskCompanions/" + user_id + "/" + mCurrentUserId + "/" + "task_id", null);
        mRootRef.updateChildren(unfriendsMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Toast.makeText(requireContext(), "Request Declined Successfully", Toast.LENGTH_SHORT).show();
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

    private void openChat(String user_id) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.addToBackStack(null).add(R.id.main_container, ChatroomFragment.newInstance(user_id)).commit();
    }

    private void acceptInvite(final String user_id, final String task_id) {
        mRootRef.child("Tasks").child(user_id).child(task_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildren().equals("")) {
                    Toast.makeText(getContext(), "Unable to complete request", Toast.LENGTH_SHORT).show();
                }
                Task task = dataSnapshot.getValue(Task.class);

                final Map<String, String> taskMap = new HashMap<>();
                taskMap.put("taskOwner", task.getTaskOwner());
                taskMap.put("title", task.getTitle());
                taskMap.put("description", task.getDescription());
                taskMap.put("type", task.getType());
                taskMap.put("task_id", task_id);
                taskMap.put("date", task.getDate());

                String noteKey = mRootRef.child("Notifications").child(mCurrentUserId).push().getKey();

                Map recipientNote = new HashMap<>();
                recipientNote.put("user", mCurrentUserId);
                recipientNote.put("type", "accepted");
                recipientNote.put("task_id", task_id);
                recipientNote.put("date", ServerValue.TIMESTAMP);


                Map compMap = new HashMap<>();
                compMap.put(mCurrentUserId, ServerValue.TIMESTAMP);
                compMap.put(user_id, ServerValue.TIMESTAMP);

                Map companionsMap = new HashMap();
                companionsMap.put("TaskCompanions/" + mCurrentUserId + "/" + task_id + "/", compMap);
                companionsMap.put("TaskCompanions/" + user_id + "/" + task_id + "/", compMap);

                companionsMap.put("Notifications/" + user_id + "/" + noteKey, recipientNote);
                companionsMap.put("Tasks/" + mCurrentUserId + "/" + task_id + "/", taskMap);

                companionsMap.put("InvitedUsers/" + mCurrentUserId +  "/" + task_id + "/" + mCurrentUserId, null);
                companionsMap.put("InvitedUsers/" + user_id +  "/" + task_id + "/" + mCurrentUserId, null);
                mRootRef.updateChildren(companionsMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            Toast.makeText(getContext(), "Task added successfully to your schedule", Toast.LENGTH_SHORT).show();
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


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public static class FriendsReqViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView mDate;
        Button accept, decline, chat, mViewSchommers;

        FriendsReqViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            chat = mView.findViewById(R.id.notification_chat_btn);
            accept = mView.findViewById(R.id.notification_accept_btn);
            decline = mView.findViewById(R.id.notification_decline_btn);
            mDate = mView.findViewById(R.id.notification_date);
            mViewSchommers = mView.findViewById(R.id.notification_view_scommers);
        }

        void setText(String text) {
            TextView user_name = mView.findViewById(R.id.notification_text_message);
            user_name.setText(text);
        }

        void setDate(String text) {
            TextView date = mView.findViewById(R.id.notification_date);
            date.setText(text);
        }
    }

}
