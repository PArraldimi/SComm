package com.exo.scomm.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.exo.scomm.R;
import com.exo.scomm.model.TasksModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class EditTaskDialog extends DialogFragment {

   private TextInputEditText mTitleEditText;
   private ProgressDialog mProgressDialog;
   private  EditText mDesc;
   DatabaseReference mRootRef;
   private TextView mSubmit, mCancel, mViewdate;
   private SwitchDateTimeDialogFragment dateTimeFragment;
   private Calendar calendar = Calendar.getInstance();
   private boolean succeeded = false;

   private static final String TAG_DATETIME_FRAGMENT = "TAG_DATETIME_FRAGMENT";



   public interface EditTaskDialogResultListener {
      void onFinishTaskEditDialog(TasksModel inputText);
   }
   public EditTaskDialog() {
      // Empty constructor is required for DialogFragment
      // Make sure not to add arguments to the constructor
      // Use `newInstance` instead as shown below
   }
   public static EditTaskDialog newInstance(String title, TasksModel task) {
      EditTaskDialog frag = new EditTaskDialog();
      Bundle args = new Bundle();
      args.putSerializable("task", task);
      args.putString("title", title);
      frag.setArguments(args);
      return frag;
   }
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      return inflater.inflate(R.layout.edit_task_layout, container);
   }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
      // Get field from view
      mTitleEditText =  view.findViewById(R.id.edit_task_title);
      mViewdate = view.findViewById(R.id.task_edit_view_date);
      mDesc = view.findViewById(R.id.ed_task_description);
      mSubmit = view.findViewById(R.id.edit_task_submit);
      mRootRef = FirebaseDatabase.getInstance().getReference();
      mCancel = view.findViewById(R.id.edit_task_cancel);
      mProgressDialog = new ProgressDialog(getContext());

      setUpDateChooser();
      // Fetch arguments from bundle and set title
      assert getArguments() != null;
      final TasksModel task = (TasksModel) getArguments().getSerializable("task");
      String title = getArguments().getString("title", "Please Enter Task Details");
      assert task != null;
      mDesc.setText(task.getDescription());
      mTitleEditText.setText(task.getTitle());
      mViewdate.setText(task.getDate());
      Objects.requireNonNull(getDialog()).setTitle(title);
      // Show soft keyboard automatically and request focus to field
      mTitleEditText.requestFocus();
      Objects.requireNonNull(getDialog().getWindow()).setSoftInputMode(
              WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

      Button changeDateTime = view.findViewById(R.id.edit_task_date_time);
      changeDateTime.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            pickTime();
         }
      });

      mCancel.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            Objects.requireNonNull(getDialog()).dismiss();
         }
      });
      mSubmit.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            String title = Objects.requireNonNull(mTitleEditText.getText()).toString();
            String desc = Objects.requireNonNull(mDesc.getText()).toString();
            String date = Objects.requireNonNull(mViewdate.getText()).toString();
            task.setDate(date);
            task.setDescription(desc);
            task.setTitle(title);
            submitTask(task);
         }
      });
   }

   private void submitTask(final TasksModel task) {
      mProgressDialog.setTitle("Updating Task");
      mProgressDialog.setMessage("Please wait while we update the task");
      mProgressDialog.setCanceledOnTouchOutside(false);
      mProgressDialog.show();

      final HashMap<String, String> taskMap = new HashMap<>();
      taskMap.put("title", task.getTitle());
      taskMap.put("description", task.getDescription());
      taskMap.put("date", task.getDate());
      taskMap.put("type", task.getType());
      taskMap.put("taskOwner", task.getTaskOwner());
      taskMap.put("task_id", task.getTask_id());

      Map requestMap = new HashMap<>();
//      requestMap.put("Notifications/" + userId + "/" + newNotificationId, recipientNote);
//      requestMap.put("Notifications/" + mCurrentUser.getUid() + "/" + newNotificationId, senderNote);
      requestMap.put("Tasks/" + task.getTaskOwner() + "/" + task.getTask_id() + "/", taskMap);

      mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
         @Override
         public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
            mProgressDialog.dismiss();

            if (databaseError != null) {
               Toast.makeText(getContext(), "There was some error updaing the task", Toast.LENGTH_SHORT).show();
            } else {
               Toast.makeText(getActivity(), "Task Updated Successfully", Toast.LENGTH_SHORT).show();
               EditTaskDialogResultListener activity = (EditTaskDialogResultListener) getActivity();
               assert activity != null;
               activity.onFinishTaskEditDialog(task);
               EditTaskDialog.this.dismiss();
            }
         }
      });
   }

   private void pickTime() {
      // Re-init each time
      dateTimeFragment.startAtCalendarView();
      dateTimeFragment.setDefaultDateTime(calendar.getTime());
      dateTimeFragment.show(requireActivity().getSupportFragmentManager(), TAG_DATETIME_FRAGMENT);
   }

   private void setUpDateChooser() {
       final Date currentDate = calendar.getTime();

      dateTimeFragment = (SwitchDateTimeDialogFragment) requireActivity().getSupportFragmentManager().findFragmentByTag(TAG_DATETIME_FRAGMENT);
      if (dateTimeFragment == null) {
         dateTimeFragment = SwitchDateTimeDialogFragment.newInstance(
                 getString(R.string.label_datetime_dialog),
                 getString(android.R.string.ok),
                 getString(android.R.string.cancel)
         );
      }

      dateTimeFragment.setTimeZone(TimeZone.getDefault());

      final SimpleDateFormat myDateFormat = new SimpleDateFormat("EEEE, dd MMM yyyy HH:mm", Locale.getDefault());

      boolean set24HoursMode = DateFormat.is24HourFormat(getContext());

      dateTimeFragment.set24HoursMode(set24HoursMode);
      dateTimeFragment.setHighlightAMPMSelection(false);
      dateTimeFragment.setMinimumDateTime(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
      dateTimeFragment.setMaximumDateTime(new GregorianCalendar(2030, Calendar.DECEMBER, 31).getTime());

      try {
         dateTimeFragment.setSimpleDateMonthAndDayFormat(new SimpleDateFormat("MMMM dd", Locale.getDefault()));
      } catch (SwitchDateTimeDialogFragment.SimpleDateMonthAndDayFormatException e) {
         Log.e("TAG", Objects.requireNonNull(e.getMessage()));
      }

      dateTimeFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonWithNeutralClickListener() {
         @Override
         public void onPositiveButtonClick(Date date) {
            if (date.before(currentDate)) {
               Toast.makeText(getContext(), "Pick a valid time schedule", Toast.LENGTH_LONG).show();

            } else {
               mViewdate.setText(myDateFormat.format(date));
            }
         }

         @Override
         public void onNegativeButtonClick(Date date) {
            // Do nothing
         }

         @Override
         public void onNeutralButtonClick(Date date) {
            // Optional if neutral button does'nt exists
         }
      });
   }
}
