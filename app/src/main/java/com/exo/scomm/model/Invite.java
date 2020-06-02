package com.exo.scomm.model;

public class Invite {
   private String requestType;
   private long date;
   private String accepted;

   public Invite() {
   }

   public String getRequestType() {
      return requestType;
   }

   public void setRequestType(String requestType) {
      this.requestType = requestType;
   }

   public long getDate() {
      return date;
   }

   public void setDate(long date) {
      this.date = date;
   }

   public String getAccepted() {
      return accepted;
   }

   public void setAccepted(String accepted) {
      this.accepted = accepted;
   }
}
