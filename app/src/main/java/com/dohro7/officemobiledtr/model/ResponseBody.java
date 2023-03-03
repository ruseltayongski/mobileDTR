package com.dohro7.officemobiledtr.model;

import com.google.gson.annotations.SerializedName;

public class ResponseBody { //used in login
    @SerializedName("code")
    public int code;
    @SerializedName("response")
    public UserModel response;

    public ResponseBody(int code, UserModel response) {
        this.code = code;
        this.response = response;
    }
}
