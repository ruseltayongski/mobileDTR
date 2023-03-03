package com.dohro7.officemobiledtr.model;

import com.google.gson.annotations.SerializedName;

public class UserModel {
    @SerializedName("userid")
    public String id;
    @SerializedName("fname")
    public String fname;
    @SerializedName("lname")
    public String lname;
    @SerializedName("authority") // FOR RESET PASSWORD
    public String authority;
    @SerializedName("section")
    public String section;
    @SerializedName("dmo_roles")
    public int dmo_roles; //value: 0-Not dmo, 1-yes

    @SerializedName("area_of_assignment_roles")
    public int area_of_assignment_roles;

    @SerializedName("region")
    public String region;

    public UserModel(String id, String fname, String lname, String authority, String section, int dmo_roles, int area_of_assignment_roles,  String region) {
        this.id = id;
        this.fname = fname;
        this.lname = lname;
        this.authority=authority;
        this.section=section;
        this.dmo_roles = dmo_roles;
        this.area_of_assignment_roles = area_of_assignment_roles;
        this.region=region;
    }

    public UserModel() {
    }

    public String getName() { return fname + " " + lname; }
    public String getUserId() { return "" + id; }
    public String getAuthority() { return authority; }
    public String getSection(){return  section; }
    public String getRegion(){return region; }
}