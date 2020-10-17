package com.exo.scomm.data.models;

import java.io.Serializable;

public class User implements Serializable {
    private String id;
    private String username;
    private String image;
    private String status;
    private String UID;
    private String phone;
    private String device_token;

    public User() {
    }

    public User(String id, String username, String image, String status, String UID, String phone, String device_token) {
        this.id = id;
        this.username = username;
        this.image = image;
        this.status = status;
        this.UID = UID;
        this.phone = phone;
        this.device_token = device_token;
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

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
