<<<<<<< HEAD
package com.exo.scomm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private String privateSelected, publicSelected;
    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentUser;
    private StorageReference mStorage;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgressDialog;
    Button mdateButton, mInvite;
    TextView mViewDate;
    private Date CurrentDate;

    private static final String TAG = "Sample";

    private static final String TAG_DATETIME_FRAGMENT = "TAG_DATETIME_FRAGMENT";

    private static final String STATE_TEXTVIEW = "STATE_TEXTVIEW";

    private SwitchDateTimeDialogFragment dateTimeFragment;

    Calendar calendar = Calendar.getInstance();

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        mBtnAddTask = findViewById(R.id.button_add_task);
        mTitle = findViewById(R.id.ed_task_title);
        mDescription = findViewById(R.id.ed_task_description);
        type = findViewById(R.id.radio_group);
        mPrivate = findViewById(R.id.private_radio);
        mPublic = findViewById(R.id.public_radio);
        mInvite = findViewById(R.id.btn_invite);
        mdateButton = findViewById(R.id.date_picker);
        mViewDate = findViewById(R.id.view_date);

        mProgressDialog = new ProgressDialog(this);
        mInvite.setVisibility(View.GONE);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mStorage = FirebaseStorage.getInstance().getReference();

        CurrentDate = calendar.getTime();




        type.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.private_radio) {
                    privateSelected = "Private";
                    mInvite.setVisibility(View.GONE);
                }
                else if (checkedId == R.id.public_radio){
                    publicSelected = "Public";
                    mInvite.setVisibility(View.VISIBLE);
                }
            }
        });

        if (savedInstanceState != null) {
            // Restore value from saved state
            mdateButton.setText(savedInstanceState.getCharSequence(STATE_TEXTVIEW));
        }

        dateTimeFragment = (SwitchDateTimeDialogFragment) getSupportFragmentManager().findFragmentByTag(TAG_DATETIME_FRAGMENT);
        if(dateTimeFragment == null) {
            dateTimeFragment = SwitchDateTimeDialogFragment.newInstance(
                    getString(R.string.label_datetime_dialog),
                    getString(android.R.string.ok),
                    getString(android.R.string.cancel)
            );
        }

        mInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddTaskActivity.this, UsersActivity.class));
            }
        });



        dateTimeFragment.setTimeZone(TimeZone.getDefault());

        final SimpleDateFormat myDateFormat = new SimpleDateFormat("EEEE, dd MMM yyyy HH:mm", java.util.Locale.getDefault());

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
                if(date.before(CurrentDate)){
                    Toast.makeText(getApplicationContext(), "Pick a valid time schedule",Toast.LENGTH_LONG).show();

                }else{
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

        mdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Re-init each time
                dateTimeFragment.startAtCalendarView();
                dateTimeFragment.setDefaultDateTime(calendar.getTime());
                dateTimeFragment.show(getSupportFragmentManager(), TAG_DATETIME_FRAGMENT);
            }
        });

        mBtnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int SelectedId = type.getCheckedRadioButtonId();
                String selectedItem = null;

                if(SelectedId == mPrivate.getId()){
                    selectedItem = "Private";
                }
                else if(SelectedId == mPublic.getId()){
                    selectedItem = "Public";
                }
                String title = Objects.requireNonNull(mTitle.getText()).toString();
                String description = Objects.requireNonNull(mDescription.getText()).toString();
                String date = Objects.requireNonNull(mViewDate.getText()).toString();


                if (title.isEmpty()){
                    mTitle.setError("Enter Title");
                    mTitle.requestFocus();
                }else if(date.isEmpty()){
                    mViewDate.setError("Pick Date");
                    mViewDate.requestFocus();
                }else{
                    addTask(title, description, selectedItem, date);
                }
            }
        });

