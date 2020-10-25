package com.exo.scomm.ui.fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.exo.scomm.AlarmReceiver;
import com.exo.scomm.R;
import com.exo.scomm.adapters.CompanionsAdapter;
import com.exo.scomm.adapters.TodayTasksAdapter;
import com.exo.scomm.adapters.UpComingTasksAdapter;
import com.exo.scomm.data.models.Task;
import com.exo.scomm.data.models.User;
import com.exo.scomm.ui.activities.Companions;
import com.exo.scomm.utils.TasksViewModel;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;


public class HomeFragment extends Fragment {
    private TodayTasksAdapter todayTasksAdapter;
    private UpComingTasksAdapter upComingTasksAdapter;
    private final List<Task> todayTasks = new ArrayList<>();
    private final List<Task> upcomingTasks = new ArrayList<>();
    private Set<User> companionsList;
    private RecyclerView todayTasksRecycler, companionsRecycler, upComingRecycler;
    private DatabaseReference mRootRef, companionRef, mUsersRef;
    private TextView textViewDate;
    private ImageView seeAllCompanions;
    private String mCurrentUid;
    private Calendar mCalender;

    public HomeFragment() {
    }

    private static boolean isSameDay(Date date1, Date date2) {
        DateTime dateObject1 = DateTime.forInstant(date1.getTime(), TimeZone.getDefault());
        DateTime dateObject2 = DateTime.forInstant(date2.getTime(), TimeZone.getDefault());
        return dateObject1.isSameDayAs(dateObject2);
    }
    private static boolean isSameTime(Date date1, Date date2) {
        DateTime dateObject1 = DateTime.forInstant(date1.getTime(), TimeZone.getDefault());
        DateTime dateObject2 = DateTime.forInstant(date2.getTime(), TimeZone.getDefault());
        return dateObject1.equals(dateObject2);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mCalender = Calendar.getInstance();
        todayTasksRecycler = view.findViewById(R.id.task_recycler);
        companionsRecycler = view.findViewById(R.id.companions_recycler);
        upComingRecycler = view.findViewById(R.id.upcoming_recycler);
        textViewDate = view.findViewById(R.id.text_view_date);
        seeAllCompanions = view.findViewById(R.id.see_all_users);
        companionsList = new HashSet<>();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        linearLayoutManager.setStackFromEnd(false);
        companionsRecycler.setLayoutManager(linearLayoutManager);

        FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert mCurrentUser != null;
        mCurrentUid = mCurrentUser.getUid();
        mUsersRef = mRootRef.child("Users");
        companionRef = mRootRef.child("TaskCompanions");
        setUpLayouts();

        TasksViewModel model = new ViewModelProvider(this).get(TasksViewModel.class);
        model.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
            final String today = new Date().toString();
            todayTasks.clear();
            upcomingTasks.clear();
            // update UI
            for (Task t : tasks
            ) {
                String taskDate = t.getDate();
                if (isSameDay(new Date(today), new Date(taskDate))) {
                    todayTasks.add(t);
                    if (isSameTime(new Date(taskDate), new Date(today) )) {
                        playReminder(t);
                    }
                } else if (new Date(taskDate).after(new Date(today))) {
                    upcomingTasks.add(t);
                } else if (new Date(taskDate).before(new Date(today))) {
                    deleteTask(t);
                }
            }
            sortTasks();
            todayTasksAdapter = new TodayTasksAdapter(getContext(), todayTasks);
            upComingTasksAdapter = new UpComingTasksAdapter(getContext(), upcomingTasks);
            todayTasksRecycler.setAdapter(todayTasksAdapter);
            upComingRecycler.setAdapter(upComingTasksAdapter);
        });

        getTaskCompanions();
        return view;
    }

    private void playReminder(Task task) {
        AlarmManager manager = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);
        Date dat = new Date(task.getDate());
        Calendar cal_alarm = Calendar.getInstance();
        Calendar cal_now = Calendar.getInstance();
        cal_now.setTime(dat);
        cal_alarm.setTime(dat);
        cal_alarm.set(Calendar.HOUR_OF_DAY,dat.getHours());
        cal_alarm.set(Calendar.MINUTE,dat.getMinutes());
        cal_alarm.set(Calendar.SECOND,dat.getSeconds());
        if(cal_alarm.before(cal_now)){
            cal_alarm.add(Calendar.DATE,1);
        }

        Intent myIntent = new Intent(requireContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, myIntent, 0);

        manager.set(AlarmManager.RTC_WAKEUP,cal_alarm.getTimeInMillis(), pendingIntent);
    }

    private void deleteTask(Task t) {
        String deleted_task_id = mRootRef.child("DeletedTask").child(mCurrentUid).push().getKey();
        HashMap<String, Object> deleteTaskMap = new HashMap<>();
        deleteTaskMap.put("Tasks/" + mCurrentUid + "/" + t.getTask_id(), null);
        deleteTaskMap.put("DeletedTask/" + deleted_task_id + "/" + t.getTask_id() + "/", t);
        mRootRef.updateChildren(deleteTaskMap, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                String error = databaseError.getMessage();
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sortTasks() {
        Collections.sort(upcomingTasks, (o1, o2) -> new Date(o1.getDate()).compareTo(new Date(o2.getDate())));
        Collections.sort(todayTasks, (o1, o2) -> new Date(o1.getDate()).compareTo(new Date(o2.getDate())));
    }

    private void getTaskCompanions() {
        final DatabaseReference companionsRef = companionRef.child(mCurrentUid);
        companionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                        final String task_id = childDataSnapshot.getKey();
                        if (task_id != null) {
                            companionsRef.child(task_id).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                                            final String userId = childDataSnapshot.getKey();
                                            mUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (userId != null) {
                                                        User user = dataSnapshot.child(userId).getValue(User.class);
                                                        if (user != null) {
                                                            user.setUID(userId);
                                                            companionsList.add(user);
                                                        }

                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                    CompanionsAdapter adapter = new CompanionsAdapter(getContext(), companionsList);
                    companionsRecycler.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        String currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(mCalender.getTime());
        textViewDate.setText(currentDate);

        seeAllCompanions.setOnClickListener(view -> {
            Intent usersIntent = new Intent(getActivity(), Companions.class);
            startActivity(usersIntent);
        });

    }

    private void setUpLayouts() {
        todayTasksRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        upComingRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
    }
}
