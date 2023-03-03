package com.dohro7.officemobiledtr.model;

import com.google.gson.annotations.SerializedName;

public class SoftwareUpdateModel {
    @SerializedName("code_version")
    public String versionName;
    @SerializedName("features")
    public String[] features;

    public SoftwareUpdateModel(String versionName, String[] features) {
        this.versionName = versionName;
        this.features = features;
    }
}
