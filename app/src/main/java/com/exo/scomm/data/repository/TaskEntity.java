package com.exo.scomm.data.repository;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
class TaskEntity {
   private String date;
   private String description;
   private String taskOwner;
   private String title;
   private String type;
   private String task_id;

   public TaskEntity() {
   }

   public String getDate() {
      return date;
   }

   public void setDate(String date) {
      this.date = date;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public String getTaskOwner() {
      return taskOwner;
   }

   public void setTaskOwner(String taskOwner) {
      this.taskOwner = taskOwner;
   }

   public String getTitle() {
      return title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public String getType() {
      return type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public String getTask_id() {
      return task_id;
   }

   public void setTask_id(String task_id) {
      this.task_id = task_id;
   }
}
