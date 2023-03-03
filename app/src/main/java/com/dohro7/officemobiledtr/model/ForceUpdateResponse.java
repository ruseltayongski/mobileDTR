package com.dohro7.officemobiledtr.model;

import com.google.gson.annotations.SerializedName;

public class ForceUpdateResponse {
    @SerializedName("message") //Software update information
    public String message;
    @SerializedName("code") //0 not force, 1- forced
    public int code;
    @SerializedName("latest_version")
    public String latest_version;

    public ForceUpdateResponse(String message, int code, String latest_version) {
        this.message=message;
        this.code = code;
        this.latest_version = latest_version;
    }
}
