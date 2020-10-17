package com.exo.scomm.data.models;

public class Chatlist {
    private String id;
    private String name;
    private String text;
    private  String date;

    public Chatlist() {
    }

    public Chatlist(String id, String name, String text, String date) {
        this.id = id;
        this.name = name;
        this.text = text;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
