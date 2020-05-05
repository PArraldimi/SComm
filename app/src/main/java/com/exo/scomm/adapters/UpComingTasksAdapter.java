package com.exo.scomm.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.exo.scomm.R;
import com.exo.scomm.TaskDetails;
import com.exo.scomm.model.TasksModel;

import java.util.List;

public class UpComingTasksAdapter extends RecyclerView.Adapter<UpComingTasksAdapter.MyViewHolder>{
    private List<TasksModel> tasksModels;
    Context mCntxt;

    public UpComingTasksAdapter(Context context, List<TasksModel> upcomingTasks) {
        this.tasksModels = upcomingTasks;
        this.mCntxt = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.upcoming_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        TasksModel task = tasksModels.get(position);
        final String task_id = task.getTask_id();
        holder.title.setText(task.getTitle());
        holder.type.setText(task.getType());
        holder.date.setText(task.getDate());
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent detailsIntent = new Intent(mCntxt, TaskDetails.class);
                detailsIntent.putExtra("task_id", task_id);
                mCntxt.startActivity(detailsIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasksModels.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, date, type;
        View mView;

        MyViewHolder(View view) {
            super(view);
            this.mView = view;
            title = (TextView) view.findViewById(R.id.upcoming_item_title);
            date = (TextView) view.findViewById(R.id.upcoming_item_date_time);
            type = (TextView) view.findViewById(R.id.upcoming_item_type);
        }
    }
}
