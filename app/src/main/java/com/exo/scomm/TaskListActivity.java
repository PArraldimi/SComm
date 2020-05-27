<<<<<<< HEAD
package com.exo.scomm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class TaskListActivity extends AppCompatActivity {

    private RecyclerView TaskListRecyclerList;
    private DatabaseReference TaskRef;
    List<TasksModel> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        taskList = new ArrayList<>();

        FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert mCurrentUser != null;
        String CurrentUid = mCurrentUser.getUid();

        TaskRef = FirebaseDatabase.getInstance().getReference().child("Tasks").child(CurrentUid);

        TaskListRecyclerList = findViewById(R.id.task_list_recycler_list);
        TaskListRecyclerList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<TasksModel> options_task =
                new FirebaseRecyclerOptions.Builder<TasksModel>()
                .setQuery(TaskRef, TasksModel.class)
                .build();

        FirebaseRecyclerAdapter<TasksModel, TaskViewHolder> adapter_task =
                new FirebaseRecyclerAdapter<TasksModel, TaskViewHolder>(options_task) {
                    @Override
                    protected void onBindViewHolder(@NonNull TaskViewHolder holder, int position, @NonNull TasksModel model) {
                        holder.mTitle.setText(model.getTitle());
                        holder.mType.setText(model.getType());
                        holder.mDate.setText(model.getDate());
                    }


                    @NonNull
                    @Override
                    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
                        return new TaskViewHolder(view);
                    }
                };

        TaskListRecyclerList.setAdapter(adapter_task);

        adapter_task.startListening();
    }


    public static class TaskViewHolder extends RecyclerView.ViewHolder
    {
        TextView mType, mTitle, mDate;
        TaskViewHolder(@NonNull View itemView)
        {
            super(itemView);

            mType = itemView.findViewById(R.id.type);
            mTitle = itemView.findViewById(R.id.title);
            mDate = itemView.findViewById(R.id.date_time);
        }
    }
}
=======
package com.exo.scomm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.exo.scomm.adapters.TaskAdapter;
import com.exo.scomm.model.Task;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class TaskListActivity extends AppCompatActivity {

    private RecyclerView TaskListRecyclerList;
    private DatabaseReference TaskRef;
    List<Task> taskList;
    TaskAdapter taskAdapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);



        taskList = new ArrayList<>();

        getAlltasks();

        FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert mCurrentUser != null;
        String CurrentUid = mCurrentUser.getUid();
        TaskRef = FirebaseDatabase.getInstance().getReference().child("Tasks").child(CurrentUid);


        TaskListRecyclerList = findViewById(R.id.task_recycler);
        TaskListRecyclerList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL,false));
        taskAdapter = new TaskAdapter(this, taskList);
        TaskListRecyclerList.setAdapter(taskAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Task> options_task =
                new FirebaseRecyclerOptions.Builder<Task>()
                .setQuery(TaskRef, Task.class)
                .build();

        FirebaseRecyclerAdapter<Task, TaskViewHolder> adapter_task =
                new FirebaseRecyclerAdapter<Task, TaskViewHolder>(options_task) {
                    @Override
                    protected void onBindViewHolder(@NonNull TaskViewHolder holder, int position, @NonNull Task model)
                    {
                        holder.mTitle.setText(model.getTitle());
                        holder.mType.setText(model.getType());
                        holder.mDate.setText(model.getDate());
                    }

                    @NonNull
                    @Override
                    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
                        return new TaskViewHolder(view);
                    }
                };

        TaskListRecyclerList.setAdapter(adapter_task);

        adapter_task.startListening();
    }


    public static class TaskViewHolder extends RecyclerView.ViewHolder
    {
        TextView mType, mTitle, mDate;
        TaskViewHolder(@NonNull View itemView)
        {
            super(itemView);

            mType = itemView.findViewById(R.id.type);
            mTitle = itemView.findViewById(R.id.title);
            mDate = itemView.findViewById(R.id.date_time);
        }
    }

    private void getAlltasks() {
        //---Your Reference to the bookList---\\

        TaskRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final com.google.firebase.database.DataSnapshot dataSnapshot) {
                taskList.clear(); // ArrayList<Pojo/Object> \\

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    String title = postSnapshot.child("title").getValue(String.class);
                    String date = postSnapshot.child("date").getValue(String.class);
                    String type = postSnapshot.child("type").getValue(String.class);
                    String owner = postSnapshot.child("type").getValue(String.class);
                    String description = postSnapshot.child("type").getValue(String.class);



                    //Use the dataType you are using and also use the reference of those childs inside arrays\\

                    // Putting Data into Getter Setter \\
                    Task task = new Task();
                    task.setTitle(title);
                    task.setTitle(date);
                    task.setDescription(description);
                    task.setTaskOwner(owner);
                    task.setType(type);

                    taskList.add(task);

                }

                if (taskList.size() == 0) {
//                    StaticMethods.customSnackBar(quoteRequestLv, "Your alert!",
//                            getResources().getColor(R.color.colorPrimaryDark),
//                            getResources().getColor(R.color.color_white), 3000);
                }

                //---Initialize your adapter as you have fetched the data---\\
                TaskAdapter taskAdapter = new TaskAdapter(TaskListActivity.this, taskList);
                TaskListRecyclerList.setAdapter(taskAdapter);
//
//                dismissDialog();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
>>>>>>> latest-mark
