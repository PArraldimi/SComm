package com.exo.scomm.fragments;

import android.content.Context;
import android.content.Intent;
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

import com.exo.scomm.AddTaskActivity;
import com.exo.scomm.AllUsersActivity;
import com.exo.scomm.Companions;
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
  private UpComingTasksAdapter upComingTasksAdapter;
  private List<TasksModel> todayTasks;
  private List<TasksModel> upcomingTasks;
  private List<User> companionsList;
  private RecyclerView todayTasksRecycler, companionsRecycler, upComingRecycler;
  private DatabaseReference taskRef;
  private DatabaseReference companionRef;
  private DatabaseReference mUsersRef;
  private TextView textViewDate, seeAllUsers;
  private String currentUid;
  private int lastScrollPosition = 0;
  private LinearLayoutManager linearLayoutManager;
  OnDataPass dataPasser;

  private Calendar calendar;

  public HomeFragment() {
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    dataPasser = (OnDataPass) context;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_home, container, false);
    calendar = Calendar.getInstance();
    todayTasksRecycler = view.findViewById(R.id.task_recycler);
    companionsRecycler = view.findViewById(R.id.companions_recycler);
    upComingRecycler = view.findViewById(R.id.upcoming_recycler);
    textViewDate = view.findViewById(R.id.text_view_date);
    List<TasksModel> taskList = new ArrayList<>();
    TextView seeAllCompanions = view.findViewById(R.id.see_all_users);
    todayTasks = new ArrayList<>();
    upcomingTasks = new ArrayList<>();
    companionsList = new ArrayList<>();

    linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
    linearLayoutManager.setStackFromEnd(false);
    companionsRecycler.setLayoutManager(linearLayoutManager);

    FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
    assert mCurrentUser != null;
    currentUid = mCurrentUser.getUid();
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    taskRef = mRootRef.child("Tasks").child(currentUid);
    mUsersRef = mRootRef.child("Users");
    companionRef = mRootRef.child("TaskCompanions");
    setUpLayouts();

    getTaskCompanions();

    seeAllCompanions.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent usersIntent = new Intent(getActivity(), Companions.class);
        startActivity(usersIntent);
      }
    });

    getAllTasks();

    return view;
  }

  private void getTaskCompanions() {
    companionRef.child(currentUid).addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
          final String uid = childDataSnapshot.getKey();
          Log.e("User Key", uid);

          mUsersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              String name = Objects.requireNonNull(dataSnapshot.child("username").getValue()).toString();
              String status = Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();
              String image = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();

              User user = new User();
              user.setUsername(name);
              user.setImage(image);
              user.setUID(uid);
              user.setStatus(status);
              companionsList.add(user);
              CompanionsAdapter adapter = new CompanionsAdapter(getContext(), companionsList,dataPasser);
              companionsRecycler.setAdapter(adapter);
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

  @Override
  public void onStart() {
    super.onStart();
    String currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
    textViewDate.setText(currentDate);
  }

  private void setUpLayouts() {
    todayTasksRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
    upComingRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
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
              todayTasks.add(tasksModel);
            } else if (new Date(taskDate).after(new Date(today))) {
              upcomingTasks.add(tasksModel);
            }
            todayTasksAdapter = new TodayTasksAdapter(getContext(), todayTasks);
            upComingTasksAdapter = new UpComingTasksAdapter(getContext(), upcomingTasks);
            todayTasksRecycler.setAdapter(todayTasksAdapter);
            upComingRecycler.setAdapter(upComingTasksAdapter);
          }
        }
        DataHolder.setTodayTasks(todayTasks);
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

  public interface OnDataPass {
  }


}
