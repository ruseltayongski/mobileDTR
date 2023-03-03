package com.dohro7.officemobiledtr.model;

import com.google.gson.annotations.SerializedName;

public class UploadResponse {

    @SerializedName("code")
    public int code;
    @SerializedName("response")
    public String response;

    public UploadResponse(int code, String response) {
        this.code = code;
        this.response = response;
    }
}
