package com.exo.scomm.ui.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.exo.scomm.R;
import com.exo.scomm.adapters.AcceptedCompanionsAdapter;
import com.exo.scomm.adapters.CompanionsTasksAdapter;
import com.exo.scomm.adapters.DataHolder;
import com.exo.scomm.adapters.PendingCompanionsAdapter;
import com.exo.scomm.data.models.Contact;
import com.exo.scomm.data.models.Task;
import com.exo.scomm.data.models.User;
import com.exo.scomm.ui.fragments.EditTaskDialog;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TaskDetails extends AppCompatActivity implements EditTaskDialog.EditTaskDialogResultListener {
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private static final String[] PROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
    };
    private final Set<User> taskCompList = new HashSet<>();
    int mDeleteBnState;
    private final Set<User> taskPendingCompList = new HashSet<>();
    private Button mInvite;
    private RecyclerView myTaskCompanions;
    private TextView taskDesc, taskTitle, taskDate, taskType, taskCreator, mTextViewDate;
    private String mCurrentUID;
    private DatabaseReference mRootRef, mUsersRef, taskCompRef;
    private Calendar calendar;
    private ProgressDialog mProgressDialog;
    private Task taskDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);
        MaterialToolbar toolbar = findViewById(R.id.task_details_app_bar);
        toolbar.setTitle("Task Details");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();  // byDefault provided backPressed method, or handle your own way
        });

        Button deleteTask = this.findViewById(R.id.details_delete_task);
        calendar = Calendar.getInstance();
        mTextViewDate = this.findViewById(R.id.details_today_date_time);
        mInvite = this.findViewById(R.id.details_invite);
        taskDesc = this.findViewById(R.id.details_task_desc);
        taskTitle = findViewById(R.id.detail_task_item_title);
        taskDate = findViewById(R.id.detail_task_item_time);
        taskType = findViewById(R.id.detail_task_item_type);
        taskCreator = findViewById(R.id.details_task_item_creator);
        myTaskCompanions = this.findViewById(R.id.task_details_companions_recycler);
        Button editTask = this.findViewById(R.id.task_details_edit);
        mProgressDialog = new ProgressDialog(TaskDetails.this);

        ImageView mScommingDetails = this.findViewById(R.id.scomming_details);
        RelativeLayout layout = findViewById(R.id.scomming_details_btn);
        taskDetails = (Task) getIntent().getSerializableExtra("task");
        deleteTask.setText(R.string.scom_out);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUsersRef = mRootRef.child("Users");
        taskCompRef = mRootRef.child("TaskCompanions");
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        linearLayoutManager1.setStackFromEnd(true);
        myTaskCompanions.setLayoutManager(linearLayoutManager1);
        mCurrentUID = FirebaseAuth.getInstance().getUid();

        taskType.setText(taskDetails.getType());
        taskDate.setText(taskDetails.getDate());
        taskTitle.setText(taskDetails.getTitle());
        taskDesc.setText(taskDetails.getDescription());

        mInvite.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            } else {
                Intent intent = new Intent(TaskDetails.this.getApplicationContext(), Contacts.class);
                intent.putExtra("contacts", (Serializable) getContactList());
                startActivityForResult(intent, 2404);
            }
        });

        if (taskDetails.getType().equals("Private")) {
            getTaskOwner(mCurrentUID);
            mDeleteBnState = 0;
            deleteTask.setText(R.string.delete_task);
            mInvite.setVisibility(View.GONE);
            layout.setVisibility(View.GONE);
        } else {
            getPendingCompanions(taskDetails.getTask_id());

            if (taskDetails.getTaskOwner().equals(mCurrentUID)) {
                getTaskOwner(mCurrentUID);
                deleteTask.setText(R.string.delete_task);
                editTask.setVisibility(View.VISIBLE);
                mDeleteBnState = 0;
            } else {
                mDeleteBnState = 1;
                getTaskOwner(taskDetails.getTaskOwner());
                mRootRef.child("TaskSupers").child(taskDetails.getTaskOwner()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(mCurrentUID)) {
                            mInvite.setEnabled(true);
                            mInvite.setVisibility(View.VISIBLE);
                        } else {
                            mInvite.setEnabled(false);
                            mInvite.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
        editTask.setOnClickListener(v -> editTask(taskDetails));
        getTaskCompanions();
        deleteTask.setOnClickListener(v -> {
            if (mDeleteBnState == 0) {
               // Toast.makeText(TaskDetails.this, "Deleting the task", Toast.LENGTH_SHORT).show();
                deleteTask();
                //Toast.makeText(TaskDetails.this, " Task deleted successfully", Toast.LENGTH_SHORT).show();
            }
            if (mDeleteBnState == 1) {
                schommeOut();
            }
        });
        mScommingDetails.setOnClickListener(v -> showBottomDialog());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 2404) {
            if (data != null) {
                List<Contact> mSelectedContacts = (List<Contact>) data.getSerializableExtra("selectedContactsList");
                List<User> mJoinedUsers = (List<User>) data.getSerializableExtra("joinedUsersList");
                final Set<User> usersToInvite = filterUsersToInvite(mJoinedUsers, mSelectedContacts);
                inviteUserToTask( usersToInvite);
            }
        }
    }

    private void inviteUserToTask(Set<User> userSet) {
        List<User> users = new ArrayList<>(userSet);
        Map<String, Object> requestMap = new HashMap<>();
        String uid;
        for (User user : users
        ) {
            for (int i = 0; i < users.size(); i++) {
                User user1 = users.get(i);
                uid = user1.getId();
                requestMap.put("InvitedUsers/" + user.getId() + "/" + taskDetails.getTask_id() + "/" + uid + "/", ServerValue.TIMESTAMP);
                requestMap.put("InvitedUsers/" + mCurrentUID + "/" + taskDetails.getTask_id() + "/" + uid + "/", ServerValue.TIMESTAMP);
                mRootRef.updateChildren(requestMap).addOnCompleteListener(task -> {
                    if (task.isComplete()) {
                        if (task.isSuccessful()) {
                            String noteId = mRootRef.child("Notifications").child(user.getId()).push().getKey();
                            sendNotifications(user1.getId(), noteId);
                        } else {
                            Log.e("ADD TASK ", task.getException().getMessage());
                        }
                    }
                });
            }
            Toast.makeText(TaskDetails.this.getApplicationContext(), user.getUsername() + "  invited successfully", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(TaskDetails.this.getApplicationContext(), "Task Added Successfully ", Toast.LENGTH_LONG).show();
    }

    private void sendNotifications(String userId, String noteId) {
        Map<String, Object> noteMap = new HashMap<>();
        noteMap.put("fromUser", mCurrentUID);
        noteMap.put("type", "invite");
        noteMap.put("toUser", userId);
        noteMap.put("task_id", taskDetails.getTask_id());
        noteMap.put("date", ServerValue.TIMESTAMP);

        Map<String, Object> noteRequestMap = new HashMap<>();
        noteRequestMap.put("Notifications/" + userId + "/" + noteId + "/", noteMap);
        mRootRef.updateChildren(noteRequestMap).addOnCompleteListener(task -> {
            if (task.isComplete()) {
                if (task.isSuccessful()) {
                } else {
                    Log.e("ADD TASK ", task.getException().getMessage());
                }
            }
        });

    }

    private void showBottomDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                TaskDetails.this, R.style.BottomSheetDialogTheme
        );
        View bottomSheetView = LayoutInflater.from(getApplicationContext())
                .inflate(
                        R.layout.scomming_details_layout,
                        (LinearLayout) findViewById(R.id.scomming_detail)
                );

        RecyclerView acceptedTaskCompanionsRecycler = bottomSheetView.findViewById(R.id.task_details_accepted_recycler);
        RecyclerView toAcceptTaskRecycler = bottomSheetView.findViewById(R.id.task_details_invites_recycler);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        LinearLayoutManager linearLayoutManager3 = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        acceptedTaskCompanionsRecycler.setLayoutManager(linearLayoutManager2);
        toAcceptTaskRecycler.setLayoutManager(linearLayoutManager3);
        toAcceptTaskRecycler.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL));
        acceptedTaskCompanionsRecycler.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL));



