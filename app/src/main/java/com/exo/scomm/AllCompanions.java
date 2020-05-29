package com.exo.scomm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.exo.scomm.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class AllCompanions extends AppCompatActivity {
    private RecyclerView companionsRecyclerView;
    private DatabaseReference companionsRef;
    private String task_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_companions);
        task_id = getIntent().getStringExtra("task_id");
        companionsRef = FirebaseDatabase.getInstance().getReference().child("Companions");

        companionsRecyclerView = findViewById(R.id.companions_recycler_view);
        companionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(companionsRef, User.class)
                        .build();

        FirebaseRecyclerAdapter<User, AllUsersActivity.FindFriendViewHolder> adapter =
                new FirebaseRecyclerAdapter<User, AllUsersActivity.FindFriendViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull AllUsersActivity.FindFriendViewHolder holder, int position, @NonNull final User model) {
                        holder.userName.setText(model.getUsername());
                        holder.userStatus.setText(model.getStatus());
                        Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);

                        final String uid = getRef(position).getKey();
                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent profileIntent = new Intent(AllCompanions.this, Profile.class);
                                profileIntent.putExtra("uid", uid);
                                profileIntent.putExtra("task_id", task_id);
                                profileIntent.putExtra("username", model.getUsername());
                                startActivity(profileIntent);

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public AllUsersActivity.FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display, parent, false);
                        return new AllUsersActivity.FindFriendViewHolder(view);
                    }
                };

        companionsRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }
}
