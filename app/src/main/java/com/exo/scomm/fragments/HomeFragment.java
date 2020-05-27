package com.exo.scomm.fragments;

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

import com.exo.scomm.R;
import com.exo.scomm.TaskListActivity;

import java.text.DateFormat;
import java.util.Calendar;


public class HomeFragment extends Fragment {
    Button buttonHome;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Calendar calendar = Calendar.getInstance();

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        buttonHome = view.findViewById(R.id.view_tasks);
        String currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());

        TextView textViewDate = view.findViewById(R.id.text_view_date);
        textViewDate.setText(currentDate);


        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), TaskListActivity.class));
            }
        });
        return view;
    }
}
