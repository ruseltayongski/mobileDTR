package com.dohro7.officemobiledtr.repository.sharedpreference;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.dohro7.officemobiledtr.model.UserModel;

public class UserSharedPreference {
    private final String USER_SHARED_PREF = "user_shared_pref";
    private MutableLiveData<UserModel> mutableUserModel = new MutableLiveData<>();
    private static UserSharedPreference instance;
    private SharedPreferences sharedPreferences;

    private UserSharedPreference(Context context) {
        sharedPreferences = context.getSharedPreferences(USER_SHARED_PREF, Context.MODE_PRIVATE);

        String userId = sharedPreferences.getString("userid", null);
        if (userId != null) {
            String firstName = sharedPreferences.getString("fname", null);
            String lastName = sharedPreferences.getString("lname", null);
            String authority=sharedPreferences.getString("authority", null);//
            String section=sharedPreferences.getString("section", null);
            int dmo_roles=sharedPreferences.getInt("dmo_roles", 0);
            int area_of_assignment_roles=sharedPreferences.getInt("area_of_assignment_roles", 0);
            String region=sharedPreferences.getString("region", null);
            UserModel userModel = new UserModel(userId, firstName, lastName,authority, section, dmo_roles,area_of_assignment_roles, region);//
            mutableUserModel.setValue(userModel);
        }
        else mutableUserModel.setValue(null);
    }

    public static UserSharedPreference getInstance(Context context) {
        if (instance == null) {
            instance = new UserSharedPreference(context);
        }
        return instance;
    }

    public void insertUser(UserModel userModel) {
        Log.e("Login", userModel.id + " " + userModel.fname + " " + userModel.lname + " " + userModel.authority);//
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userid", userModel.id);
        editor.putString("fname", userModel.fname);
        editor.putString("lname", userModel.lname);
        editor.putString("authority", userModel.authority);
        editor.putString("section", userModel.section);
        editor.putInt("dmo_roles", userModel.dmo_roles);
        editor.putInt("area_of_assignment_roles", userModel.area_of_assignment_roles);
        editor.putString("region", userModel.region);
        editor.commit(); //apply changes

        mutableUserModel.setValue(userModel);

    }

    public MutableLiveData<UserModel> getUserModel() {
        return mutableUserModel;
    }
}
