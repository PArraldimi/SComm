package com.exo.scomm.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.exo.scomm.R;
import com.exo.scomm.data.models.User;
import com.exo.scomm.ui.activities.HomeActivity;
import com.exo.scomm.ui.activities.MessageActivity;
import com.exo.scomm.ui.activities.Profile;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class PendingCompanionsAdapter extends RecyclerView.Adapter<PendingCompanionsAdapter.MyViewHolder> {

   private final Set<User> companionsList;
   private final Context mCtxt;
   private final String taskId;

   public PendingCompanionsAdapter(Context taskDetails, Set<User> taskCompList, String taskId) {
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
      Log.e("TAG", "Phone" + user.getPhone() +"Name "+user.getUsername());

      holder.userName.setText(user.getUsername());
      holder.phoneNo.setText(user.getPhone());
      Picasso.get().load(user.getImage()).placeholder(R.drawable.scomm_user_placeholder_white).into(holder.profileImage);
      holder.itemView.setOnClickListener(v -> {
         CharSequence[] options = new CharSequence[]{"Send Message"};
         AlertDialog.Builder builder = new AlertDialog.Builder(mCtxt);
         builder.setTitle("Select Options");
         builder.setItems(options, (dialog, i) -> {
             if (i == 0) {
               Log.e("User Key", "" + user.getId());
               Intent intent = new Intent(mCtxt, MessageActivity.class);
               intent.putExtra("fromTaskDetails", "1");
               intent.putExtra("username", user.getUsername());
               intent.putExtra("user_id", user.getId());
               mCtxt.startActivity(intent);
            }
         });
         builder.show();
      });

   }


   @Override
   public int getItemCount() {
      return companionsList.size();
   }

   static class MyViewHolder extends RecyclerView.ViewHolder {
      View mView;
      TextView userName, phoneNo;
      CircleImageView profileImage;
      CheckBox selectCheck;


      MyViewHolder(View view) {
         super(view);
         mView = itemView;
         userName = mView.findViewById(R.id.single_user_tv_name);
         phoneNo = mView.findViewById(R.id.single_user_phone);
         profileImage = mView.findViewById(R.id.single_user_circle_image);
         selectCheck = mView.findViewById(R.id.select_check);
         selectCheck.setVisibility(View.INVISIBLE);
      }
   }
}
