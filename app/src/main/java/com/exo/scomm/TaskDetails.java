package com.exo.scomm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.exo.scomm.adapters.CompanionsTasksAdapter;
import com.exo.scomm.adapters.DataHolder;
import com.exo.scomm.adapters.TodayTasksDetailsAdapter;
import com.exo.scomm.fragments.EditTaskDialog;
import com.exo.scomm.model.TasksModel;
import com.exo.scomm.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TaskDetails extends AppCompatActivity {
   Button deleteTask, addNewTask, mInvite, editTask;
   RecyclerView myTaskCompanions, todayTasks;
   TextView taskDesc, taskTitle, taskDate, taskType, taskCreator;
   TodayTasksDetailsAdapter tasksDetailsAdapter;
   private String task_id, mCurrentUID, date, desc, title, type, owner;
   private DatabaseReference taskCompRef;
   private DatabaseReference mRootRef, mUsersRef;
   private List<User> taskCompList = new ArrayList<>();
   private List<TasksModel> tasksModelList;
   private TasksModel task;
   int lastScrollPosition = 0;
   LinearLayoutManager linearLayoutManager;
   int mDeleteBnState;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_task_details);
      deleteTask = this.findViewById(R.id.details_delete_task);
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
      date = getIntent().getStringExtra("date");
      desc = getIntent().getStringExtra("desc");
      title = getIntent().getStringExtra("title");
      type = getIntent().getStringExtra("type");
      owner = getIntent().getStringExtra("owner");
      task = new TasksModel(task_id, date, desc, title, type, owner);
      tasksModelList = DataHolder.getTodayTasks();
      mDeleteBnState = 0;
      mRootRef = FirebaseDatabase.getInstance().getReference();
      mUsersRef = mRootRef.child("Users");
      taskCompRef = mRootRef.child("TaskCompanions");
      linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
      linearLayoutManager.setStackFromEnd(true);
      myTaskCompanions.setLayoutManager(linearLayoutManager);
      tasksDetailsAdapter = new TodayTasksDetailsAdapter(getApplicationContext(), tasksModelList);
      mCurrentUID = FirebaseAuth.getInstance().getUid();

      taskType.setText(type);
      taskDate.setText(date);
      taskTitle.setText(title);
      taskDesc.setText(desc);
      getTaskOwner(owner);

      if (!owner.equals(mCurrentUID)) {
         deleteTask.setText("SchommOut");
         mDeleteBnState = 1;
         editTask.setVisibility(View.GONE);
      }
      editTask.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            editTask(task_id, owner);
         }
      });

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
               deleteTask();
            }
            if (mDeleteBnState == 1) {
               schommeOut();
            }
         }
      });
   }

   private void editTask(String task_id, String owner) {
      FragmentManager fm = getSupportFragmentManager();
      EditTaskDialog editNameDialogFragment = EditTaskDialog.newInstance("Change Task Details");
      editNameDialogFragment.show(fm, "fragment_edit_task");
   }

   private void editTaskDescription(String task_id, String owner) {
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
      mRootRef.child("TaskCompanions/" + mCurrentUID + "/" + owner + "/" + "task_id").setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
         @Override
         public void onComplete(@NonNull Task<Void> task) {
            if (task.isSuccessful()) {
               Toast.makeText(TaskDetails.this, "You opted out of the task successfully", Toast.LENGTH_SHORT).show();
            }
         }
      });
   }

   private void getTaskCompanions() {
      taskCompRef.child(task_id).addValueEventListener(new ValueEventListener() {
         @Override
         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
               final String uid = childDataSnapshot.getKey();
               mUsersRef.child(uid).addValueEventListener(new ValueEventListener() {
                  @Override
                  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                     String name = Objects.requireNonNull(dataSnapshot.child("username").getValue()).toString();
                     String status = Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();
                     String image = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();
                     User user = new User();
                     user.setUID(uid);
                     user.setUsername(name);
                     user.setImage(image);
                     user.setStatus(status);
                     taskCompList.add(user);
                  }

                  @Override
                  public void onCancelled(@NonNull DatabaseError databaseError) {

                  }
               });
               CompanionsTasksAdapter adapter = new CompanionsTasksAdapter(TaskDetails.this, taskCompList, task_id);
               myTaskCompanions.setAdapter(adapter);

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

   }

//    private void populateTaskCompanionsAdapter(Context context, List<User> taskCompanions) {
//
//        if (lastScrollPosition == 0) lastScrollPosition = taskCompanions.size() - 1;
//
//        myTaskCompanions.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                lastScrollPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
//            }
//        });
//        myTaskCompanions.scrollToPosition(lastScrollPosition);
//    }

   public void invite(View view) {
      Intent usersIntent = new Intent(TaskDetails.this, AllUsersActivity.class);
      usersIntent.putExtra("task_id", task_id);
      usersIntent.putExtra("fromTaskDetails", "1");
      startActivity(usersIntent);
   }

   public void deleteTask() {
      String noteKey = mRootRef.child("Notifications").child(mCurrentUID).push().getKey();
      String deleted_tas_id = mRootRef.child("DeletedTask").child(mCurrentUID).push().getKey();
      for (User user : taskCompList
      ) {

         String userId = user.getUID();
         Map recipientNote = new HashMap<>();
         recipientNote.put("user", mCurrentUID);
         recipientNote.put("type", "deleteTask");
         recipientNote.put("task_id", task_id);
         recipientNote.put("date", ServerValue.TIMESTAMP);

         Map senderNote = new HashMap<>();
         senderNote.put("user", userId);
         senderNote.put("type", "deleteTask");
         senderNote.put("task_id", task_id);
         senderNote.put("date", ServerValue.TIMESTAMP);

         HashMap<String, Object> deleteTaskMap = new HashMap<>();
         deleteTaskMap.put("TaskCompanions/" + mCurrentUID + "/" + userId + "/" + "task_id", null);
         deleteTaskMap.put("TaskCompanions/" + userId + "/" + mCurrentUID + "/" + "task_id", null);
         deleteTaskMap.put("Notifications/" + userId + "/" + noteKey, recipientNote);
         deleteTaskMap.put("Notifications/" + mCurrentUID + "/" + noteKey, senderNote);
         deleteTaskMap.put("Tasks/" + mCurrentUID + "/" + task_id, null);
         deleteTaskMap.put("Tasks/" + userId + "/" + task_id, null);
         deleteTaskMap.put("DeletedTask/" + task_id + "/", task);

         mRootRef.updateChildren(deleteTaskMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
               if (databaseError == null) {
                  Toast.makeText(TaskDetails.this, "Task deleted successfully", Toast.LENGTH_SHORT).show();
                  taskType.setText("");
                  taskDate.setText("");
                  taskTitle.setText("");
                  taskDesc.setText("");
               } else {
                  String error = databaseError.getMessage();
                  Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
               }
            }
         });
      }

   }


   public void addNewTask(View view) {
      Intent intent = new Intent(TaskDetails.this, AddTaskActivity.class);
      startActivity(intent);
   }
}
