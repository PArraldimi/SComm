package com.exo.scomm.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.exo.scomm.R;
import com.exo.scomm.model.TasksModel;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.MyViewHolder> {
    private Context context;
    private List<TasksModel> taskList;

    public TaskAdapter(Context context, List<TasksModel> taskList) {
        super();
        this.context = context;
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskAdapter.MyViewHolder holder, int position) {
        TasksModel task = taskList.get(position);
        holder.title.setText(task.getTitle());
        holder.type.setText(task.getType());
        holder.date.setText(task.getDate());

    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, date, type;

        MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.task_item_title);
            date = view.findViewById(R.id.task_item_date_time);
            type = view.findViewById(R.id.task_item_type);
        }
    }
}

