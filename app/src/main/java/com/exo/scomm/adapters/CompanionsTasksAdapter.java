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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.exo.scomm.HomeActivity;
import com.exo.scomm.Profile;
import com.exo.scomm.R;
import com.exo.scomm.AllUsersActivity;
import com.exo.scomm.TaskDetails;
import com.exo.scomm.fragments.ChatroomFragment;
import com.exo.scomm.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CompanionsTasksAdapter extends RecyclerView.Adapter<CompanionsTasksAdapter.MyViewHolder> {
    private List<User> companionsList;
    private TaskDetails mCtxt;
    String taskId;
    public CompanionsTasksAdapter(TaskDetails taskDetails, List<User> taskCompList, String taskId) {
        this.mCtxt = taskDetails;
        this.companionsList = taskCompList;
        this.taskId = taskId;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;

        if(viewType == R.layout.companion_item){
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.companion_item, parent, false);
        }
        else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.button, parent, false);
        }
        return new CompanionsTasksAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        if(position == companionsList.size()) {
            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCtxt.startActivity(new Intent(mCtxt.getApplicationContext(), AllUsersActivity.class));
                }
            });
        }else {
            final User user = companionsList.get(position);
            holder.username.setText(user.getUsername());
            Picasso.get().load(user.getImage()).placeholder(R.drawable.profile_image).into(holder.profile);
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
                            }else if (i == 1) {
                                Log.e("User Key", ""+user.getUID());
                                Intent intent = new Intent(mCtxt.getApplicationContext(), HomeActivity.class);
                                intent.putExtra("fromTaskDetails", "1");
                                intent.putExtra("username", user.getUsername());
                                intent.putExtra("userId", user.getUID());
                                mCtxt.startActivity(intent);
                            }else if (i == 2){
                                FirebaseDatabase.getInstance().getReference().child("TaskSupers").child(taskId).child(user.getUID()).setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                       if (task.isSuccessful()){
                                           Toast.makeText(mCtxt.getApplicationContext(), "User is now a super Schommer", Toast.LENGTH_SHORT).show();
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
    }
    @Override
    public int getItemViewType(int position) {
        return (position == companionsList.size()) ? R.layout.button : R.layout.companion_item;
    }

    @Override
    public int getItemCount() {
        return companionsList.size()+1;
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
