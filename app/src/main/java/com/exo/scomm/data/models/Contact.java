package com.exo.scomm.data.models;

import java.io.Serializable;

public class Contact implements Serializable {
    public String name;
    public String phoneNumber;
   public boolean joined;

    public Contact() {
    }

    public Contact(String name, String phoneNumber ) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }



    public boolean isJoined() {
        return joined;
    }

    public void setJoined(boolean joined) {
        this.joined = joined;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
