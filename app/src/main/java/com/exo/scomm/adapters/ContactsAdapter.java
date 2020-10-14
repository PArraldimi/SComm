package com.exo.scomm.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.exo.scomm.R;
import com.exo.scomm.data.models.Contact;
import com.exo.scomm.ui.activities.AddTaskActivity;
import com.exo.scomm.ui.activities.AllUsersActivity;
import com.exo.scomm.ui.activities.Contacts;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder> implements Filterable {
    private List<Contact> contactList;
    private Context mCtxt;
    private List<Contact> contactListAll;
    private ContactsAdapterListener listener;
    public List<Contact> mSelectedContactsSet = new ArrayList<>();
    private Contacts mContacts;

    public ContactsAdapter(Context context, List<Contact> contacts, ContactsAdapterListener listener) {
        this.contactList = contacts;
        this.mCtxt = context;
        this.contactListAll = new ArrayList<>(contacts);
        this.listener = listener;
        this.mContacts  = new Contacts();
    }

    public class ContactsViewHolder extends RecyclerView.ViewHolder {
        private TextView shareApp;
        private final TextView name;
        private final TextView phoneNumber;
        MaterialCardView cardView;
        Button invite;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            shareApp = itemView.findViewById(R.id.contact_share_app);
            name = itemView.findViewById(R.id.contact_item_name);
            phoneNumber = itemView.findViewById(R.id.contact_item_phone);
            invite = itemView.findViewById(R.id.contact_item_invite);
            cardView = itemView.findViewById(R.id.contact_card);
            itemView.setOnClickListener(view -> {
                // send selected contact in callback
                listener.onContactSelected(contactList.get(getAdapterPosition()));
            });
        }

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
            holder.invite.setVisibility(View.VISIBLE);
            holder.shareApp.setVisibility(View.GONE);
        } else {
            holder.shareApp.setText(R.string.share_app);
            holder.invite.setVisibility(View.GONE);
        }
        holder.shareApp.setOnClickListener(v -> {
            inviteUser(contact.getPhoneNumber());
        });
        holder.cardView.setOnClickListener(v -> {
            if (holder.cardView.isChecked()) {
                holder.cardView.setChecked(false);
                mSelectedContactsSet.remove(contact);

            } else {
                holder.cardView.setChecked(true);
                mSelectedContactsSet.add(contact);
            }
            listener.onContactSelected(contact);
        });
    }

    private void inviteUser(String phoneNumber) {
        Uri sms_uri = Uri.parse("smsto:"+phoneNumber);
        Intent sms_intent = new Intent(Intent.ACTION_SENDTO, sms_uri);
        sms_intent.putExtra("sms_body", "This Will be link pto the app");
        mCtxt.startActivity(sms_intent);
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                List<Contact> filteredList = new ArrayList<>();
                if (charSequence.toString().isEmpty()) {
                    filteredList.addAll(contactListAll);
                } else {
                    for (Contact row : contactListAll) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getName().toLowerCase().contains(charSequence.toString().toLowerCase()) || row.getPhoneNumber().contains(charSequence.toString())) {
                            filteredList.add(row);
                        }
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                contactList.clear();
                contactList.addAll((Collection<? extends Contact>) filterResults.values);
                notifyDataSetChanged();
            }
        };
    }

    public interface ContactsAdapterListener {
        void onContactSelected(Contact contact);
    }
}



