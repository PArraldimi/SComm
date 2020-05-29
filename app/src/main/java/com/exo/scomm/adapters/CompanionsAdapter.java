package com.exo.scomm.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.exo.scomm.R;
import com.exo.scomm.AllUsersActivity;
import com.exo.scomm.model.User;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CompanionsAdapter extends RecyclerView.Adapter<CompanionsAdapter.MyViewHolder> {
    private  Context mCtxt;
    private List<User> companionsList;
    public CompanionsAdapter(Context context, List<User> companionsList) {
        this.mCtxt = context;
        this.companionsList=companionsList;
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

        return new MyViewHolder(itemView);

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
            User task = companionsList.get(position);
            holder.username.setText(task.getUsername());
            Picasso.get().load(task.getImage()).placeholder(R.drawable.profile_image).into(holder.profile);
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
