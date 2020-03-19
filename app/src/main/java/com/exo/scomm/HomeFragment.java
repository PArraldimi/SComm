package com.exo.scomm;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.exo.scomm.adapters.TaskAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class HomeFragment extends Fragment {
//    Button buttonHome;
    private TaskAdapter adapter;
    private List<TasksModel> taskList;
    private RecyclerView recyclerView;
    private DatabaseReference taskRef;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        // Inflate the layout for this fragment
        Calendar calendar = Calendar.getInstance();
        recyclerView= view.findViewById(R.id.task_recycler);
        taskList = new ArrayList<>();

        FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert mCurrentUser != null;
        String CurrentUid = mCurrentUser.getUid();

        taskRef = FirebaseDatabase.getInstance().getReference().child("Tasks").child(CurrentUid);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TaskAdapter(getContext(), taskList);
        recyclerView.setAdapter(adapter);

//        buttonHome = view.findViewById(R.id.view_tasks);
        String currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());

        TextView textViewDate = view.findViewById(R.id.text_view_date);
        textViewDate.setText(currentDate);


//        buttonHome.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(getContext(), TaskListActivity.class));
//            }
//        });
        return view;
    }
}
