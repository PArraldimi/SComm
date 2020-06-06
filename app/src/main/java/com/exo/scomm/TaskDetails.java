package com.exo.scomm;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.exo.scomm.adapters.CompanionsTasksAdapter;
import com.exo.scomm.adapters.DataHolder;
import com.exo.scomm.adapters.TodayTasksDetailsAdapter;
import com.exo.scomm.fragments.EditTaskDialog;
import com.exo.scomm.fragments.HomeFragment;
import com.exo.scomm.model.TasksModel;
import com.exo.scomm.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class TaskDetails extends AppCompatActivity implements EditTaskDialog.EditTaskDialogResultListener {
   Button deleteTask, addNewTask, mInvite, editTask;
   RecyclerView myTaskCompanions;
   TextView taskDesc, taskTitle, taskDate, taskType, taskCreator, mTextViewDate;
   ImageView mScommingDetails;
   private String task_id, mCurrentUID, mDate, mDesc, mTitle, mType, owner;
   private DatabaseReference taskCompRef, mTaskRef;
   private DatabaseReference mRootRef, mUsersRef;
   private Set<User> taskCompList = new HashSet<>();
   private TasksModel task;
   LinearLayoutManager linearLayoutManager;
   int mDeleteBnState;
   private Calendar calendar;
   EditTaskDialog dialog;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_task_details);
      MaterialToolbar toolbar = findViewById(R.id.task_details_app_bar);
      toolbar.setTitle("Task Details");
      setSupportActionBar(toolbar);
      toolbar.setNavigationOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            onBackPressed();  // byDefault provided backPressed method, or handle your own way
         }
      });



      deleteTask = this.findViewById(R.id.details_delete_task);
      calendar = Calendar.getInstance();
      mTextViewDate = this.findViewById(R.id.details_today_date_time);
      addNewTask = this.findViewById(R.id.details_add_new_task);
      mInvite = this.findViewById(R.id.details_invite);
      taskDesc = this.findViewById(R.id.details_task_desc);
      taskTitle = findViewById(R.id.detail_task_item_title);
      taskDate = findViewById(R.id.detail_task_item_time);
      taskType = findViewById(R.id.detail_task_item_type);
      taskCreator = findViewById(R.id.details_task_item_creator);
      myTaskCompanions = this.findViewById(R.id.task_details_companions_recycler);
      editTask = this.findViewById(R.id.task_details_edit);
      task_id = getIntent().getStringExtra("task_id");
      owner = getIntent().getStringExtra("owner");

      dialog = new EditTaskDialog();

      mDeleteBnState = 0;
      mRootRef = FirebaseDatabase.getInstance().getReference();
      mUsersRef = mRootRef.child("Users");
      taskCompRef = mRootRef.child("TaskCompanions");
      mTaskRef = mRootRef.child("Tasks");
      linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
      linearLayoutManager.setStackFromEnd(true);
      myTaskCompanions.setLayoutManager(linearLayoutManager);
      mCurrentUID = FirebaseAuth.getInstance().getUid();


      getTaskOwner(owner);

      if (!owner.equals(mCurrentUID)) {
         deleteTask.setText("SchommOut");
         mDeleteBnState = 1;
         editTask.setVisibility(View.GONE);
      }


      mRootRef.child("TaskSupers").child(task_id).addListenerForSingleValueEvent(new ValueEventListener() {
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

      getTaskCompanions();

      deleteTask.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {

            if (mDeleteBnState == 0) {
               Toast.makeText(TaskDetails.this, "Deleting the task", Toast.LENGTH_SHORT).show();
               deleteTask(task);
               Toast.makeText(TaskDetails.this, " Task deleted successfully", Toast.LENGTH_SHORT).show();
            }
            if (mDeleteBnState == 1) {
               schommeOut();
            }
         }
      });

      mScommingDetails = this.findViewById(R.id.scomming_details);
      mScommingDetails.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                    TaskDetails.this, R.style.BottomSheetDialogTheme
            );
            View bottomSheetView = LayoutInflater.from(getApplicationContext())
                    .inflate(
                            R.layout.scomming_details_layout,
                            (LinearLayout)findViewById(R.id.scomming_detail)
                    );
