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
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.exo.scomm.R;
import com.exo.scomm.adapters.DataHolder;
import com.exo.scomm.data.models.Contact;
import com.exo.scomm.data.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

public class AddTaskActivity extends AppCompatActivity {

    public Set<User> mSelectedUsers;
    private TextInputEditText mTitle;
    private EditText mDescription;
    private RadioButton mPrivate, mPublic;
    private RadioGroup type;
    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentUser;
    private StorageReference mStorage;
    private ProgressDialog mProgressDialog;
    private Button mdateButton, pickDate, mInvite;
    private TextView mViewDate, mInvites, invitesLabel;
    private Date currentDate;
    private String userId;
    private String task_id;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    private static final String TAG = "Sample";
    private static final String TAG_DATETIME_FRAGMENT = "TAG_DATETIME_FRAGMENT";
    private static final String STATE_TEXTVIEW = "STATE_TEXTVIEW";
    private static final String STATE_TITLE = "title";
    private static final String STATE_DESC = "description";
    private SwitchDateTimeDialogFragment dateTimeFragment;
    private Calendar calendar = Calendar.getInstance();
    private DatabaseReference mRootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        mTitle = findViewById(R.id.ed_task_title);
        mDescription = findViewById(R.id.ed_task_description);
        type = findViewById(R.id.radio_group);
        mInvite = findViewById(R.id.btn_invite);
        mPrivate = findViewById(R.id.private_radio);
        mPublic = findViewById(R.id.public_radio);
        mViewDate = findViewById(R.id.view_date);
        pickDate = findViewById(R.id.date_picker);
        mProgressDialog = new ProgressDialog(this);
        mInvite.setVisibility(View.GONE);
        mInvites = findViewById(R.id.taskInvites);
        invitesLabel = findViewById(R.id.invitesLabel);
        mInvites.setVisibility(View.GONE);
        invitesLabel.setVisibility(View.GONE);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();
        currentDate = calendar.getTime();

        ListView listView = findViewById(R.id.list_view);

        userId = mCurrentUser.getUid();
        task_id = FirebaseDatabase.getInstance().getReference().child("Tasks").child(userId).push().getKey();

        if (savedInstanceState != null) {
            // Restore value from saved state
            mdateButton.setText(savedInstanceState.getCharSequence(STATE_TEXTVIEW));
            mDescription.setText(savedInstanceState.getString(STATE_TEXTVIEW));
            mTitle.setText(savedInstanceState.getString(STATE_TITLE));
        }