//        @Override
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            selectedItems = new ArrayList();  // Where we track the selected items
//            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//            // Set the dialog title
//            builder.setTitle(R.string.pick_toppings)
//                    // Specify the list array, the items to be selected by default (null for none),
//                    // and the listener through which to receive callbacks when items are selected
//                    .setMultiChoiceItems(R.array.toppings, null,
//                            new DialogInterface.OnMultiChoiceClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which,
//                                                    boolean isChecked) {
//                                    if (isChecked) {
//                                        // If the user checked the item, add it to the selected items
//                                        selectedItems.add(which);
//                                    } else if (selectedItems.contains(which)) {
//                                        // Else, if the item is already in the array, remove it
//                                        selectedItems.remove(Integer.valueOf(which));
//                                    }
//                                }
//                            })
//                    // Set the action buttons
//                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int id) {
//                            // User clicked OK, so save the selectedItems results somewhere
//                            // or return them to the component that opened the dialog
//                   ...
//                        }
//                    })
//                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int id) {
//                   ...
//                        }
//                    });
//
//            return builder.create();
//        }



    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the current textView
        savedInstanceState.putCharSequence(STATE_TEXTVIEW, mViewDate.getText());
        super.onSaveInstanceState(savedInstanceState);
    }

    private void addTask(String title, String description, String selectedItem, String date) {
        Log.e("check", "description" + description);
        Log.e("check", "title" + title);


        mProgressDialog.setTitle("Adding Task");
        mProgressDialog.setMessage("Please wait while we add the task");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
        String userId = mCurrentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Tasks").child(userId).push();
        HashMap<String, String> usermap = new HashMap<>();
        usermap.put("task_owner", userId);
        usermap.put("title", title);
        usermap.put("description", description);
        usermap.put("type", selectedItem);
        usermap.put("date", date);
        Log.e("check", "usermap" + usermap);
        mDatabase.setValue(usermap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    mProgressDialog.dismiss();
                    Toast.makeText(AddTaskActivity.this, "Task Added Successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AddTaskActivity.this, HomeActivity.class));
                    finish();
                }
                else{
                    Log.e("Error", task.getResult().toString());
                }
            }
        });
    }


    public void onRadioButtonClicked(View view) {

    }
}
=======
package com.exo.scomm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private String privateSelected, publicSelected;
    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentUser;
    private StorageReference mStorage;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgressDialog;
    Button mdateButton, mInvite;
    TextView mViewDate;
    private Date CurrentDate;

    private static final String TAG = "Sample";

    private static final String TAG_DATETIME_FRAGMENT = "TAG_DATETIME_FRAGMENT";

    private static final String STATE_TEXTVIEW = "STATE_TEXTVIEW";

    private SwitchDateTimeDialogFragment dateTimeFragment;

    Calendar calendar = Calendar.getInstance();

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        mBtnAddTask = findViewById(R.id.button_add_task);
        mTitle = findViewById(R.id.ed_task_title);
        mDescription = findViewById(R.id.ed_task_description);
        type = findViewById(R.id.radio_group);
        mPrivate = findViewById(R.id.private_radio);
        mPublic = findViewById(R.id.public_radio);
        mInvite = findViewById(R.id.btn_invite);
        mdateButton = findViewById(R.id.date_picker);
        mViewDate = findViewById(R.id.view_date);

        mProgressDialog = new ProgressDialog(this);
        mInvite.setVisibility(View.GONE);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mStorage = FirebaseStorage.getInstance().getReference();

        CurrentDate = calendar.getTime();




        type.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.private_radio) {
                    privateSelected = "Private";
                    mInvite.setVisibility(View.GONE);
                }
                else if (checkedId == R.id.public_radio){
                    publicSelected = "Public";
                    mInvite.setVisibility(View.VISIBLE);
                }
            }
        });

        if (savedInstanceState != null) {
            // Restore value from saved state
            mdateButton.setText(savedInstanceState.getCharSequence(STATE_TEXTVIEW));
        }

        dateTimeFragment = (SwitchDateTimeDialogFragment) getSupportFragmentManager().findFragmentByTag(TAG_DATETIME_FRAGMENT);
        if(dateTimeFragment == null) {
            dateTimeFragment = SwitchDateTimeDialogFragment.newInstance(
                    getString(R.string.label_datetime_dialog),
                    getString(android.R.string.ok),
                    getString(android.R.string.cancel)
            );
        }

        mInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddTaskActivity.this, UsersActivity.class));
            }
        });



        dateTimeFragment.setTimeZone(TimeZone.getDefault());

        final SimpleDateFormat myDateFormat = new SimpleDateFormat("EEEE, dd MMM yyyy HH:mm", java.util.Locale.getDefault());

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
                if(date.before(CurrentDate)){
                    Toast.makeText(getApplicationContext(), "Pick a valid time schedule",Toast.LENGTH_LONG).show();

                }else{
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

        mdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Re-init each time
                dateTimeFragment.startAtCalendarView();
                dateTimeFragment.setDefaultDateTime(calendar.getTime());
                dateTimeFragment.show(getSupportFragmentManager(), TAG_DATETIME_FRAGMENT);
            }
        });

        mBtnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int SelectedId = type.getCheckedRadioButtonId();
                String selectedItem = null;

                if(SelectedId == mPrivate.getId()){
                    selectedItem = "Private";
                }
                else if(SelectedId == mPublic.getId()){
                    selectedItem = "Public";
                }
                String title = Objects.requireNonNull(mTitle.getText()).toString();
                String description = Objects.requireNonNull(mDescription.getText()).toString();
                String date = Objects.requireNonNull(mViewDate.getText()).toString();


                if (title.isEmpty()){
                    mTitle.setError("Enter Title");
                    mTitle.requestFocus();
                }else if(date.isEmpty()){
                    mViewDate.setError("Pick Date");
                    mViewDate.requestFocus();
                }else{
                    addTask(title, description, selectedItem, date);
                }
            }
        });

