package com.exo.scomm.ui.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.exo.scomm.R;
import com.exo.scomm.adapters.ContactsAdapter;
import com.exo.scomm.adapters.DataHolder;
import com.exo.scomm.data.models.Contact;
import com.exo.scomm.data.models.User;
import com.exo.scomm.utils.DataViewModel;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Contacts extends AppCompatActivity implements ContactsAdapter.ContactsAdapterListener {
    public List<Contact> joinedContacts = new ArrayList<>();
    public MenuItem mDone;
    public List<User> joinedUsers = new ArrayList<>();
    Set<User> mSelectedUsersSet = new HashSet<>();
    private ContactsAdapter contactsAdapter;
    private RecyclerView recyclerView;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        MaterialToolbar toolbar = findViewById(R.id.my_toolbar);
        toolbar.setTitle("Contacts");
        setSupportActionBar(toolbar);

        List<Contact> contacts = (List<Contact>) getIntent().getSerializableExtra("contacts");

        recyclerView = findViewById(R.id.contacts_recycler);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        DataViewModel model = new ViewModelProvider(this).get(DataViewModel.class);
        model.getAllUsers().observe(this, users -> populateRecycler(users, contacts));

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void populateRecycler(List<User> users, List<Contact> contacts) {
        joinedContacts = new ArrayList<>();
        List<Contact> otherContacts = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            for (Contact c :
                    contacts) {
                if (user.getPhone() != null) {
                    if (user.getPhone().equals(c.phoneNumber)) {
                        c.setJoined(true);
                        joinedContacts.add(c);
                        joinedUsers.add(user);
                    } else {
                        otherContacts.add(c);
                    }
                }
            }
        }
        contactsAdapter = new ContactsAdapter(this, Stream.concat(joinedContacts.stream(), otherContacts.stream())
                .collect(Collectors.toList()), this);
        recyclerView.setAdapter(contactsAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_done) {
            DataHolder.setSelectedUsers(contactsAdapter.mSelectedContactsSet);
//            Intent intent = new Intent(getApplicationContext(), AddTaskActivity.class);
//            intent.putExtra("fromContactsActivity", "1");
//            intent.putExtra("users", (Serializable) contactsAdapter.mSelectedContactsSet);
//            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        mDone = menu.findItem(R.id.action_done);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) item.getActionView();
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
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onContactSelected(Contact contact) {
        mDone.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        if (contactsAdapter.mSelectedContactsSet.isEmpty()) {
            mDone.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
    }
}