//        allowScrolling(acceptedTaskCompanionsRecycler);
//        allowScrolling(toAcceptTaskRecycler);


        PendingCompanionsAdapter pendingCompanionsAdapter = new PendingCompanionsAdapter(TaskDetails.this, taskPendingCompList, taskDetails.getTask_id());
        AcceptedCompanionsAdapter acceptedCompanionsAdapter = new AcceptedCompanionsAdapter(TaskDetails.this, taskCompList, taskDetails.getTask_id());
        acceptedTaskCompanionsRecycler.setAdapter(acceptedCompanionsAdapter);
        toAcceptTaskRecycler.setAdapter(pendingCompanionsAdapter);

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void allowScrolling(RecyclerView acceptedTaskCompanionsRecycler) {
        acceptedTaskCompanionsRecycler.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v1, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow NestedScrollView to intercept touch events.
                        v1.getParent().requestDisallowInterceptTouchEvent(false);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow NestedScrollView to intercept touch events.
                        v1.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle RecyclerView touch events.
                v1.onTouchEvent(event);
                return true;
            }
        });
    }

    private void getPendingCompanions(final String task_id) {
        mRootRef.child("InvitedUsers").child(mCurrentUID).child(task_id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                                final String userId = childDataSnapshot.getKey();
                                Log.e("Invited User Id", userId);
                                if (userId != null) {
                                    mUsersRef.child(userId).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                User user = dataSnapshot.getValue(User.class);
                                                taskPendingCompList.add(user);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                    Log.e("Invited Users", taskPendingCompList.toString());
                                }
                            }
                        }
                        Log.e("Invited Users", taskPendingCompList.toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void editTask(Task task) {
        FragmentManager fm = getSupportFragmentManager();
        EditTaskDialog editNameDialogFragment = EditTaskDialog.newInstance("Change Task Details", task);
        editNameDialogFragment.show(fm, "fragment_edit_task");
    }

    private void getTaskOwner(String owner) {
        mUsersRef.child(owner).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("username").getValue().toString();
                taskCreator.setText(String.format("Created By %s", username));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void schommeOut() {
        HashMap<String, Object> schommOutMap = new HashMap<>();
        schommOutMap.put("TaskCompanions/" + mCurrentUID + "/" + taskDetails.getTask_id() + "/", null);
        schommOutMap.put("Tasks/" + mCurrentUID + "/" + taskDetails.getTask_id() + "/", null);
        mRootRef.updateChildren(schommOutMap, (databaseError, databaseReference) -> {
            if (databaseError == null) {
                taskType.setText("");
                taskDate.setText("");
                taskTitle.setText("");
                taskDesc.setText("");
                taskCreator.setText("");
                Toast.makeText(TaskDetails.this, "You opted out of the task successfully", Toast.LENGTH_SHORT).show();
            } else {
                String error = databaseError.getMessage();
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                getContactList();
            } else {
                Toast.makeText(this, "Until you grant the permission, we cannot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void getTaskCompanions() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Fetching companions");
        progressDialog.setMessage("Please wait!!");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        final DatabaseReference companionsRef = taskCompRef.child(mCurrentUID).child(taskDetails.getTask_id());
        companionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressDialog.dismiss();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                        final String userId = childDataSnapshot.getKey();
                        mUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                assert userId != null;
                                if (dataSnapshot.hasChild(userId)) {
                                    User user = dataSnapshot.child(userId).getValue(User.class);
                                    assert user != null;
                                    user.setUID(userId);
                                    taskCompList.add(user);
                                    CompanionsTasksAdapter adapter = new CompanionsTasksAdapter(TaskDetails.this, taskCompList, taskDetails.getTask_id());
                                    myTaskCompanions.setAdapter(adapter);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        String currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
        mTextViewDate.setText(currentDate);
    }

    private Set<User> filterUsersToInvite(List<User> joinedUsers, List<Contact> mSelectedContacts) {
        Set<User> usersToInvite = new HashSet<>();
        for (int i = 0; i < joinedUsers.size(); i++) {
            User user = joinedUsers.get(i);
            String userPhone;
            if (user.getPhone() != null) {
                userPhone = new StringBuilder(user.getPhone()).reverse().toString();
                for (Contact c :
                        mSelectedContacts) {
                    String contactPhone = new StringBuilder(c.getPhoneNumber()).reverse().toString();
                    if (userPhone.length() >= 10 && contactPhone.length() >= 10) {
                        String trimmedUserPhone = userPhone.substring(0,7);
                        String trimmedContactPhone = contactPhone.substring(0,7);
                        if (trimmedUserPhone.equals(trimmedContactPhone) ) {
                            usersToInvite.add(user);
                        }
                    }
                }
            }
        }
        return usersToInvite;
    }

    public void deleteTask() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Deleting task");
        progressDialog.setMessage("Please wait!!");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        String noteKey = mRootRef.child("Notifications").child(taskDetails.getTask_id()).push().getKey();
        if (taskDetails.getType().equals("Private")) {
            deletePrivateTask(noteKey, mCurrentUID);
            progressDialog.dismiss();
            Toast.makeText(TaskDetails.this.getApplicationContext(), " Task deleted successfully", Toast.LENGTH_SHORT).show();

            finish();
        } else {
            if (taskPendingCompList.size() == 0 && taskCompList.size() == 0) {
                deletePrivateTask(noteKey, mCurrentUID);
            } else {
                deletePublicTask(noteKey);
            }
        }
    }

    private void deletePublicTask(String noteKey) {

        Map<String, Object> recipientNote = new HashMap<>();
        recipientNote.put("type", "deletedTask");
        recipientNote.put("task_id", taskDetails.getTask_id());
        recipientNote.put("taskOwner", mCurrentUID);
        recipientNote.put("date", ServerValue.TIMESTAMP);
        for (User user : taskPendingCompList
        ) {
            HashMap<String, Object> deleteTaskMap = new HashMap<>();
            deleteTaskMap.put("InviteUsers/" + user.getUID() + "/" + taskDetails.getTask_id(), null);
            deleteTaskMap.put("Notifications/" + user.getUID() + "/" + taskDetails.getTask_id() + "/", null);
            deleteTaskMap.put("Tasks/" + mCurrentUID + "/" + taskDetails.getTask_id(), null);
            deleteTaskMap.put("Tasks/" + user.getUID() + "/" + taskDetails.getTask_id(), null);
            deleteTaskMap.put("DeletedTask/" + noteKey + "/" + taskDetails.getTask_id() + "/", taskDetails);
            performDelete(deleteTaskMap);
        }

        for (User user : taskCompList
        ) {
            HashMap<String, Object> deleteTaskMap = new HashMap<>();
            deleteTaskMap.put("Notifications/" + user.getUID() + "/" + taskDetails.getTask_id() + "/", recipientNote);
            deleteTaskMap.put("Tasks/" + mCurrentUID + "/" + taskDetails.getTask_id(), null);
            deleteTaskMap.put("Tasks/" + user.getUID() + "/" + taskDetails.getTask_id(), null);
            deleteTaskMap.put("DeletedTask/" + noteKey + "/" + taskDetails.getTask_id() + "/", taskDetails);
            performDelete(deleteTaskMap);
        }
    }

    private void deletePublicTask(String noteKey, User user) {
        Map<String, Object> recipientNote = new HashMap<>();
        recipientNote.put("type", "deletedTask");
        recipientNote.put("task_id", taskDetails.getTask_id());
        recipientNote.put("taskOwner", mCurrentUID);
        recipientNote.put("date", ServerValue.TIMESTAMP);

        HashMap<String, Object> deleteTaskMap = new HashMap<>();
        deleteTaskMap.put("Notifications/" + user.getUID() + "/" + taskDetails.getTask_id() + "/" + noteKey, recipientNote);
        deleteTaskMap.put("Tasks/" + mCurrentUID + "/" + taskDetails.getTask_id(), null);
        deleteTaskMap.put("Tasks/" + user.getUID() + "/" + taskDetails.getTask_id(), null);
        deleteTaskMap.put("DeletedTask/" + noteKey + "/" + taskDetails.getTask_id() + "/", taskDetails);
        performDelete(deleteTaskMap);
    }

    private void performDelete(HashMap<String, Object> deleteTaskMap) {
        mRootRef.updateChildren(deleteTaskMap, (databaseError, databaseReference) -> {
            if (databaseError == null) {
                Toast.makeText(TaskDetails.this, "Task deleted successfully", Toast.LENGTH_SHORT).show();
                taskType.setText("");
                taskDate.setText("");
                taskTitle.setText("");
                taskDesc.setText("");
                taskCreator.setText("");
                this.finish();
            } else {
                String error = databaseError.getMessage();
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deletePrivateTask(String deleted_task_id, String mCurrentUID) {
        HashMap<String, Object> deleteTaskMap = new HashMap<>();
        deleteTaskMap.put("Tasks/" + mCurrentUID + "/" + taskDetails.getTask_id(), null);
        deleteTaskMap.put("DeletedTask/" + deleted_task_id + "/" + taskDetails.getTask_id() + "/", taskDetails);
        performDelete(deleteTaskMap);
    }

    public void addNewTask(View view) {
        Intent intent = new Intent(TaskDetails.this, AddTaskActivity.class);
        startActivity(intent);
    }

    @Override
    public void onFinishTaskEditDialog(Task task) {
        taskType.setText(task.getType());
        taskDate.setText(task.getDate());
        taskTitle.setText(task.getTitle());
        taskDesc.setText(task.getDescription());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Respond to the action bar's Up/Home button
        if (item.getItemId() == android.R.id.home) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public List<Contact> getContactList() {
        List<Contact> contactList = new ArrayList<>();
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        if (cursor != null) {
            HashSet<String> mobileNoSet = new HashSet<>();
            try {
                final int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                final int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String name, number;
                while (cursor.moveToNext()) {
                    name = cursor.getString(nameIndex);
                    number = cursor.getString(numberIndex);
                    number = number.replace(" ", "");
                    if (!mobileNoSet.contains(number)) {
                        contactList.add(new Contact(name, number));
                        mobileNoSet.add(number);
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return contactList;
    }
}
