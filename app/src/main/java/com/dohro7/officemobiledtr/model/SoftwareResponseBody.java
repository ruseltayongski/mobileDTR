package com.dohro7.officemobiledtr.model;

import com.google.gson.annotations.SerializedName;

public class SoftwareResponseBody {

    @SerializedName("code")
    public int code;
    @SerializedName("response")
    public SoftwareUpdateModel softwareUpdateModel;

    public SoftwareResponseBody(int code, SoftwareUpdateModel softwareUpdateModel) {
        this.code = code;
        this.softwareUpdateModel = softwareUpdateModel;
    }
}
