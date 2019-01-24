package com.github.neone35.ageversary.pojo;

import org.parceler.Parcel;

@Parcel
public class User {

    String birthDate;
    String photoUrl;
    String username;

    User() {
    }

    public User(String birthDate, String photoUrl, String username) {
        this.birthDate = birthDate;
        this.photoUrl = photoUrl;
        this.username = username;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
