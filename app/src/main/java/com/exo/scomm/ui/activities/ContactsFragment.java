package com.exo.scomm.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.exo.scomm.R;
import com.exo.scomm.adapters.ContactsAdapter;
import com.exo.scomm.data.models.Contact;
import com.exo.scomm.data.models.User;
import com.exo.scomm.utils.DataViewModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.List;

public class ContactsFragment extends DialogFragment {
    private DatabaseReference mRootRef;
    private LinearLayoutManager layoutManager;

    public ContactsFragment() {
    }

    public static ContactsFragment newInstance(List<Contact> contacts) {
        ContactsFragment fragment = new ContactsFragment();
        Bundle args = new Bundle();
        args.putSerializable("contacts", (Serializable) contacts);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contacts, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        assert getArguments() != null;
        final List<Contact> contacts = (List<Contact>) getArguments().getSerializable("contacts");
        final RecyclerView recyclerView = view.findViewById(R.id.contacts_recycler);
        mRootRef = FirebaseDatabase.getInstance().getReference();
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);
        ContactsAdapter contactsAdapter =new ContactsAdapter(getContext(),contacts);
        recyclerView.setAdapter(contactsAdapter);
        DataViewModel  model =new ViewModelProvider(this).get(DataViewModel.class);
        model.getAllUsers().observe(getViewLifecycleOwner(), new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                for (int i = 0; i < users.size(); i++) {
                    User user = users.get(i);
                    for (Contact c :
                            contacts) {
                        if (user.getPhone().equals(c.phoneNumber)) {
                            Log.e("User with app ", user.getPhone());
                            c.setJoined(true);
                        }
                    }
                }

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
