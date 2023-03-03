package com.dohro7.officemobiledtr.model;

import com.google.gson.annotations.SerializedName;


public class AnnouncementModel { //used in login
    @SerializedName("code") //0 none, 1- has
    public int code;
    @SerializedName("message")
    public String message;

    public AnnouncementModel(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