//        @Override
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            selectedItems = new ArrayList();  // Where we track the selected items
//            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//            // Set the dialog title
//            builder.setTitle(R.string.pick_toppings)
//                    // Specify the list array, the items to be selected by default (null for none),
//                    // and the listener through which to receive callbacks when items are selected
//                    .setMultiChoiceItems(R.array.toppings, null,
//                            new DialogInterface.OnMultiChoiceClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which,
//                                                    boolean isChecked) {
//                                    if (isChecked) {
//                                        // If the user checked the item, add it to the selected items
//                                        selectedItems.add(which);
//                                    } else if (selectedItems.contains(which)) {
//                                        // Else, if the item is already in the array, remove it
//                                        selectedItems.remove(Integer.valueOf(which));
//                                    }
//                                }
//                            })
//                    // Set the action buttons
//                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int id) {
//                            // User clicked OK, so save the selectedItems results somewhere
//                            // or return them to the component that opened the dialog
//                   ...
//                        }
//                    })
//                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int id) {
//                   ...
//                        }
//                    });
//
//            return builder.create();
//        }



    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the current textView
        savedInstanceState.putCharSequence(STATE_TEXTVIEW, mViewDate.getText());
        super.onSaveInstanceState(savedInstanceState);
    }

    private void addTask(String title, String description, String selectedItem, String date) {
        Log.e("check", "description" + description);
        Log.e("check", "title" + title);


        mProgressDialog.setTitle("Adding Task");
        mProgressDialog.setMessage("Please wait while we add the task");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
        String userId = mCurrentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Tasks").child(userId).push();
        HashMap<String, String> usermap = new HashMap<>();
        usermap.put("task_owner", userId);
        usermap.put("title", title);
        usermap.put("description", description);
        usermap.put("type", selectedItem);
        usermap.put("date", date);
        Log.e("check", "usermap" + usermap);
        mDatabase.setValue(usermap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    mProgressDialog.dismiss();
                    Toast.makeText(AddTaskActivity.this, "Task Added Successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AddTaskActivity.this, HomeActivity.class));
                    finish();
                }
                else{
                    Log.e("Error", task.getResult().toString());
                }
            }
        });
    }


    public void onRadioButtonClicked(View view) {

    }
}
>>>>>>> latest-mark
