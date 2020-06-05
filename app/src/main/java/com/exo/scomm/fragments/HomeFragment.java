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
import com.exo.scomm.TaskDetails;
import com.exo.scomm.adapters.CompanionsAdapter;
import com.exo.scomm.adapters.CompanionsTasksAdapter;
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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;


public class HomeFragment extends Fragment {
   private TodayTasksAdapter todayTasksAdapter;
   private UpComingTasksAdapter upComingTasksAdapter;
   private Set<TasksModel> todayTasks;
   private Set<TasksModel> upcomingTasks;
   private Set<User> companionsList;
   private RecyclerView todayTasksRecycler, companionsRecycler, upComingRecycler;
   private DatabaseReference taskRef;
   private DatabaseReference companionRef;
   private DatabaseReference mUsersRef;
   private TextView textViewDate, seeAllCompanions;
   private String currentUid;
   private Calendar calendar;

   public HomeFragment() {
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
      seeAllCompanions = view.findViewById(R.id.see_all_users);
      todayTasks = new HashSet<>();
      upcomingTasks = new HashSet<>();
      companionsList = new HashSet<>();

      LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
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



      return view;
   }

   private void getTaskCompanions() {
     final DatabaseReference companionsRef = companionRef.child(currentUid);
      companionsRef.addValueEventListener(new ValueEventListener() {
         @Override
         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
           if (dataSnapshot.exists()) {
             for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
               final String task_id = childDataSnapshot.getKey();
               companionsRef.child(task_id).addValueEventListener(new ValueEventListener() {
                 @Override
                 public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                   if (dataSnapshot.exists()) {
                     for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                       final String userId = childDataSnapshot.getKey();
                       mUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                         @Override
                         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                           if (dataSnapshot.hasChild(userId)) {
                             User user = dataSnapshot.child(userId).getValue(User.class);
                             user.setUID(userId);
                             companionsList.add(user);
                             CompanionsAdapter adapter = new CompanionsAdapter(getContext(), companionsList);
                             companionsRecycler.setAdapter(adapter);
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



      seeAllCompanions.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            Intent usersIntent = new Intent(getActivity(), Companions.class);
            startActivity(usersIntent);
         }
      });

      getAllTasks();
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
                  task.setTask_id(taskId);
                  String taskDate = task.getDate();
                  if (isSameDay(new Date(today), new Date(taskDate))) {
                     todayTasks.add(task);
                  } else if (new Date(taskDate).after(new Date(today))) {
                     upcomingTasks.add(task);
                  }
                  todayTasksAdapter = new TodayTasksAdapter(getContext(), todayTasks);
                  upComingTasksAdapter = new UpComingTasksAdapter(getContext(), upcomingTasks);
                  todayTasksRecycler.setAdapter(todayTasksAdapter);
                  upComingRecycler.setAdapter(upComingTasksAdapter);
               }
            }
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
