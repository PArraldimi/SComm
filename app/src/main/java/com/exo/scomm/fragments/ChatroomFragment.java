package com.exo.scomm.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.exo.scomm.ChatActivity;
import com.exo.scomm.Profile;
import com.exo.scomm.R;
import com.exo.scomm.model.Companion;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatroomFragment extends Fragment {
    private RecyclerView mFriendsRecycler;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    public ChatroomFragment() {
        // Required empty public constructor
    }
    static ChatroomFragment newInstance(String uid) {
        ChatroomFragment fragment = new ChatroomFragment();
        Bundle args = new Bundle();
        args.putString("uid", uid);
        //args.putString("username", userName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_chatroom, container, false);
        mFriendsRecycler = view.findViewById(R.id.messages_recycler);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Companions").child(mCurrentUserId);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mFriendsRecycler.setHasFixedSize(true);
        mFriendsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }
    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Companion> options =
                new FirebaseRecyclerOptions.Builder<Companion>()
                        .setQuery(mFriendsDatabase, Companion.class)
                        .build();

        FirebaseRecyclerAdapter<Companion, ChatroomFragment.FriendsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Companion, ChatroomFragment.FriendsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatroomFragment.FriendsViewHolder holder, int position, @NonNull Companion model) {
                        holder.setDate(model.getDate());

                        final String uid = getRef(position).getKey();
                        assert uid != null;
                        mUsersDatabase.child(uid).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                final String userName = dataSnapshot.child("username").getValue().toString();
                                String image = dataSnapshot.child("image").getValue().toString();
                                if (dataSnapshot.hasChild("online")) {
                                    String userOnline =  dataSnapshot.child("online").getValue().toString();
                                    holder.setUserOnline(userOnline);
                                }
                                holder.setName(userName);
                                holder.setProfileImage(image);
                                holder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CharSequence options[] = new CharSequence[]{"Open Profile", "Send Message"};
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder.setTitle("Select Options");
                                        builder.setItems(options, new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int i) {
                                                if (i == 0) {
                                                    Intent profileIntent = new Intent(getContext(), Profile.class);
                                                    profileIntent.putExtra("uid", uid);
                                                    startActivity(profileIntent);
                                                }
                                                if (i == 1) {
                                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                                    chatIntent.putExtra("uid", uid);
                                                    chatIntent.putExtra("username", userName);
                                                    startActivity(chatIntent);
                                                }
                                            }
                                        });
                                        builder.show();
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ChatroomFragment.FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(getContext()).inflate(R.layout.users_display, parent, false);
                        return new ChatroomFragment.FriendsViewHolder(view);
                    }
                };
        mFriendsRecycler.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {
        View mView;

        FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        void setDate(String date) {
            TextView usernameView = itemView.findViewById(R.id.single_user_status);
            usernameView.setText(date);
        }

        void setName(String userName) {
            TextView user_name = mView.findViewById(R.id.single_user_tv_name);
            user_name.setText(userName);
        }

        void setProfileImage(String image) {
            CircleImageView imageView = mView.findViewById(R.id.single_user_circle_image);
            Picasso.get().load(image).placeholder(R.drawable.profile_image_placeholder).into(imageView);
        }

        void setUserOnline(String online_status) {
            ImageView userOnlineView = mView.findViewById(R.id.single_user_online);
            if (online_status.equals("true")) {
                userOnlineView.setVisibility(View.VISIBLE);
            } else {
                userOnlineView.setVisibility(View.INVISIBLE);
            }
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
