package com.exo.scomm.adapters;

import android.app.AlertDialog;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.exo.scomm.R;
import com.exo.scomm.data.models.User;
import com.exo.scomm.ui.activities.HomeActivity;
import com.exo.scomm.ui.activities.Profile;
import com.exo.scomm.ui.activities.TaskDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class CompanionsTasksAdapter extends RecyclerView.Adapter<CompanionsTasksAdapter.MyViewHolder> {
   private Set<User> companionsList;
   private TaskDetails mCtxt;
   private com.exo.scomm.data.models.Task task;
   private String taskType;

   public CompanionsTasksAdapter(TaskDetails taskDetails, Set<User> taskCompList, String task_id, com.exo.scomm.data.models.Task taskType) {
      this.mCtxt = taskDetails;
      this.companionsList = taskCompList;
      this.task = taskType;
   }

   @NonNull
   @Override
   public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.companion_item, parent, false);
      return new CompanionsTasksAdapter.MyViewHolder(itemView);
   }

   @Override
   public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
      List<User> usersList = new ArrayList<>(companionsList);
      final User user = usersList.get(position);
      holder.username.setText(user.getUsername());
      Picasso.get().load(user.getImage()).placeholder(R.drawable.profile_image).into(holder.profile);
      holder.itemView.setOnClickListener(v -> {
         if (task.getType().equals("Public")) {
            if (!task.getTaskOwner().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

               CharSequence[] options = new CharSequence[]{"Open Profile", "Send Message", "Make Super Scommer"};
               AlertDialog.Builder builder = new AlertDialog.Builder(mCtxt);
               builder.setTitle("Select Options");
               builder.setItems(options, (dialog, i) -> {
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
                     FirebaseDatabase.getInstance().getReference().child("TaskSupers").child(task.getTask_id()).child(user.getUID()).setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                           if (task.isSuccessful()) {
                              Toast.makeText(mCtxt.getApplicationContext(), "User is now a super Schommer", Toast.LENGTH_SHORT).show();
                           }
                        }
                     });
                  }
               });
               builder.show();
            }
         }
      });

   }


   @Override
   public int getItemCount() {
      return companionsList.size();
   }

   public static class MyViewHolder extends RecyclerView.ViewHolder {
      public TextView username;
      CircleImageView profile;
      Button button;

      MyViewHolder(View view) {
         super(view);
         profile = (CircleImageView) view.findViewById(R.id.companion_profile_image);
         username = (TextView) view.findViewById(R.id.companion_user_name);
         button = (Button) view.findViewById(R.id.companion_button);
      }
   }
}
