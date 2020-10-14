package com.exo.scomm.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.exo.scomm.R;
import com.exo.scomm.data.models.User;
import com.exo.scomm.ui.activities.HomeActivity;
import com.exo.scomm.ui.activities.Profile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class AcceptedCompanionsAdapter extends RecyclerView.Adapter<AcceptedCompanionsAdapter.MyViewHolder> {

   private Set<User> companionsList;
   private Context mCtxt;
   private String taskId;

   public AcceptedCompanionsAdapter(Context taskDetails, Set<User> taskCompList, String taskId) {
      this.mCtxt = taskDetails;
      this.companionsList = taskCompList;
      this.taskId = taskId;
   }

   @NonNull
   @Override
   public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display, parent, false);
      return new MyViewHolder(itemView);
   }

   @Override
   public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
      List<User> usersList = new ArrayList<>(companionsList);
      final User user = usersList.get(position);
      holder.userName.setText(user.getUsername());
      Picasso.get().load(user.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);
      holder.itemView.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            CharSequence[] options = new CharSequence[]{"Open Profile", "Send Message", "Make Super Scommer"};
            AlertDialog.Builder builder = new AlertDialog.Builder(mCtxt);
            builder.setTitle("Select Options");
            builder.setItems(options, new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int i) {
                  if (i == 0) {
                     Intent profileIntent = new Intent(mCtxt, Profile.class);
                     profileIntent.putExtra("uid", user.getUID());
                     mCtxt.startActivity(profileIntent);
                  } else if (i == 1) {
                     Log.e("User Key", "" + user.getUID());
                     Intent intent = new Intent(mCtxt.getApplicationContext(), HomeActivity.class);
                     intent.putExtra("fromTaskDetails", "1");
                     intent.putExtra("username", user.getUsername());
                     intent.putExtra("userId", user.getUID());
                     mCtxt.startActivity(intent);
                  } else if (i == 2) {
                     FirebaseDatabase.getInstance().getReference().child("TaskSupers").child(taskId).child(user.getUID()).setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                           if (task.isSuccessful()) {
                              Toast.makeText(mCtxt.getApplicationContext(), user.getUsername()+" is now a super Schommer", Toast.LENGTH_SHORT).show();
                           }
                        }
                     });
                  }
               }
            });
            builder.show();
         }
      });

   }


   @Override
   public int getItemCount() {
      return companionsList.size();
   }

   static class MyViewHolder extends RecyclerView.ViewHolder {
      View mView;
      TextView userName, userStatus;
      CircleImageView profileImage;
      CheckBox selectCheck;

      MyViewHolder(View view) {
         super(view);
         mView = itemView;
         userName = mView.findViewById(R.id.single_user_tv_name);
         userStatus = mView.findViewById(R.id.single_user_status);
         profileImage = mView.findViewById(R.id.single_user_circle_image);
         selectCheck = mView.findViewById(R.id.select_check);
         selectCheck.setVisibility(View.INVISIBLE);
      }
   }
}
