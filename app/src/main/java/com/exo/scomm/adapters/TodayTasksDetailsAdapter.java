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

public class TodayTasksDetailsAdapter extends RecyclerView.Adapter<TodayTasksDetailsAdapter.MyViewHolder>{
    private List<TasksModel> tasksModels;
    private Context context;
    public TodayTasksDetailsAdapter(Context context, List<TasksModel> tasksModelList) {
        this.context = context;
        this.tasksModels = tasksModelList;
    }
    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_detail_item, parent, false);

        return new TodayTasksDetailsAdapter.MyViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        TasksModel task = tasksModels.get(position);
        TasksModel testTask = tasksModels.get(holder.getAdapterPosition());
        DataHolder.setTask(testTask);
        holder.title.setText(task.getTitle());
        holder.type.setText(task.getType());
        holder.date.setText(task.getDate());
        holder.taskOwner.setText("Paul");

    }

    @Override
    public int getItemCount() {
        return tasksModels.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, type, taskOwner;
        View view ;

        MyViewHolder(View view) {
            super(view);
            this.view = view;
            title = (TextView) view.findViewById(R.id.detail_task_item_title);
            date = (TextView) view.findViewById(R.id.detail_task_item_time);
            type = (TextView) view.findViewById(R.id.detail_task_item_type);
            taskOwner = (TextView) view.findViewById(R.id.details_task_item_creator);
        }
    }
}
