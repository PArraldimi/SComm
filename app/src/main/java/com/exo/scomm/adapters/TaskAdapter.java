package com.exo.scomm.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.exo.scomm.R;
import com.exo.scomm.TasksModel;

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

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            date = (TextView) view.findViewById(R.id.date_time);
            type = (TextView) view.findViewById(R.id.type);
        }
    }

//    @Override
//    public int getCount() {
//        return taskList.size();
//    }
//
//    @Override
//    public Object getItem(int position) {
//        return taskList.get(position);
//    }
//
//    @Override
//    public long getItemId(int position) {
//        return position;
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//
//        ViewHolder viewHolder;
//
//        if (convertView == null) {
//            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            convertView = inflater.inflate(R.layout.task_item, null, false);
//
//            viewHolder =new ViewHolder(convertView);
//            convertView.setTag(viewHolder);
//        }else{
//            viewHolder = (ViewHolder) convertView.getTag();
//        }
//
//        ViewHolder.class
//
//
//
//        return counterView;
//    }
//
//    private View.OnClickListener onClickListener(final int position) {
//        return  new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        };
//    }





}
