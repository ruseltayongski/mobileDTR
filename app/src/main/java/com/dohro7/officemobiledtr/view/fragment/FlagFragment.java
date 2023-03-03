package com.dohro7.officemobiledtr.view.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.dohro7.officemobiledtr.R;
import com.dohro7.officemobiledtr.adapter.FlagAdapter;
import com.dohro7.officemobiledtr.broadcastreceiver.WifiLocationBroadcastReceiver;
import com.dohro7.officemobiledtr.model.FlagModel;
import com.dohro7.officemobiledtr.model.LocationIdentifier;
import com.dohro7.officemobiledtr.model.UserModel;
import com.dohro7.officemobiledtr.utility.DateTimeUtility;
import com.dohro7.officemobiledtr.utility.LocationAssistant;
import com.dohro7.officemobiledtr.utility.SystemUtility;
import com.dohro7.officemobiledtr.viewmodel.FlagViewModel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;

import java.io.File;

public class FlagFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, ResultCallback, GoogleApiClient.OnConnectionFailedListener , LocationAssistant.Listener {

    private File flagFile;
    private String filePath = "";
    private String fileName = "";
    private FlagModel flagModel;
    private FlagViewModel flagViewModel;
    private UserModel currentUser;

    private WifiLocationBroadcastReceiver locationBroadcastReceiver;
    private GoogleApiClient googleApiClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private LocationSettingsRequest locationSettingsRequest;
    private LocationSettingsRequest.Builder locationSettingsBuilder;
    private SettingsClient settingsClient;
    private LocationIdentifier locationIdentifier = new LocationIdentifier();

    private TextView txtLocationStatus;
    private ConstraintLayout locationStatusContainer;
    private TextView txtLocationDate;
    private TextView txtLocationTime;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;


    private LocationAssistant assistant;
    private Dialog mockDialog;
    private FlagAdapter flagAdapter;

    private boolean wifiSSID=false;
    private boolean locationInside=false;

    private String dohSSID ="DOH7_FLAG";
    private String locationOutside = "Location/GPS outside the allowed premise";
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mockDialog = new Dialog(getContext());

        flagFile = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Flag");

        flagViewModel = ViewModelProviders.of(this).get(FlagViewModel.class);
       //UserId
        flagViewModel.getCurrentUser().observe(this, userModel -> {
            if (userModel != null) {
                currentUser =userModel;
            }
        });

        locationBroadcastReceiver = new WifiLocationBroadcastReceiver(getContext());

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        googleApiClient.connect();

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);



