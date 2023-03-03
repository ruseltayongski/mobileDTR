package com.dohro7.officemobiledtr.broadcastreceiver;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.MutableLiveData;

import com.dohro7.officemobiledtr.R;
import com.dohro7.officemobiledtr.model.LocationIdentifier;
import com.dohro7.officemobiledtr.utility.DateTimeUtility;
import com.dohro7.officemobiledtr.utility.SystemUtility;

public class LocationBroadcastReceiver extends BroadcastReceiver {
    private LocationIdentifier locationIdentifier = new LocationIdentifier();
    private MutableLiveData<LocationIdentifier> mutableLiveDataLocation;

    public MutableLiveData<LocationIdentifier> getMutableLiveDataLocation() {
        return mutableLiveDataLocation;
    }

    public LocationBroadcastReceiver(Context context) {
        mutableLiveDataLocation = new MutableLiveData<>();
        updateLocationIdentifier(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        updateLocationIdentifier(context);
    }


    public void updateLocationIdentifier(Context context) {
        //Determines the location status
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            locationIdentifier.colorResource = R.color.gps_disabled;
            locationIdentifier.message = context.getResources().getString(R.string.location_permission);
            locationIdentifier.visible = View.GONE;
            return;
        }
        if (SystemUtility.isLocationEnabled(context)) {
            locationIdentifier.colorResource = R.color.location_calibrating;
            locationIdentifier.message = context.getResources().getString(R.string.location_calibrating);
           locationIdentifier.visible = View.VISIBLE;
            locationIdentifier.date = DateTimeUtility.getCurrentDate();
            locationIdentifier.time = DateTimeUtility.getCurrentTime();
        } else {
            locationIdentifier.colorResource = R.color.gps_disabled;
            locationIdentifier.message = context.getResources().getString(R.string.gps_not_enabled);
            locationIdentifier.visible = View.GONE;
            locationIdentifier.date = "";
            locationIdentifier.time = "";
        }
        mutableLiveDataLocation.setValue(locationIdentifier);
    }





}
