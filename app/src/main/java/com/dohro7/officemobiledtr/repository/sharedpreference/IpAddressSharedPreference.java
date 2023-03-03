package com.dohro7.officemobiledtr.repository.sharedpreference;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.MutableLiveData;

public class IpAddressSharedPreference {
    private static IpAddressSharedPreference instance;
    private final String IP_SHARED_PREF = "ip_shared_pref";
    private SharedPreferences sharedPreferences;
    private MutableLiveData<String> mutable_ip=new MutableLiveData<>();
    public static IpAddressSharedPreference getInstance(Context context)
    {
        if (instance == null) {
            instance = new IpAddressSharedPreference(context);
        }
        return instance;
    }

    private IpAddressSharedPreference(Context context)
    {
        sharedPreferences = context.getSharedPreferences(IP_SHARED_PREF, Context.MODE_PRIVATE);
        mutable_ip.setValue(sharedPreferences.getString("ip_address", null));
    }

    public MutableLiveData<String> getIpAddress_mutable() {
        return mutable_ip;
    }

    public void insertUpdateIP(String ipAddress) {
        mutable_ip.setValue(ipAddress);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("ip_address", ipAddress);
        editor.apply();
        editor.commit();
    }
}