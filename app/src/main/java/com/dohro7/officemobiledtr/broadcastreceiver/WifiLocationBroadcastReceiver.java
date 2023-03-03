package com.dohro7.officemobiledtr.broadcastreceiver;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.MutableLiveData;

import com.dohro7.officemobiledtr.R;
import com.dohro7.officemobiledtr.model.LocationIdentifier;
import com.dohro7.officemobiledtr.utility.DateTimeUtility;
import com.dohro7.officemobiledtr.utility.SystemUtility;

public class WifiLocationBroadcastReceiver extends BroadcastReceiver {

    private LocationIdentifier locationIdentifier = new LocationIdentifier();
    private MutableLiveData<LocationIdentifier> mutableLiveDataLocation;

    private String dohSSID = "DOH7_FLAG";

    public MutableLiveData<LocationIdentifier> getMutableLiveDataLocation() {
        return mutableLiveDataLocation;
    }

    public WifiLocationBroadcastReceiver(Context context) {
        mutableLiveDataLocation = new MutableLiveData<>();
        updateLocationIdentifier(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //check wifi SSID
        if(intent.getAction().equalsIgnoreCase(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
            Bundle extras = intent.getExtras();
            NetworkInfo info = extras.getParcelable("networkInfo");
            NetworkInfo.State state = info.getState();

            if(state == NetworkInfo.State.CONNECTED && info.getTypeName().equalsIgnoreCase("WIFI")){
                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                String ssid1  = wifiManager.getConnectionInfo().getSSID().trim().replaceAll("\"", ""); ;// info1.getSSID().trim();

                if(ssid1.equalsIgnoreCase(dohSSID)){
                    locationIdentifier.ssid = true;
                    locationIdentifier.wifi = true;
                    locationIdentifier.wifiMessage = "Connected";
                    locationIdentifier.colorResource = R.color.location_acquired;
                    locationIdentifier.visible= View.GONE;
                    mutableLiveDataLocation.setValue(locationIdentifier);
                }else {
                    locationIdentifier.ssid = false;
                    locationIdentifier.wifi = true;
                    locationIdentifier.wifiMessage = "please connect to " + dohSSID;
                    mutableLiveDataLocation.setValue(locationIdentifier);
                    updateLocationIdentifier(context);
                }
            }else{
                locationIdentifier.ssid = false;
                locationIdentifier.wifi = false;
                locationIdentifier.wifiMessage = "Disabled";
                mutableLiveDataLocation.setValue(locationIdentifier);
                updateLocationIdentifier(context);
            }
        }else{
            updateLocationIdentifier(context);
        }
    }


    public void updateLocationIdentifier(Context context) {
        //Determines the location status
        if(!locationIdentifier.wifi || !locationIdentifier.ssid){
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
}