//            Functionality of bottomSheetView(Listing Scommers)
            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();
         }
      });
   }

   private void editTask(TasksModel task) {
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
      schommOutMap.put("TaskCompanions/" + mCurrentUID + "/" + task_id + "/", null);
      schommOutMap.put("Tasks/" + mCurrentUID + "/" + task_id + "/", null);
      mRootRef.updateChildren(schommOutMap, new DatabaseReference.CompletionListener() {
         @Override
         public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
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
         }
      });
   }

   private void getTaskCompanions() {
      final ProgressDialog progressDialog = new ProgressDialog(this);
      progressDialog.setTitle("Fetching companions");
      progressDialog.setMessage("Please wait!!");
      progressDialog.setCanceledOnTouchOutside(false);
      progressDialog.show();

      final DatabaseReference companionsRef = taskCompRef.child(mCurrentUID).child(task_id);

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
                        if (dataSnapshot.hasChild(userId)) {
                           User user = dataSnapshot.child(userId).getValue(User.class);
                           assert user != null;
                           user.setUID(userId);
                           taskCompList.add(user);
                           CompanionsTasksAdapter adapter = new CompanionsTasksAdapter(TaskDetails.this, taskCompList, task_id);
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

      mTaskRef.child(owner).child(task_id).addListenerForSingleValueEvent(new ValueEventListener() {
         @Override
         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.hasChildren()) {
               mTitle = Objects.requireNonNull(dataSnapshot.child("title").getValue()).toString();
               mDate = Objects.requireNonNull(dataSnapshot.child("date").getValue()).toString();
               mDesc = Objects.requireNonNull(dataSnapshot.child("description").getValue()).toString();
               mType = Objects.requireNonNull(dataSnapshot.child("type").getValue()).toString();

               task = new TasksModel(task_id, mDate, mDesc, mTitle, mType, owner);
               taskType.setText(mType);
               taskDate.setText(mDate);
               taskTitle.setText(mTitle);
               taskDesc.setText(mDesc);

               editTask.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                     editTask(task);
                  }
               });
            }
         }

         @Override
         public void onCancelled(@NonNull DatabaseError databaseError) {

         }
      });


   }

   @Override
   protected void onResume() {
      super.onResume();
   }

   public void invite(View view) {
      Intent usersIntent = new Intent(TaskDetails.this, AllUsersActivity.class);
      usersIntent.putExtra("task_id", task_id);
      usersIntent.putExtra("fromTaskDetails", "1");
      startActivity(usersIntent);
   }

   public void deleteTask(TasksModel tasksModel) {
      String noteKey = mRootRef.child("Notifications").child(mCurrentUID).push().getKey();
      String deleted_task_id = mRootRef.child("DeletedTask").child(mCurrentUID).push().getKey();

      if (taskCompList.size() == 0 || tasksModel.getType().equals("Private")) {
         deletePrivateTask(noteKey, deleted_task_id, mCurrentUID);
      } else {
         for (User user : taskCompList
         ) {
            deletePublicTask(noteKey, deleted_task_id, user);
         }
      }
   }

   private void deletePublicTask(String noteKey, String deleted_task_id, User user) {
      Map recipientNote = getSenderNoteMap(user.getUID());
      Map senderNote = getSenderNoteMap(mCurrentUID);
      HashMap<String, Object> deleteTaskMap = new HashMap<>();
      deleteTaskMap.put("TaskCompanions/" + mCurrentUID + "/" + user.getUID() + "/" + "task_id", null);
      deleteTaskMap.put("TaskCompanions/" + user.getUID() + "/" + mCurrentUID + "/" + "task_id", null);
      deleteTaskMap.put("Notifications/" + user.getUID() + "/" + noteKey, recipientNote);
      deleteTaskMap.put("Notifications/" + mCurrentUID + "/" + noteKey, senderNote);
      deleteTaskMap.put("Tasks/" + mCurrentUID + "/" + task_id, null);
      deleteTaskMap.put("Tasks/" + user.getUID() + "/" + task_id, null);
      deleteTaskMap.put("DeletedTask/" + deleted_task_id + "/" + task_id + "/", task);

      performDelete(deleteTaskMap);
   }

   private void performDelete(HashMap<String, Object> deleteTaskMap) {
      mRootRef.updateChildren(deleteTaskMap, new DatabaseReference.CompletionListener() {
         @Override
         public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
            if (databaseError == null) {
               Toast.makeText(TaskDetails.this, "Task deleted successfully", Toast.LENGTH_SHORT).show();
               taskType.setText("");
               taskDate.setText("");
               taskTitle.setText("");
               taskDesc.setText("");
               taskCreator.setText("");
            } else {
               String error = databaseError.getMessage();
               Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
            }
         }
      });
   }

   private void deletePrivateTask(String noteKey, String deleted_task_id, String mCurrentUID) {
      HashMap<String, Object> deleteTaskMap = new HashMap<>();
      Map senderNote = getSenderNoteMap(mCurrentUID);
      deleteTaskMap.put("Notifications/" + mCurrentUID + "/" + noteKey, senderNote);
      deleteTaskMap.put("Tasks/" + mCurrentUID + "/" + task_id, null);
      deleteTaskMap.put("DeletedTask/" + deleted_task_id + "/" + task_id + "/", task);
      performDelete(deleteTaskMap);
   }

   private Map getSenderNoteMap(String mCurrentUID) {
      HashMap<String, Object> senderNote = new HashMap<>();
      senderNote.put("user", mCurrentUID);
      senderNote.put("type", "deleteTask");
      senderNote.put("task_id", task_id);
      senderNote.put("date", ServerValue.TIMESTAMP);
      return senderNote;
   }

   public void addNewTask(View view) {
      Intent intent = new Intent(TaskDetails.this, AddTaskActivity.class);
      startActivity(intent);
   }

   @Override
   public void onFinishTaskEditDialog(TasksModel task) {
      taskType.setText(task.getType());
      taskDate.setText(task.getDate());
      taskTitle.setText(task.getTitle());
      taskDesc.setText(task.getDescription());
   }

   @Override
   public boolean onOptionsItemSelected(@NonNull MenuItem item) {
      switch (item.getItemId()) {
         // Respond to the action bar's Up/Home button
         case android.R.id.home:
            return true;
      }

      return super.onOptionsItemSelected(item);
   }
}
