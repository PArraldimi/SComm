package com.exo.scomm;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.exo.scomm.adapters.DataHolder;
import com.exo.scomm.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class Companions extends AppCompatActivity {

   private RecyclerView FindFriendsRecyclerList;
   private DatabaseReference TaskCompanionsRef;

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_companions);
      TaskCompanionsRef = FirebaseDatabase.getInstance().getReference().child("Users");

      FindFriendsRecyclerList = findViewById(R.id.find_friends_recycler_list);
      FindFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));


   }

   private void goBack(Set<User> selectedSet) {
      Log.e(Companions.class.getSimpleName(), ""+selectedSet.toString());
      DataHolder.setSelectedUsers(selectedSet);
      AddTaskActivity taskActivity = new AddTaskActivity();
      taskActivity.mSelectedUsers = selectedSet;
      this.finish();
   }

   @Override
   protected void onStart() {
      super.onStart();

      FirebaseRecyclerOptions<User> options =
              new FirebaseRecyclerOptions.Builder<User>()
                      .setQuery(TaskCompanionsRef, User.class)
                      .build();

      FirebaseRecyclerAdapter<User, FindFriendViewHolder> adapter =
              new FirebaseRecyclerAdapter<User, FindFriendViewHolder>(options) {
                 @Override
                 protected void onBindViewHolder(@NonNull final FindFriendViewHolder holder, int position, @NonNull final User model) {
                    final String uid = getRef(position).getKey();
                    model.setUID(uid);
                    holder.userName.setText(model.getUsername());
                    holder.userStatus.setText(model.getStatus());
                    Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);

                    holder.mView.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                          CharSequence options[] = new CharSequence[]{"Open Profile", "Send Message"};
                          AlertDialog.Builder builder = new AlertDialog.Builder(Companions.this);
                          builder.setTitle("Select Options");
                          builder.setItems(options, new DialogInterface.OnClickListener() {

                             @Override
                             public void onClick(DialogInterface dialog, int i) {
                                if (i == 0) {
                                   Intent profileIntent = new Intent(Companions.this, Profile.class);
                                   profileIntent.putExtra("uid", uid);
                                   startActivity(profileIntent);
                                }
                                if (i == 1) {
                                   Intent chatIntent = new Intent(Companions.this, HomeActivity.class);
                                   chatIntent.putExtra("uid", uid);
                                   chatIntent.putExtra("fromCompanions", "1");
                                   chatIntent.putExtra("username", model.getUsername());
                                   startActivity(chatIntent);
                                }
                             }
                          });
                          builder.show();
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
      CheckBox selectCheck;

      FindFriendViewHolder(@NonNull View itemView) {
         super(itemView);
         mView = itemView;
         userName = mView.findViewById(R.id.single_user_tv_name);
         userStatus = mView.findViewById(R.id.single_user_status);
         profileImage = mView.findViewById(R.id.single_user_circle_image);
         selectCheck = mView.findViewById(R.id.select_check);
         selectCheck.setVisibility(View.GONE);

      }
   }
}