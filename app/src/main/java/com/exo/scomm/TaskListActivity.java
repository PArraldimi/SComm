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
