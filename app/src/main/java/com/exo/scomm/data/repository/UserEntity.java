package com.exo.scomm.data.repository;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class UserEntity {
    private String username;
    private String image;
    private String status;
    private String UID;
    private String phone;

    public UserEntity() {
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }
}
