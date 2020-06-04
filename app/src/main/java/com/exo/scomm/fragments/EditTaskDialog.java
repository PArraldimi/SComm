package com.exo.scomm.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.exo.scomm.R;

public class EditTaskDialog extends DialogFragment {

   private EditText mTitleEditText;
   private Button mChangeDate;
   private TextView mSubmit, mCancel, mViewdate;

   public EditTaskDialog() {
      // Empty constructor is required for DialogFragment
      // Make sure not to add arguments to the constructor
      // Use `newInstance` instead as shown below
   }
   public static EditTaskDialog newInstance(String title) {
      EditTaskDialog frag = new EditTaskDialog();
      Bundle args = new Bundle();
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
   public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
      // Get field from view
      mTitleEditText = (EditText) view.findViewById(R.id.edit_task_title);
      mChangeDate = view.findViewById(R.id.edit_task_time);
      mViewdate = view.findViewById(R.id.view_date);
      mSubmit = view.findViewById(R.id.edit_task_submit);
      mCancel = view.findViewById(R.id.edit_task_cancel);
      // Fetch arguments from bundle and set title
      String title = getArguments().getString("title", "Please Enter Task Details");
      getDialog().setTitle(title);
      // Show soft keyboard automatically and request focus to field
      mTitleEditText.requestFocus();
      getDialog().getWindow().setSoftInputMode(
              WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
   }
}
