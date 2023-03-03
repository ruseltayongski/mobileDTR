package com.dohro7.officemobiledtr.repository.sharedpreference;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class MockLocationSharedPreference {

    private  static MockLocationSharedPreference instance;
    private SharedPreferences sharedPreferences;
    private MutableLiveData<String> mutable_mockLocation = new MutableLiveData<>();
    public static MockLocationSharedPreference getInstance(Context context){
        if(instance==null){
            instance= new MockLocationSharedPreference(context);
        }
        return  instance;
    }
    private MockLocationSharedPreference(Context context){
        sharedPreferences=context.getSharedPreferences("MOCK_SHARED_PREF", Context.MODE_PRIVATE);
        mutable_mockLocation.setValue(sharedPreferences.getString("mocked_created_at", null));
    }

    public void insertMockLocationCreatedAt(String value){
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("mocked_created_at", value);
        editor.apply();
        editor.commit();

        mutable_mockLocation.setValue(value);
        Log.e("mock", "MocksharedPref= " + getMockLocationSharedPref().getValue());
    }

    public MutableLiveData<String> getMockLocationSharedPref(){
        return mutable_mockLocation;
    }
}
