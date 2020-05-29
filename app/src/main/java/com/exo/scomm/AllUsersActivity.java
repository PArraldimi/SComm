package com.exo.scomm;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.exo.scomm.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class AllUsersActivity extends AppCompatActivity {

    private RecyclerView FindFriendsRecyclerList;
    private DatabaseReference UsersRef;
    private String task_id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        task_id = getIntent().getStringExtra("task_id");
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        FindFriendsRecyclerList = findViewById(R.id.find_friends_recycler_list);
        FindFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(UsersRef, User.class)
                        .build();

        FirebaseRecyclerAdapter<User, FindFriendViewHolder> adapter =
                new FirebaseRecyclerAdapter<User, FindFriendViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, int position, @NonNull final User model) {
                        holder.userName.setText(model.getUsername());
                        holder.userStatus.setText(model.getStatus());
                        Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);

                        final String uid = getRef(position).getKey();
                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent profileIntent = new Intent(AllUsersActivity.this, Profile.class);
                                profileIntent.putExtra("uid", uid);
                                profileIntent.putExtra("task_id", task_id);
                                profileIntent.putExtra("username", model.getUsername());
                                startActivity(profileIntent);

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display, parent, false);
                        return new FindFriendViewHolder(view);
                    }
                };

        FindFriendsRecyclerList.setAdapter(adapter);

        adapter.startListening();
    }

    static class FindFriendViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView userName, userStatus;
        CircleImageView profileImage;

        FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            userName = mView.findViewById(R.id.single_user_tv_name);
            userStatus = mView.findViewById(R.id.single_user_status);
            profileImage = mView.findViewById(R.id.single_user_circle_image);
        }
    }
}