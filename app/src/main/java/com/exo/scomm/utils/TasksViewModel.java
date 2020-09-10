package com.exo.scomm.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.exo.scomm.data.repository.FirebaseDatabaseRepository;
import com.exo.scomm.data.models.Task;
import com.exo.scomm.data.repository.TaskRepository;

import java.util.List;

public class TasksViewModel extends ViewModel {

   private MutableLiveData<List<Task>> mAllTasks;

   public LiveData<List<Task>> getAllTasks() {
      if (mAllTasks == null) {
         mAllTasks = new MutableLiveData<>();
         new TaskRepository().addListener(new FirebaseDatabaseRepository.FirebaseDatabaseRepositoryCallback() {
            @Override
            public void onSuccess(List result) {
               mAllTasks.setValue(result);
            }
            @Override
            public void onError(Exception e) {
               mAllTasks.setValue(null);
            }
         });
      }
      return mAllTasks;
   }
   public void addTask(){

   }
}