//Location Restriction //location observer
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null) {
                    locationIdentifier.latitude = locationResult.getLastLocation().getLatitude() + "";
                    locationIdentifier.longitude = locationResult.getLastLocation().getLongitude() + "";
                    //Toast.makeText(getContext(),"hello"+ locationIdentifier.latitude + ", " + locationIdentifier.longitude, Toast.LENGTH_SHORT).show();
                   // if(locationIdentifier!=null){
                       // if(!locationIdentifier.ssid.equalsIgnoreCase(ssid) || !locationIdentifier.wifi){
                           new CheckLocationLoopAsyncTask().execute(getContext());
                      //  }
                    //}
                }
            }
        };
        assistant = new LocationAssistant(getContext(), this, LocationAssistant.Accuracy.HIGH, 5000, false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.flag_fragment_layout, container, false);
        recyclerView = view.findViewById(R.id.flag_recycler_view);
        progressBar=view.findViewById(R.id.flag_progressbar);

        txtLocationStatus = view.findViewById(R.id.location_status);
        txtLocationDate = view.findViewById(R.id.location_date);
        txtLocationTime = view.findViewById(R.id.location_time);
        locationStatusContainer = view.findViewById(R.id.flocation_status_container);

        flagAdapter = new FlagAdapter();
        recyclerView.setAdapter(flagAdapter);

        locationBroadcastReceiver.getMutableLiveDataLocation().observe(this, locationIdentifier -> {
           if(locationIdentifier!=null){
               this.locationIdentifier = locationIdentifier;
           }else{
               //Default value
                locationIdentifier.visible = View.GONE;
                locationIdentifier.date = DateTimeUtility.getCurrentDate();
                locationIdentifier.time = DateTimeUtility.getCurrentTime();
                locationIdentifier.message = getContext().getResources().getString(R.string.gps_not_enabled);
                locationIdentifier.colorResource = R.color.gps_disabled;
           }

           if(locationIdentifier.wifi && locationIdentifier.ssid){
               fusedLocationProviderClient.flushLocations();
               fusedLocationProviderClient.removeLocationUpdates(locationCallback);
           }else {
               requestLocationPermission();
           }

            txtLocationDate.setText(locationIdentifier.date);
            txtLocationTime.setText(locationIdentifier.time);
            txtLocationStatus.setText("*WiFi: " + locationIdentifier.wifiMessage +"\n*"+locationIdentifier.message);
            locationStatusContainer.setBackgroundResource(locationIdentifier.colorResource);

            txtLocationDate.setVisibility(locationIdentifier.visible);
            txtLocationTime.setVisibility(locationIdentifier.visible);
        });

        flagViewModel.getMutableLiveDataList().observe(this, models -> flagAdapter.setList(models));

        flagViewModel.getMutableMessage().observe(this, s -> Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show());
        return view;
    }


    /*
    @Override
    public void onResume() {
        super.onResume();
        assistant.start();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.location.PROVIDERS_CHANGED");
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        getContext().registerReceiver(locationBroadcastReceiver, intentFilter);

       *//* if(locationIdentifier!=null){
            if(!locationIdentifier.wifi || !locationIdentifier.ssid.equalsIgnoreCase(ssid))
                requestLocationPermission();
        }*//*

    }*/

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.location.PROVIDERS_CHANGED");
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        getContext().registerReceiver(locationBroadcastReceiver, intentFilter);

    }


    @Override
    public void onPause() {
        assistant.stop();
        super.onPause();

        if (locationCallback != null) {
            fusedLocationProviderClient.flushLocations();
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
        getContext().unregisterReceiver(locationBroadcastReceiver);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.flag_menu, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.flag_upload) {
            if (flagViewModel.getMutableLiveDataList().getValue().size() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Are you sure you want to upload?");
                builder.setNegativeButton("Cancel", null);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       flagViewModel.upload();
                    }
                });
                Dialog dialog = builder.create();
                dialog.show();
            } else {
                flagViewModel.getMutableMessage().setValue("Nothing to upload");
            }
        }else if(item.getItemId() == R.id.flag_attend){
            //TODO: insert time validation
            String locationMsg = locationBroadcastReceiver.getMutableLiveDataLocation().getValue().message;
            String wifiMsg = locationBroadcastReceiver.getMutableLiveDataLocation().getValue().wifiMessage;
            boolean wifiSSID = locationBroadcastReceiver.getMutableLiveDataLocation().getValue().ssid;
            boolean wifi = locationBroadcastReceiver.getMutableLiveDataLocation().getValue().wifi;
            if(!SystemUtility.isTimeAutomatic(getContext())) {
                Toast.makeText(getContext(), "'Automatic Date and Time' and/or 'Automatic Timezone' must be ENABLED", Toast.LENGTH_SHORT).show();
            }
            else if(!wifi && locationMsg.equalsIgnoreCase(getContext().getResources().getString(R.string.gps_not_enabled))){ //wifi: off and location: off
                    Toast.makeText(getContext(),"Please enable WIFI or Location/GPS", Toast.LENGTH_SHORT ).show();
            }
            else if(!wifi && locationMsg.equalsIgnoreCase(locationOutside)){ //Wifi: off and location: outside
                Toast.makeText(getContext(),"Please enable WIFI or stand in the allowed premise", Toast.LENGTH_SHORT ).show();
            }
            else if(wifi && !wifiSSID && locationMsg.equalsIgnoreCase(getContext().getResources().getString(R.string.gps_not_enabled))){ //ssid: wrong, location: off
                Toast.makeText(getContext(),"Please connect WIFI to "+ dohSSID + " or enable Location/GPS", Toast.LENGTH_SHORT ).show();
            }
            else if(wifi && !wifiSSID && locationMsg.equalsIgnoreCase(locationOutside)){ //ssid: wrong, location: outside
                Toast.makeText(getContext(),"Please connect WIFI to "+ dohSSID + " or stand in the allowed premise", Toast.LENGTH_SHORT ).show();
            }
            else  {
                //requestCameraAndStoragePermission();
                String validation = flagViewModel.timeValidate().trim();
                if(validation.equalsIgnoreCase("")){
                    requestCameraAndStoragePermission();
                }else {
                    displayAlreadyExistsDialog(validation.trim());
                }
            }


/*
            if (locationBroadcastReceiver.getMutableLiveDataLocation().getValue().message.equalsIgnoreCase(getContext().getResources().getString(R.string.gps_not_enabled))) {
                Toast.makeText(getContext(),"Please enable Location/GPS", Toast.LENGTH_SHORT ).show(); }

            else if(!SystemUtility.isTimeAutomatic(getContext())) {
                Toast.makeText(getContext(), "'Automatic Date and Time' and/or 'Automatic Timezone' must be ENABLED", Toast.LENGTH_SHORT).show();
            }

            else if(locationBroadcastReceiver.getMutableLiveDataLocation().getValue().message.equalsIgnoreCase(getContext().getResources().getString(R.string.location_outside)))
            {  Toast.makeText(getContext(), "Please stand in the allowed premise, and try again", Toast.LENGTH_SHORT).show(); }

            else  {
               String validation = flagViewModel.timeValidate().trim();
                if(validation.equalsIgnoreCase("")){
                    requestCameraAndStoragePermission();
                }else {
                   displayAlreadyExistsDialog(validation.trim());
                }
            }*/


        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        assistant.onPermissionsUpdated(requestCode, grantResults);

        switch (requestCode) {
            case 0: requestLocationPermission();
                return;
            case 1: requestCameraAndStoragePermission();
        }
    }

    public void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);

            return;
        }
        if(!fusedLocationProviderClient.getLastLocation().isSuccessful())
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private void requestCameraAndStoragePermission() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);

            return;
        }

      //  if (locationBroadcastReceiver.getMutableLiveDataLocation().getValue().message.equalsIgnoreCase("Location/GPS acquired")) {
            fileName = "flag_" + DateTimeUtility.getFilenameDate(currentUser.getUserId()) + ".jpg";
            File imageFile = new File(flagFile, fileName);
            imageFile.getParentFile().mkdirs();

            filePath = imageFile.getAbsolutePath();
            flagModel = newFlagLog();

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri uriImage = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) ?
                    FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileprovider", imageFile) :
                    Uri.fromFile(imageFile);

            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriImage);
            try{
                cameraIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(cameraIntent, 0);
            }
            catch (Exception e){
                Toast.makeText(getContext(), "No activity can handle this request", Toast.LENGTH_SHORT).show();
            }
     /*   }
        else {
            Toast.makeText(getContext(),  "Please stand in the allowed premise, and try again", Toast.LENGTH_LONG).show();
        }*/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        assistant.onActivityResult(requestCode, resultCode);

        switch (requestCode) {
            case 0:
                if (resultCode == Activity.RESULT_OK) {
                    showTimeInDialog(flagModel.time, flagModel.remarks);
                    flagViewModel.insertFlagLog(flagModel);
                    break;
                } else {
                    dispayActionCancelledDialog();
                }
        }
    }
    public void dispayActionCancelledDialog() {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_cancelled_layout);


        dialog.findViewById(R.id.dialog_cancelled_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialog.getWindow().setAttributes(lp);
        dialog.show();
    }

    public void showTimeInDialog(String time, String remarks) {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_timelog_layout);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCanceledOnTouchOutside(false);
        TextView dialogLogStatus = dialog.findViewById(R.id.dialog_timelog_status);
        TextView dialogLogTime = dialog.findViewById(R.id.dialog_timelog_time);

        String currentTime = DateTimeUtility.getCurrentTime();
        if(remarks!=null){
            if(remarks.equalsIgnoreCase("FLAG_MORNING")){
                remarks = "FLAG CEREMONY";
            }else if(remarks.equalsIgnoreCase("FLAG_RETREAT")){
                remarks = "FLAG RETREAT";
            }
        }
        dialogLogStatus.setText(remarks);

        if(time!=null){
            dialogLogTime.setText(time);}
        else{
            dialogLogTime.setText(currentTime);
        }

        dialog.findViewById(R.id.dialog_timelog_continue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialog.getWindow().setAttributes(lp);
        dialog.show();
    }

    public void displayAlreadyExistsDialog(String message) {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_already_timed);

        TextView dialogMessage = dialog.findViewById(R.id.dialog_already_message);
        dialogMessage.setText(message);

        dialog.findViewById(R.id.dialog_already_continue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialog.getWindow().setAttributes(lp);
        dialog.show();
    }

    public void displayDialogMockLocation() {

        mockDialog.setContentView(R.layout.dialog_cancelled_layout);
        mockDialog.setCancelable(false);
        final TextView text = mockDialog.findViewById(R.id.dialog_undertime_message);
        final TextView title = mockDialog.findViewById(R.id.dialog_undertime_title);
        title.setText(R.string.warning);
        text.setText(R.string.location_mock);

        mockDialog.findViewById(R.id.dialog_cancelled_ok).setVisibility(View.GONE);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(mockDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        mockDialog.getWindow().setAttributes(lp);
        mockDialog.show();
    }

    class CheckLocationLoopAsyncTask extends AsyncTask<Context, Void, Void> { //polygonial style
          int    polyCorners  =  4;
          double[]  polyX = {10.30745, 10.307531, 10.307937, 10.30788};//polyX = {10.30745, 10.307510, 10.307932, 10.30788};
          double[]  polyY = {123.89356,123.893796, 123.893649,123.89341};//polyY = {123.89356,123.893750, 123.89361,123.89341};

        @Override
        protected Void doInBackground(Context... contexts) {
            double x= Double.parseDouble(locationIdentifier.latitude);
            double y=  Double.parseDouble(locationIdentifier.longitude);
            int   i, j=polyCorners-1 ;
            Boolean  oddNodes = false;
            double answer;

            for (i=0; i<polyCorners; i++) {
                if ((polyY[i]< y && polyY[j]>=y  ||  polyY[j]< y && polyY[i]>=y) && (polyX[i]<=x || polyX[j]<=x)) {
                    answer = (polyX[i]+((y-polyY[i])/(polyY[j]-polyY[i])))*(polyX[j]-polyX[i]);
                    if (answer<x) {
                            oddNodes=!oddNodes;
                    }
                }j=i;
            }

            if(oddNodes){ //YES- inside
                locationIdentifier.message = contexts[0].getResources().getString(R.string.location_acquired);
                locationIdentifier.colorResource = R.color.location_acquired;
                locationIdentifier.visible = View.GONE;
            }else { //No - outside
                locationIdentifier.message = locationOutside;//contexts[0].getResources().getString(R.string.location_outside);
                locationIdentifier.visible = View.GONE;
                locationIdentifier.colorResource = R.color.delete;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            locationBroadcastReceiver.getMutableLiveDataLocation().setValue(locationIdentifier);
            super.onPostExecute(aVoid);
        }
    }

    public FlagModel newFlagLog() {
        FlagModel flagLog = new FlagModel();
        flagLog.id = 0;
        flagLog.date = DateTimeUtility.getCurrentDate();
        flagLog.time = DateTimeUtility.getCurrentTime();
        flagLog.filePath = filePath;
        flagLog.fileName = fileName;
        flagLog.latitude = locationBroadcastReceiver.getMutableLiveDataLocation().getValue().latitude;
        flagLog.longitude = locationBroadcastReceiver.getMutableLiveDataLocation().getValue().longitude;
         flagLog.mocked = flagViewModel.getMockLocationSharedPref().getValue();

         if((flagLog.getHour()==7 && flagLog.getMinutes()>=55) || (flagLog.getHour()==8 && flagLog.getMinutes() <=10)){
            flagLog.remarks = "FLAG_MORNING";
            flagLog.edited = "8";
        }else if(flagLog.getHour()==16 && flagLog.getMinutes()<=5){
            flagLog.remarks = "FLAG_RETREAT";
            flagLog.edited = "9";
        }/*else {
             flagLog.remarks = "FLAG_TESTING";
             flagLog.edited = "8";
         }*/

        return flagLog;
    }

    //Location Assistant Listener
    @Override
    public void onNeedLocationPermission() { }

    @Override
    public void onExplainLocationPermission() { }

    @Override
    public void onLocationPermissionPermanentlyDeclined(View.OnClickListener fromView, DialogInterface.OnClickListener fromDialog) { }

    @Override
    public void onNeedLocationSettingsChange() { }

    @Override
    public void onFallBackToSystemSettings(View.OnClickListener fromView, DialogInterface.OnClickListener fromDialog) { }

    @Override
    public void onNewLocationAvailable(Location location) {
        if(!location.isFromMockProvider() && mockDialog.isShowing()){
            mockDialog.dismiss();
        }
    }

    @Override
    public void onMockLocationsDetected(View.OnClickListener fromView, DialogInterface.OnClickListener fromDialog) {
        displayDialogMockLocation();
        if(flagViewModel.getMockLocationSharedPref().getValue()==null){
            flagViewModel.insertMockLocationCreatedAt(DateTimeUtility.getCurrentDateTime());
        }
    }

    @Override
    public void onError(LocationAssistant.ErrorType type, String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationSettingsBuilder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        locationSettingsBuilder.setAlwaysShow(true);

        locationSettingsRequest = locationSettingsBuilder.build();

        PendingResult result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, locationSettingsRequest);

        settingsClient = LocationServices.getSettingsClient(getContext());
        settingsClient.checkLocationSettings(locationSettingsRequest);

        result.setResultCallback(this);
    }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }

    @Override
    public void onResult(@NonNull Result result) {
        final Status status = result.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                break;

            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                try {
                    status.startResolutionForResult((Activity) getContext(), 100);
                } catch (IntentSender.SendIntentException e) {
                }
                break;

            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                break;
        }
    }
}