        type.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.private_radio) {
                    mInvite.setVisibility(View.GONE);
                    mInvites.setVisibility(View.GONE);
                    invitesLabel.setVisibility(View.GONE);
                } else if (checkedId == R.id.public_radio) {
                    mInvite.setVisibility(View.VISIBLE);
                    mInvites.setVisibility(View.VISIBLE);
                    invitesLabel.setVisibility(View.VISIBLE);

                }
            }
        });
        setUpDateChooser();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSelectedUsers = DataHolder.getSelectedUsers();
        if (mSelectedUsers != null) {
            Set<String> inviteNames = new HashSet<>();
            for (User u : mSelectedUsers
            ) {
                inviteNames.add(u.getUsername());
            }
            mInvites.setText(inviteNames.toString());
        }

    }

    private void setUpDateChooser() {
        dateTimeFragment = (SwitchDateTimeDialogFragment) getSupportFragmentManager().findFragmentByTag(TAG_DATETIME_FRAGMENT);
        if (dateTimeFragment == null) {
            dateTimeFragment = SwitchDateTimeDialogFragment.newInstance(
                    getString(R.string.label_datetime_dialog),
                    getString(android.R.string.ok),
                    getString(android.R.string.cancel)
            );
        }

        dateTimeFragment.setTimeZone(TimeZone.getDefault());

        final SimpleDateFormat myDateFormat = new SimpleDateFormat("EEEE, dd MMM yyyy HH:mm", Locale.getDefault());

        boolean set24HoursMode = DateFormat.is24HourFormat(this);

        dateTimeFragment.set24HoursMode(set24HoursMode);
        dateTimeFragment.setHighlightAMPMSelection(false);
        dateTimeFragment.setCancelable(false);
        dateTimeFragment.setMinimumDateTime(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        dateTimeFragment.setMaximumDateTime(new GregorianCalendar(2030, Calendar.DECEMBER, 31).getTime());

        try {
            dateTimeFragment.setSimpleDateMonthAndDayFormat(new SimpleDateFormat("MMMM dd", Locale.getDefault()));
        } catch (SwitchDateTimeDialogFragment.SimpleDateMonthAndDayFormatException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }

        dateTimeFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonWithNeutralClickListener() {
            @Override
            public void onPositiveButtonClick(Date date) {
                long validTime = currentDate.getTime() + 900000;

                if (date.before(currentDate)) {
                    pickDate.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Pick a valid time schedule", Toast.LENGTH_LONG).show();

                } else if (date.getTime() < validTime) {
                    pickDate.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Please choose task time beyond 15 minutes", Toast.LENGTH_LONG).show();
                } else {
                    mViewDate.setText(myDateFormat.format(date));
                    pickDate.setEnabled(true);
                }
            }

            @Override
            public void onNegativeButtonClick(Date date) {
                // Do nothing
                pickDate.setEnabled(true);

            }

            @Override
            public void onNeutralButtonClick(Date date) {
                // Optional if neutral button does'nt exists
                mViewDate.setText("");
                pickDate.setEnabled(true);

            }
        });
    }

    public void addTask(View view) {
        prepareTask();
    }

    private void prepareTask() {
        int SelectedId = type.getCheckedRadioButtonId();
        String selectedItem = null;

        if (SelectedId == mPrivate.getId()) {
            selectedItem = "Private";
        } else if (SelectedId == mPublic.getId()) {
            selectedItem = "Public";
        }
        String title = Objects.requireNonNull(mTitle.getText()).toString();
        String description = Objects.requireNonNull(mDescription.getText()).toString();
        String date = Objects.requireNonNull(mViewDate.getText()).toString();

        if (title.isEmpty()) {
            mTitle.setError("Enter Title");
            mTitle.requestFocus();
        } else if (date.isEmpty()) {
            mViewDate.setError("Pick Date");
            mViewDate.requestFocus();
        } else {
            createTask(title, description, selectedItem, date);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the current textView
        savedInstanceState.putCharSequence(STATE_TEXTVIEW, mViewDate.getText());
        savedInstanceState.putCharSequence(STATE_TITLE, mTitle.getText());
        savedInstanceState.putCharSequence(STATE_DESC, mDescription.getText());
        super.onSaveInstanceState(savedInstanceState);
    }

    private void createTask(String title, String description, String selectedItem, final String date) {

        mProgressDialog.setTitle("Adding Task");
        mProgressDialog.setMessage("Please wait while we add the task");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        final HashMap<String, String> taskMap = new HashMap<>();
        taskMap.put("taskOwner", userId);
        taskMap.put("title", title);
        taskMap.put("description", description);
        taskMap.put("type", selectedItem);
        taskMap.put("date", date);
        taskMap.put("task_id", task_id);
        if (selectedItem.equals("Private")) {
            assert task_id != null;
            mDatabase = FirebaseDatabase.getInstance().getReference().child("Tasks").child(userId).child(task_id);
            mDatabase.setValue(taskMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        mRootRef.child("TaskSupers").child(task_id).child(mCurrentUser.getUid()).setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                mProgressDialog.dismiss();
                                Toast.makeText(AddTaskActivity.this, "Task Added Successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });

                    } else {
                        Log.e("Error", task.getResult().toString());
                    }
                }
            });
        } else if (selectedItem.equals("Public")) {
            mDatabase = FirebaseDatabase.getInstance().getReference().child("Tasks").child(userId).child(task_id);
            mDatabase.setValue(taskMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        if (mSelectedUsers == null) {
                            Toast.makeText(AddTaskActivity.this, "Task Added Successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            for (final User user : mSelectedUsers
                            ) {
                                String userId = user.getUID();
                                DatabaseReference newNotificationRef = mRootRef.child("Notifications").child(userId).push();
                                String newNotificationId = newNotificationRef.getKey();

                                Map recipientNote = new HashMap<>();
                                recipientNote.put("fromUser", mCurrentUser.getUid());
                                recipientNote.put("type", "received");
                                recipientNote.put("task_id", task_id);
                                recipientNote.put("date", ServerValue.TIMESTAMP);

                                Map senderNote = new HashMap<>();
                                senderNote.put("toUser", userId);
                                senderNote.put("type", "sent");
                                senderNote.put("task_id", task_id);
                                senderNote.put("date", ServerValue.TIMESTAMP);

                                Map requestSentData = new HashMap();
                                requestSentData.put("request_type", "sent");
                                requestSentData.put("date", ServerValue.TIMESTAMP);
                                requestSentData.put("accepted", "false");
                                requestSentData.put("task_id", task_id);


                                Map requestReceivedData = new HashMap();
                                requestReceivedData.put("request_type", "received");
                                requestReceivedData.put("date", ServerValue.TIMESTAMP);
                                requestReceivedData.put("task_id", task_id);
                                requestReceivedData.put("accepted", "false");

                                Map requestMap = new HashMap<>();
                                requestMap.put("Notifications/" + userId + "/" + newNotificationId, recipientNote);
                                requestMap.put("Tasks/" + mCurrentUser.getUid() + "/" + task_id + "/", taskMap);
                                requestMap.put("TaskInviteRequests/" + mCurrentUser.getUid() + "/" + userId + "/" + task_id, requestSentData);
                                requestMap.put("TaskInviteRequests/" + userId + "/" + mCurrentUser.getUid() + "/" + task_id, requestReceivedData);
                                requestMap.put("TaskSupers/" + task_id + "/" + mCurrentUser.getUid() + "/", ServerValue.TIMESTAMP);

                                mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                        if (databaseError != null) {
                                            Toast.makeText(getApplicationContext(), "Error in sending invite request", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Toast.makeText(AddTaskActivity.this, "Task Added Successfully", Toast.LENGTH_SHORT).show();
                                            Toast.makeText(AddTaskActivity.this, "Request sent to " + user.getUsername() + " successfully", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    }
                                });
                            }
                        }
                    } else {
                        Log.e("Error", Objects.requireNonNull(task.getResult()).toString());
                    }
                }
            });
        }

    }

    public void onRadioButtonClicked(View view) {

    }

    public void pickTime(View view) {
        view.findViewById(R.id.date_picker).setEnabled(false);
        // Re-init each time
        dateTimeFragment.startAtCalendarView();
        dateTimeFragment.setDefaultDateTime(calendar.getTime());
        dateTimeFragment.show(getSupportFragmentManager(), TAG_DATETIME_FRAGMENT);
    }

    public void inviteCompanions(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            List<Contact> contacts = getContactList();
           FragmentManager fm = getSupportFragmentManager();
           ContactsFragment contactsFragment = ContactsFragment.newInstance(contacts);
           contactsFragment.show(fm, "contacts");

        }
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


    private static final String[] PROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
    };


    private ArrayList<Contact> getContactList() {
        ArrayList<Contact> contactList = new ArrayList<>();
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        if (cursor != null) {
            HashSet<String> mobileNoSet = new HashSet<String>();
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

    @Override
    protected void onResume() {
        super.onResume();
        Intent i = getIntent();
        String data = i.getStringExtra("fromUsersActivity");
        if (data != null && data.contentEquals("1")) {
            Set<User> users = (Set<User>) i.getSerializableExtra("users");

            assert users != null;
            Log.e("HomeResume", "" + users.size());
        }

    }
}
