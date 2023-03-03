package com.dohro7.officemobiledtr.model;

import com.google.gson.annotations.SerializedName;

public class RetrofitMessage {
//
//    @SerializedName("message")
//    public String message;
//
//    public RetrofitMessage(String message) {
//        this.message = message;
//    }

    @SerializedName("response")
    public String response;

    public RetrofitMessage(String response) {
        this.response = response;
    }
}
