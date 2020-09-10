package com.exo.scomm.data.repository;

import com.exo.scomm.data.models.Task;
import com.exo.scomm.utils.mapper.FirebaseMapper;

class TaskMapper extends FirebaseMapper< TaskEntity, Task> {

   @Override
   public Task map(TaskEntity taskEntity) {
      Task model = new Task();
      model.setDate(taskEntity.getDate());
      model.setDescription(taskEntity.getDescription());
      model.setTask_id(taskEntity.getTask_id());
      model.setType(taskEntity.getType());
      model.setTitle(taskEntity.getTitle());
      model.setTaskOwner(taskEntity.getTaskOwner());
      return model;
   }
}
