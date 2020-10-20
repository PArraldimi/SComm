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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;


public class AcceptedCompanionsAdapter extends RecyclerView.Adapter<AcceptedCompanionsAdapter.MyViewHolder> {

    private final Set<User> companionsList;
    private final Context mCtxt;
    private com.exo.scomm.data.models.Task mTask;
    private Set<String> superScommersKey = new HashSet<>();

    public AcceptedCompanionsAdapter(Context taskDetails, Set<User> taskCompList, com.exo.scomm.data.models.Task task) {
        this.mCtxt = taskDetails;
        this.companionsList = taskCompList;
        this.mTask = task;
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
        holder.phoneNo.setText(user.getPhone());
        Picasso.get().load(user.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);
        holder.itemView.setOnClickListener(v -> {
            getSuperScommers(mTask.getTask_id());
            String owner = mTask.getTaskOwner();
            if (!user.getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                CharSequence[] options = new CharSequence[]{"Send Message", "Make Super Scommer"};

                if (user.getId().equals(owner)) {
                }
                if (!owner.equals(user.getId()) && superScommersKey.contains(user.getId())) {

                }


//            if (user.getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
//               options[0]= ""
//            }
                AlertDialog.Builder builder = new AlertDialog.Builder(mCtxt);
                builder.setTitle("Select Options");
                builder.setItems(options, (dialog, i) -> {
                    if (i == 0) {
                        Log.e("User Key", "" + user.getUID());
                        Intent intent = new Intent(mCtxt.getApplicationContext(), HomeActivity.class);
                        intent.putExtra("fromTaskDetails", "1");
                        intent.putExtra("username", user.getUsername());
                        intent.putExtra("userId", user.getUID());
                        mCtxt.startActivity(intent);
                    } else if (i == 1) {
                        FirebaseDatabase.getInstance().getReference().child("TaskSupers").child(mTask.getTask_id()).child(user.getUID()).setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(mCtxt.getApplicationContext(), user.getUsername() + " is now a super Schommer", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
                builder.show();
            }

        });
    }

    private void getSuperScommers(String taskId) {
        FirebaseDatabase.getInstance().getReference().child("TaskSupers").child(taskId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot childSnapshot : snapshot.getChildren()
                    ) {
                        superScommersKey.add(childSnapshot.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
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
