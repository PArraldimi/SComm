package com.exo.scomm.model;

import java.io.Serializable;

public class User implements Serializable {
    private String username;
    private String image;
    private String status;
    private String UID;

    public User() {
    }

    public User(String username, String image, String status) {
        this.username = username;
        this.image = image;
        this.status = status;
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
