package com.exo.scomm;

import android.content.Context;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.exo.scomm.adapters.CompanionsTasksAdapter;
import com.exo.scomm.adapters.DataHolder;
import com.exo.scomm.adapters.TodayTasksDetailsAdapter;
import com.exo.scomm.model.TasksModel;
import com.exo.scomm.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class TaskDetails extends AppCompatActivity {
    Button deleteTask,  addNewTask;
    RecyclerView myTaskCompanions, todayTasks;
    TextView taskDesc, taskTitle, taskDate, taskType, taskCreator;
    ImageView editTask;
    TodayTasksDetailsAdapter tasksDetailsAdapter;
    private String task_id, mCurrentUID, date, desc, title, type;
    private DatabaseReference taskCompRef;
    private DatabaseReference mRootRef;
    private List<User> taskCompList = new ArrayList<>();
    private List<TasksModel> tasksModelList;
    int lastScrollPosition = 0;
    LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_t_ask_details);
        deleteTask = this.findViewById(R.id.details_delete_task);
        addNewTask = this.findViewById(R.id.details_add_new_task);
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

        taskType.setText(type);
        taskDate.setText(date);
        taskTitle.setText(title);
        taskDesc.setText(desc);

        mCurrentUID = FirebaseAuth.getInstance().getUid();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        taskCompRef = mRootRef.child("TaskCompanions").child(mCurrentUID);
        tasksModelList = DataHolder.getTodayTasks();

        linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        linearLayoutManager.setStackFromEnd(true);
        myTaskCompanions.setLayoutManager(linearLayoutManager);

        tasksDetailsAdapter = new TodayTasksDetailsAdapter(getApplicationContext(), tasksModelList);

        taskCompRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(task_id).exists()) {
                    String taskOwner = Objects.requireNonNull(dataSnapshot.child(task_id).child("taskOwner").getValue()).toString();
                    mRootRef.child("Users").child(taskOwner).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String name = Objects.requireNonNull(dataSnapshot.child("username").getValue()).toString();
                            String status = Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();
                            String image = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();
                            taskCreator.setText(name);
                            User user = new User();
                            user.setUsername(name);
                            user.setImage(image);
                            user.setStatus(status);

                            taskCompList.add(user);
                            populateTaskCompanionsAdapter(TaskDetails.this, taskCompList);

                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {
                    Toast.makeText(TaskDetails.this, "You have no task companions yet", Toast.LENGTH_SHORT).show();
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

    private void populateTaskCompanionsAdapter(Context context, List<User> taskCompanions) {

        if (lastScrollPosition == 0) lastScrollPosition = taskCompanions.size() - 1;
        CompanionsTasksAdapter adapter = new CompanionsTasksAdapter(context, taskCompanions);
        myTaskCompanions.setAdapter(adapter);
        myTaskCompanions.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastScrollPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
            }
        });
        myTaskCompanions.scrollToPosition(lastScrollPosition);
    }

    public void invite(View view) {
        Intent usersIntent = new Intent(TaskDetails.this, AllUsersActivity.class);
        usersIntent.putExtra("task_id", task_id);
        startActivity(usersIntent);
    }

    public void deleteTask(View view) {
        HashMap<String, Object> friendsMap = new HashMap<>();
        friendsMap.put("TaskCompanions/" + mCurrentUID + "/" + task_id + "/", null);
        friendsMap.put("Tasks/" + mCurrentUID + "/" + task_id + "/", null);
        mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
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

    public void addNewTask(View view) {
        Intent intent = new Intent(TaskDetails.this, AddTaskActivity.class);
        startActivity(intent);
    }
}
