package com.exo.scomm.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.exo.scomm.R;
import com.exo.scomm.data.models.Contact;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder> {
    private List<Contact> contactList;
    private Context Ctxt;

    public ContactsAdapter(Context context, List<Contact> contacts) {
        this.contactList = contacts;
        this.Ctxt = context;
    }


    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_item, parent, false);

        return new ContactsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        holder.name.setText(contact.getName());
        holder.phoneNumber.setText(contact.getPhoneNumber());
        if (contact.isJoined()) {
            holder.invite.setText(R.string.invite);
            holder.shareApp.setVisibility(View.GONE);
        }else {
            holder.shareApp.setText(R.string.share_app);
            holder.invite.setVisibility(View.GONE);
        }
        holder.invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.cardView.setChecked(!holder.cardView.isChecked());
            }
        });

    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {
private TextView shareApp;
        private final TextView name;
        private final TextView phoneNumber;
        MaterialCardView cardView;
        Button invite;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            shareApp =itemView.findViewById(R.id.contact_share_app);
            name = itemView.findViewById(R.id.contact_item_name);
            phoneNumber = itemView.findViewById(R.id.contact_item_phone);
            invite = itemView.findViewById(R.id.contact_item_invite);
            cardView = itemView.findViewById(R.id.contact_card);
        }
    }
}
