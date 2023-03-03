package com.dohro7.officemobiledtr.repository.sharedpreference;


import android.content.Context;
import android.content.SharedPreferences;

public class DtrEventSharedPreference {
    private static DtrEventSharedPreference instance;
    private final String DTR_SHARED_PREF = "dtr_shared_pref";
    private SharedPreferences sharedPreferences;

    public static DtrEventSharedPreference getInstance(Context context)
    {
        if (instance == null) {

            instance = new DtrEventSharedPreference(context);

        }
        return instance;
    }

    private DtrEventSharedPreference(Context context)
    {
        sharedPreferences = context.getSharedPreferences(DTR_SHARED_PREF, Context.MODE_PRIVATE);
    }

    public String getMenuTitle()
    {
        return sharedPreferences.getString("dtr_status", null);
    }

    public String getDtrLastDate()
    {
        return sharedPreferences.getString("dtr_date", null);
    }

    public void insertUpdateMenuStatus(String status, String date) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("dtr_status", status);
        editor.putString("dtr_date", date);
        editor.commit();
    }
}
