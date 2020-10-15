package com.exo.scomm.ui.activities;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.exo.scomm.R;
import com.exo.scomm.adapters.ContactsAdapter;
import com.exo.scomm.data.models.Contact;
import com.exo.scomm.data.models.User;
import com.exo.scomm.utils.DataViewModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends DialogFragment implements ContactsAdapter.ContactsAdapterListener {
    private ContactsAdapter contactsAdapter;
    private List<Contact> joinedContacts = new ArrayList<>();
    private RecyclerView recyclerView;

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
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Add this! (as above)
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        Toolbar toolbar = view.findViewById(R.id.my_toolbar);
        toolbar.setTitle("Contacts");
        toolbar.inflateMenu(R.menu.menu_search);

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        assert getArguments() != null;
        final List<Contact> contacts = (List<Contact>) getArguments().getSerializable("contacts");
        recyclerView = view.findViewById(R.id.contacts_recycler);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);
        DataViewModel model = new ViewModelProvider(this).get(DataViewModel.class);
        model.getAllUsers().observe(getViewLifecycleOwner(), users -> populateRecycler(users, contacts));
    }

    private void populateRecycler(List<User> users, List<Contact> contacts) {
        joinedContacts = new ArrayList<>();
        List<Contact> otherContacts = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            String userPhone = new StringBuilder(user.getPhone()).reverse().toString();
            Log.e("TAG", "reversed" + userPhone);
            Log.e("TAG", "userphonetrimmed" + userPhone.substring(8));

            for (Contact c :
                    contacts) {
                String contactPhone = new StringBuilder(c.getPhoneNumber()).reverse().toString();
                Log.e("TAG", "contact reversed" + contactPhone);
                Log.e("TAG", "contact trimmed" + contactPhone.substring(8));


                if (user.getPhone() != null) {
                    if (userPhone.substring(8).equals(contactPhone.substring(8))) {
                        c.setJoined(true);
                        joinedContacts.add(c);
                    } else {
                        otherContacts.add(c);
                    }
                }
            }
        }
       // joinedContacts.addAll(otherContacts);

        contactsAdapter = new ContactsAdapter(getContext(), joinedContacts, joinedContacts, this);
        recyclerView.setAdapter(contactsAdapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setActionView(searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                contactsAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    @Override
    public void onContactSelected(Contact contact) {

    }
}
