package com.exo.scomm.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.exo.scomm.R;
import com.exo.scomm.adapters.CompanionsAdapter;
import com.exo.scomm.adapters.DataHolder;
import com.exo.scomm.adapters.TodayTasksAdapter;
import com.exo.scomm.adapters.UpComingTasksAdapter;
import com.exo.scomm.model.TasksModel;
import com.exo.scomm.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;


public class HomeFragment extends Fragment {
    private TodayTasksAdapter todayTasksAdapter;
    private CompanionsAdapter companionsAdapter;
    private UpComingTasksAdapter upComingTasksAdapter;
    private List<TasksModel> taskList, todaysTasks, upcomingTasks;
    private List<User> companionsList;
    private RecyclerView todaysTasksRecycler, companionsRecycler, upComingRecycler;
    private DatabaseReference taskRef;
    private DatabaseReference companionRef, taskCompRef, mRootRef;
    private DatabaseReference usersRef;
    TextView textViewDate;
    private String currentUid;
    Calendar calendar;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        calendar = Calendar.getInstance();
        todaysTasksRecycler = view.findViewById(R.id.task_recycler);
        companionsRecycler = view.findViewById(R.id.companions_recycler);
        upComingRecycler = view.findViewById(R.id.upcoming_recycler);
        textViewDate = view.findViewById(R.id.text_view_date);
        taskList = new ArrayList<>();
        todaysTasks = new ArrayList<>();
        upcomingTasks = new ArrayList<>();
        companionsList = new ArrayList<>();
        FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert mCurrentUser != null;
        currentUid = mCurrentUser.getUid();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        taskRef = mRootRef.child("Tasks").child(currentUid);
        companionRef = FirebaseDatabase.getInstance().getReference().child("Companions");
        taskCompRef = mRootRef.child("task_companions").child(currentUid);

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        getAllTasks();
        String currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
        textViewDate.setText(currentDate);
        todaysTasksRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        upComingRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        companionsRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        companionRef.child(currentUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    String uid = childDataSnapshot.getKey();
                    Log.e("User Key", uid);
                    usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                    usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String name = Objects.requireNonNull(dataSnapshot.child("username").getValue()).toString();
                            String status = Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();
                            String image = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();

                            User user = new User();
                            user.setUsername(name);
                            user.setImage(image);
                            user.setStatus(status);

                            companionsList.add(user);
                            companionsAdapter = new CompanionsAdapter(getContext(), companionsList);
                            companionsRecycler.setAdapter(companionsAdapter);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getAllTasks() {
        final String today = new Date().toString();
        taskRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                        String taskId = childDataSnapshot.getKey().toString();
                        TasksModel task = childDataSnapshot.getValue(TasksModel.class);
                        assert task != null;
                        TasksModel tasksModel = new TasksModel();
                        tasksModel.setDate(task.getDate());
                        tasksModel.setTitle(task.getTitle());
                        tasksModel.setType(task.getType());
                        tasksModel.setDescription(task.getDescription());
                        tasksModel.setTaskOwner(task.getTaskOwner());
                        tasksModel.setTask_id(taskId);
                        String taskDate = task.getDate();
                        if (isSameDay(new Date(today), new Date(taskDate))) {
                            todaysTasks.add(tasksModel);
                        } else if (new Date(taskDate).after(new Date(today))) {
                            upcomingTasks.add(tasksModel);
                        }
                        todayTasksAdapter = new TodayTasksAdapter(getContext(), todaysTasks);
                        upComingTasksAdapter = new UpComingTasksAdapter(getContext(), upcomingTasks);
                        todaysTasksRecycler.setAdapter(todayTasksAdapter);
                        upComingRecycler.setAdapter(upComingTasksAdapter);
                    }
                }
                DataHolder.setTodayTasks(todaysTasks);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private static boolean isSameDay(Date date1, Date date2) {
        DateTime dateObject1 = DateTime.forInstant(date1.getTime(), TimeZone.getDefault());
        DateTime dateObject2 = DateTime.forInstant(date2.getTime(), TimeZone.getDefault());
        return dateObject1.isSameDayAs(dateObject2);
    }
}
