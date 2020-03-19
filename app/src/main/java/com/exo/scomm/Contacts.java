package com.exo.scomm;

public class Contacts {
    public String username, image, status;

    public Contacts() {
    }

    public Contacts(String username, String image, String status) {
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
}
