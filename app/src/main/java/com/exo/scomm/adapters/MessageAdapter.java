package com.exo.scomm.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.exo.scomm.R;
import com.exo.scomm.data.models.Messages;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Messages> messagesList;
    private DatabaseReference mUserdatabase;

    public MessageAdapter(List<Messages> messagesList) {
        this.messagesList = messagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        Messages messages = messagesList.get(position);
        String from_user = messages.getFrom();
        String message_type = messages.getType();
        DateFormat dateFormat = new SimpleDateFormat("hh.mm aa");
        final String dateString = dateFormat.format(messages.getTime());

        mUserdatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
        mUserdatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("username").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                holder.displayName.setText(name);
                holder.displayTime.setText(dateString);
                Picasso.get().load(image).placeholder(R.drawable.profile_image_placeholder).into(holder.profile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (message_type.equals("text")) {
            holder.messageText.setText(messages.getMessage());
            holder.messageImage.setVisibility(View.INVISIBLE);
        } else {
            holder.messageText.setVisibility(View.INVISIBLE);
            Picasso.get().load(messages.getMessage()).into(holder.messageImage);

        }


    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView displayName, displayTime;
        ImageView messageImage;
        public CircleImageView profile;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_single_text);
            profile = itemView.findViewById(R.id.message_profile_image);
            displayName = itemView.findViewById(R.id.message_single_dispaly_name);
            messageImage = itemView.findViewById(R.id.message_single_image);
            displayTime = itemView.findViewById(R.id.message_single_display_time);
        }
    }
}
