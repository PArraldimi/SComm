package com.exo.scomm.data.repository;

import com.exo.scomm.data.models.Task;
import com.google.firebase.auth.FirebaseAuth;

public class TaskRepository extends FirebaseDatabaseRepository<Task> {

   public TaskRepository() {
      super(new TaskMapper());
   }

   @Override
   protected String getRootNode() {
      return "Tasks"+"/"+FirebaseAuth.getInstance().getUid();
   }
}
