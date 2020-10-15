package com.exo.scomm.ui.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
    private ContactsAdapter contactsAdapter;
    private RecyclerView recyclerView;
    private FloatingActionButton mSelectedContactsFab;
    private RelativeLayout mLayout;
    private TextView mSelectedScommers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        MaterialToolbar toolbar = findViewById(R.id.my_toolbar);
        toolbar.setTitle("Contacts");
        setSupportActionBar(toolbar);
        mSelectedContactsFab = findViewById(R.id.selectedContactsFab);
        mSelectedScommers = findViewById(R.id.selected_scommers);

        mLayout = findViewById(R.id.relative004);
        mLayout.setVisibility(View.GONE);
        mSelectedContactsFab.setVisibility(View.GONE);

        List<Contact> contacts = (List<Contact>) getIntent().getSerializableExtra("contacts");

        recyclerView = findViewById(R.id.contacts_recycler);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        DataViewModel model = new ViewModelProvider(this).get(DataViewModel.class);
        model.getAllUsers().observe(this, users -> populateRecycler(users, contacts));

        mSelectedContactsFab.setOnClickListener(v -> {
            DataHolder.setSelectedUsers(contactsAdapter.mSelectedContactsSet);
            finish();
        });
    }

    private void populateRecycler(List<User> users, List<Contact> contacts) {
        joinedContacts = new ArrayList<>();
        List<Contact> otherContacts = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            String userPhone;
            if (user.getPhone() != null){
             userPhone = new StringBuilder(user.getPhone()).reverse().toString();
                for (Contact c :
                        contacts) {
                    String contactPhone = new StringBuilder(c.getPhoneNumber()).reverse().toString();

                    if ((userPhone != null && userPhone.length() >= 10 ) && (contactPhone != null && contactPhone.length() >= 10)) {
                        if (userPhone.substring(0, 8).equals(contactPhone.substring(0, 8))) {
                            c.setJoined(true);
                            joinedContacts.add(c);
                            joinedUsers.add(user);
                        } else {
                            otherContacts.add(c);
                        }
                    }
                }
            }


        }
        contactsAdapter = new ContactsAdapter(this,joinedContacts,otherContacts, this);
        recyclerView.setAdapter(contactsAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_done) {
            DataHolder.setSelectedUsers(contactsAdapter.mSelectedContactsSet);
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
        mLayout.setVisibility(View.VISIBLE);
        mSelectedContactsFab.setVisibility(View.VISIBLE);
        String str = getSelectedContacts().toString().replaceAll("\\[|\\]", "");
        mSelectedScommers.setText(str);
      //  mDone.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        if (contactsAdapter.mSelectedContactsSet.isEmpty()) {
          //  mDone.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            mLayout.setVisibility(View.GONE);
            mSelectedContactsFab.setVisibility(View.GONE);
        }
    }
    private List<String> getSelectedContacts(){
        List<String> name = new ArrayList<>();
        for (Contact contact: contactsAdapter.mSelectedContactsSet
             ) {
            name.add(contact.getName());
        }
        return name;
    }
}