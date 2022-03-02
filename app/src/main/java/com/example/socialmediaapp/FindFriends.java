package com.example.socialmediaapp;

public class FindFriends
{
    public String profile_picture,fullName,status;

    public FindFriends(String profile_picture, String fullName, String status) {
        this.profile_picture = profile_picture;
        this.fullName = fullName;
        this.status = status;
    }

    public FindFriends(){}
    public String getProfile_picture() {
        return profile_picture;
    }

    public void setProfile_picture(String profile_picture) {
        this.profile_picture = profile_picture;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
