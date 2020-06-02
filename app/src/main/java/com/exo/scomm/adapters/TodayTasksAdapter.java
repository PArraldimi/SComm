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

public class TodayTasksAdapter extends RecyclerView.Adapter<TodayTasksAdapter.MyViewHolder> {
   private List<TasksModel> taskList;
   private Context mCntxt;

   public TodayTasksAdapter(Context mCntxt, List<TasksModel> taskList) {
      super();
      this.mCntxt = mCntxt;
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
   public void onBindViewHolder(@NonNull final TodayTasksAdapter.MyViewHolder holder, int position) {
      final TasksModel task = taskList.get(position);
      final String task_id = task.getTask_id();
      holder.title.setText(task.getTitle());
      holder.type.setText(task.getType());
      holder.date.setText(task.getDate());


      holder.view.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            Intent detailsIntent = new Intent(mCntxt, TaskDetails.class);
            detailsIntent.putExtra("task_id", task_id);
            detailsIntent.putExtra("title", task.getTitle());
            detailsIntent.putExtra("desc", task.getDescription());
            detailsIntent.putExtra("date", task.getDate());
            detailsIntent.putExtra("owner", task.getTaskOwner());
            DataHolder.setTodayTasks(taskList);
            mCntxt.startActivity(detailsIntent);
         }
      });

   }

   @Override
   public int getItemCount() {
      return taskList.size();
   }

   public static class MyViewHolder extends RecyclerView.ViewHolder {
      public TextView title, date, type;
      View view;

      MyViewHolder(View view) {
         super(view);
         this.view = view;
         title = (TextView) view.findViewById(R.id.task_item_title);
         date = (TextView) view.findViewById(R.id.task_item_date_time);
         type = (TextView) view.findViewById(R.id.task_item_type);
      }
   }

}
