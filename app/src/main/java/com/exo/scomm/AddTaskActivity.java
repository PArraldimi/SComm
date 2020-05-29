package com.exo.scomm;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class AddTaskActivity extends AppCompatActivity {

    private Button mBtnAddTask;
    private TextInputEditText mTitle;
    private EditText mDescription;
    private RadioButton mPrivate, mPublic;
    private RadioGroup type;
    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentUser;
    private StorageReference mStorage;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgressDialog;
    private Button mdateButton, mInvite;
    private TextView mViewDate;
    private Date CurrentDate;
    private String userId;
    private String task_id;

    private static final String TAG = "Sample";
    private static final String TAG_DATETIME_FRAGMENT = "TAG_DATETIME_FRAGMENT";
    private static final String STATE_TEXTVIEW = "STATE_TEXTVIEW";

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
        mProgressDialog = new ProgressDialog(this);
        mInvite.setVisibility(View.GONE);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();
        CurrentDate = calendar.getTime();

        userId = mCurrentUser.getUid();
        task_id = FirebaseDatabase.getInstance().getReference().child("Tasks").child(userId).push().getKey();


        if (savedInstanceState != null) {
            // Restore value from saved state
            mdateButton.setText(savedInstanceState.getCharSequence(STATE_TEXTVIEW));
        }

        type.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.private_radio) {
                    mInvite.setVisibility(View.GONE);
                } else if (checkedId == R.id.public_radio) {
                    mInvite.setVisibility(View.VISIBLE);
                }
            }
        });
        setUpDateChooser();

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
                if (date.before(CurrentDate)) {
                    Toast.makeText(getApplicationContext(), "Pick a valid time schedule", Toast.LENGTH_LONG).show();

                } else {
                    mViewDate.setText(myDateFormat.format(date));
                }
            }

            @Override
            public void onNegativeButtonClick(Date date) {
                // Do nothing
            }

            @Override
            public void onNeutralButtonClick(Date date) {
                // Optional if neutral button does'nt exists
                mViewDate.setText("");
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
        super.onSaveInstanceState(savedInstanceState);
    }

    private void createTask(String title, String description, String selectedItem, String date) {

        mProgressDialog.setTitle("Adding Task");
        mProgressDialog.setMessage("Please wait while we add the task");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        HashMap<String, String> userMap = new HashMap<>();
        userMap.put("taskOwner", userId);
        userMap.put("title", title);
        userMap.put("description", description);
        userMap.put("type", selectedItem);
        userMap.put("date", date);
        userMap.put("task_id", task_id);
        Log.e("check", "userMap" + userMap);
        if (selectedItem.equals("Private")) {
            assert task_id != null;
            mDatabase = FirebaseDatabase.getInstance().getReference().child("Tasks").child(userId).child(task_id);
            userMap.put("task_id", task_id);
            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        mProgressDialog.dismiss();
                        Toast.makeText(AddTaskActivity.this, "Task Added Successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AddTaskActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        Log.e("Error", task.getResult().toString());
                    }
                }
            });
        } else if (selectedItem.equals("Public")) {

//            DatabaseReference newNotificationRef = mRootRef.child("notifications").child(user_id).push();
//            String newNotificationId = newNotificationRef.getKey();
//
//            HashMap<String, String> notificationData = new HashMap<>();
//            notificationData.put("from", mCurrentUser.getUid());
//            notificationData.put("type", "request");
//            notificationData.put("task_id", newTask_id);
//            notificationData.put("to_user", user_id);


            mDatabase = FirebaseDatabase.getInstance().getReference().child("Tasks").child(userId).child(task_id);

            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        mProgressDialog.dismiss();

                        Toast.makeText(AddTaskActivity.this, "Task Added Successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AddTaskActivity.this, HomeActivity.class));
                        finish();
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
        // Re-init each time
        dateTimeFragment.startAtCalendarView();
        dateTimeFragment.setDefaultDateTime(calendar.getTime());
        dateTimeFragment.show(getSupportFragmentManager(), TAG_DATETIME_FRAGMENT);
    }

    public void inviteCompanions(View view) {
        Intent usersIntent = new Intent(AddTaskActivity.this, AllCompanions.class);
        usersIntent.putExtra("task_id", task_id);
        startActivity(usersIntent);
    }
}
