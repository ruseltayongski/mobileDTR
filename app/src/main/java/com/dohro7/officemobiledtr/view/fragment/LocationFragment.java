package com.dohro7.officemobiledtr.view.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.dohro7.officemobiledtr.R;
import com.dohro7.officemobiledtr.broadcastreceiver.LocationBroadcastReceiver;
import com.dohro7.officemobiledtr.model.LocationIdentifier;
import com.dohro7.officemobiledtr.model.UserModel;
import com.dohro7.officemobiledtr.utility.DateTimeUtility;
import com.dohro7.officemobiledtr.utility.LocationAssistant;
import com.dohro7.officemobiledtr.viewmodel.DtrViewModel;
import com.dohro7.officemobiledtr.viewmodel.LoginViewModel;
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

public class LocationFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, ResultCallback, GoogleApiClient.OnConnectionFailedListener, LocationAssistant.Listener {

    private DtrViewModel dtrViewModel;
    private LoginViewModel loginViewModel;
    UserModel currentUser = new UserModel();
    WebView myWebView;
    String locationUrl="";
    String baseUrl="";
    ProgressBar progressBar;
    Boolean webview_displayed=false;
    private Dialog mockDialog;

    private LocationBroadcastReceiver locationBroadcastReceiver;
    private LocationIdentifier locationIdentifier = new LocationIdentifier();
    private GoogleApiClient googleApiClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private LocationSettingsRequest locationSettingsRequest;
    private LocationSettingsRequest.Builder locationSettingsBuilder;
    private SettingsClient settingsClient;
    private LocationAssistant assistant;

//http://222.127.126.35/dtr/get/user/area_of_assignment/0635?latitude=9.967165&longitude=123.413426
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        webview_displayed=false;
        dtrViewModel = ViewModelProviders.of(this).get(DtrViewModel.class);
        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);

        mockDialog = new Dialog(getContext());

        dtrViewModel.getCurrentUser().observe(this, new Observer<UserModel>() {
            @Override
            public void onChanged(UserModel userModel) {
                if (userModel != null) {
                    currentUser =userModel;
                }
            }});

        //location
        locationBroadcastReceiver = new LocationBroadcastReceiver(getContext());

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

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if(currentUser.id!=null && baseUrl.contains("http") ){
                    if (locationResult != null) {
                        locationIdentifier.latitude = locationResult.getLastLocation().getLatitude() + "";
                        locationIdentifier.longitude = locationResult.getLastLocation().getLongitude() + "";

                        //Log.e("location", "lat/long= " + locationIdentifier.latitude + ", " +locationIdentifier.longitude);

                        locationUrl=baseUrl+currentUser.id +"?latitude="+ locationResult.getLastLocation().getLatitude() + "&longitude="+locationResult.getLastLocation().getLongitude();//9.967165123.413426
                    }
                    else {
                            locationUrl=currentUser.id;
                    }
                    if(!webview_displayed){
                        myWebView.loadUrl(locationUrl);
                        webview_displayed=true;
                    }
                }
            }
        };
        assistant = new LocationAssistant(getContext(), this, LocationAssistant.Accuracy.HIGH, 5000, false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        final View view = inflater.inflate(R.layout.location_fragment, container, false);
        progressBar=view.findViewById(R.id.location_progressbar);


//webview
        myWebView = view.findViewById(R.id.software_webview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

         myWebView.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if(progressBar!=null && progressBar.getVisibility()==View.VISIBLE)
                {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
        loginViewModel.getIpAddress_mutable().observe(this,  new Observer<String>() {
            @Override
            public void onChanged(String s) {
                progressBar.setVisibility(View.VISIBLE);
                if(s!=null){
                    if(!s.trim().isEmpty()){
                        baseUrl ="http://" + s.trim() + "/dtr/get/user/area_of_assignment/";
                    }
                    else{
                        baseUrl="http://" + getString(R.string.default_ip) + "/dtr/get/user/area_of_assignment/";

                    }
                }else {
                    baseUrl="http://" + getString(R.string.default_ip) + "/dtr/get/user/area_of_assignment/";
                }

            }
        });


        dtrViewModel.getDeleteMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s!=null){
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show();
                }
            }
        });

        return view;
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.location_menu, menu);  //get area_of_assignment, refresh map
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //onclick

        switch (item.getItemId()) {
            case R.id.location_get_list:
                 progressBar.setVisibility(View.VISIBLE);
                 dtrViewModel.getLocationAssignmentsFromServer(currentUser.getUserId());
                break;

            case R.id.location_refresh_map:
                myWebView.loadUrl( locationUrl );
                progressBar.setVisibility(View.VISIBLE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onStart() {
        super.onStart();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.location.PROVIDERS_CHANGED");
        getContext().registerReceiver(locationBroadcastReceiver, intentFilter);
    }
    @Override
    public void onResume() {
        super.onResume();
        assistant.start();

        requestLocationPermission();
    }

    @Override
    public void onPause() {
        assistant.stop();
        super.onPause();

        if (locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }getContext().unregisterReceiver(locationBroadcastReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        assistant.onPermissionsUpdated(requestCode, grantResults);

        if (requestCode == 0) {
            requestLocationPermission();
        }
    }

    public void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);

            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    //implements
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
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

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

    //Mock Location
    @Override
    public void onNeedLocationPermission() {

    }

    @Override
    public void onExplainLocationPermission() {

    }

    @Override
    public void onLocationPermissionPermanentlyDeclined(View.OnClickListener fromView, DialogInterface.OnClickListener fromDialog) {

    }

    @Override
    public void onNeedLocationSettingsChange() {

    }

    @Override
    public void onFallBackToSystemSettings(View.OnClickListener fromView, DialogInterface.OnClickListener fromDialog) {

    }

    @Override
    public void onNewLocationAvailable(Location location) {
        //Log.e("mock", "onNewLocationAvailable");
        if(!location.isFromMockProvider() && mockDialog.isShowing()){
            //Log.e("mock", "onNewLocationAvailable isFromMockProvider");
            mockDialog.dismiss();
        }
    }

    @Override
    public void onMockLocationsDetected(View.OnClickListener fromView, DialogInterface.OnClickListener fromDialog) {
        //Log.e("mock", "onMockLocationsDetected");

        displayDialogMockLocation();
        if(dtrViewModel.getMockLocationSharedPref().getValue()==null){
            dtrViewModel.insertMockLocationCreatedAt(DateTimeUtility.getCurrentDateTime());
        }  //TODO: uncomment after testing

    }

    @Override
    public void onError(LocationAssistant.ErrorType type, String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
