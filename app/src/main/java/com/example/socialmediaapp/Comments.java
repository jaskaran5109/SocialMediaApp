package com.example.socialmediaapp;

public class Comments
{
    String comment,date,time,userName,profile_picture;
    public Comments(){

    }

    public Comments(String comment, String date, String time, String userName, String profile_picture) {
        this.comment = comment;
        this.date = date;
        this.time = time;
        this.userName = userName;
        this.profile_picture = profile_picture;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProfile_picture() {
        return profile_picture;
    }

    public void setProfile_picture(String profile_picture) {
        this.profile_picture = profile_picture;
    }
}
