package com.dohro7.officemobiledtr.view.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.net.Uri;
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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.dohro7.officemobiledtr.R;
import com.dohro7.officemobiledtr.adapter.DtrAdapter;
import com.dohro7.officemobiledtr.broadcastreceiver.LocationBroadcastReceiver;
import com.dohro7.officemobiledtr.model.DateLog;
import com.dohro7.officemobiledtr.model.LocationIdentifier;
import com.dohro7.officemobiledtr.model.LocationModel;
import com.dohro7.officemobiledtr.model.TimeLogModel;
import com.dohro7.officemobiledtr.model.UserModel;
import com.dohro7.officemobiledtr.utility.BitmapDecoder;
import com.dohro7.officemobiledtr.utility.DateTimeUtility;
import com.dohro7.officemobiledtr.utility.LocationAssistant;
import com.dohro7.officemobiledtr.utility.SystemUtility;
import com.dohro7.officemobiledtr.viewmodel.DtrViewModel;
import com.dohro7.officemobiledtr.viewmodel.SoftwareUpdateViewModel;
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
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DtrFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, ResultCallback, GoogleApiClient.OnConnectionFailedListener , LocationAssistant.Listener {
    private RecyclerView recyclerView;
    private TextView txtLocationStatus;
    private ConstraintLayout locationStatusContainer;
    private TextView txtLocationDate;
    private TextView txtLocationTime;
    private DtrAdapter dtrAdapter;
    private MenuItem menuItem;
    private DtrViewModel dtrViewModel;
    private SoftwareUpdateViewModel softwareUpdateViewModel;

    private boolean swipeBack;
    private ProgressBar progressBar;

    private LocationBroadcastReceiver locationBroadcastReceiver;

    private GoogleApiClient googleApiClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private LocationSettingsRequest locationSettingsRequest;
    private LocationSettingsRequest.Builder locationSettingsBuilder;
    private SettingsClient settingsClient;


    private String filePath = "";
    private String fileName = "";
    private String userSection;
    private File timeLogFile;
    private File screenShotFile;
    private LocationIdentifier locationIdentifier = new LocationIdentifier();

    private LocationAssistant assistant;
    private Dialog mockDialog;

    private Context context;

    private TimeLogModel timeLogModel;

    private List<LocationModel> list_location = new ArrayList<>();

    UserModel currentUser = new UserModel();

    Double latitude;
    Double Longitude;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mockDialog = new Dialog(getContext());
        this.context = getContext();

        timeLogFile = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Timelogs");
        screenShotFile = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Screenshot");

        dtrViewModel = ViewModelProviders.of(this).get(DtrViewModel.class);
        userSection= dtrViewModel.getSection();
          //SOFTWARE UPDATE
        softwareUpdateViewModel = ViewModelProviders.of(this).get(SoftwareUpdateViewModel.class);
        softwareUpdateViewModel.checkForceUpdate();

        //UserId
        dtrViewModel.getCurrentUser().observe(this, new Observer<UserModel>() {
            @Override
            public void onChanged(UserModel userModel) {
                if (userModel != null) {
                    currentUser =userModel;
                }
            }});


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

        //Default value
        locationIdentifier.visible = View.GONE;
        locationIdentifier.date = DateTimeUtility.getCurrentDate();
        locationIdentifier.time = DateTimeUtility.getCurrentTime();
        locationIdentifier.message = getContext().getResources().getString(R.string.gps_not_enabled);
        locationIdentifier.colorResource = R.color.gps_disabled;

//Location Restriction
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                //Uncomment if you want to test on a location calibrating status
               /* locationIdentifier.colorResource = R.color.location_calibrating;
                locationIdentifier.message = "Location/GPS calibrating";
                locationIdentifier.visible = View.VISIBLE;
                locationIdentifier.date = DateTimeUtility.getCurrentDate();
                locationIdentifier.time = DateTimeUtility.getCurrentTime();*/

                if (locationResult != null) {
                    locationIdentifier.latitude = locationResult.getLastLocation().getLatitude() + "";
                    locationIdentifier.longitude = locationResult.getLastLocation().getLongitude() + "";

                    //Log.e("loc", "lat/long= " + locationIdentifier.latitude + ", " +locationIdentifier.longitude +" dmo_roles" +  currentUser.dmo_roles  +" area_of_assignment_roles " +  currentUser.area_of_assignment_roles);

                    if(currentUser.dmo_roles==1 && currentUser.area_of_assignment_roles==0){ //DMOs
                     //no location restriction, can punch in/out anywhere
                            //Log.e("loc", "DMO & area of assignment 0:" + currentUser.area_of_assignment_roles);
                            locationIdentifier.message = getContext().getResources().getString(R.string.location_acquired);
                            locationIdentifier.colorResource = R.color.location_acquired;
                            locationIdentifier.visible=View.GONE;
                            locationBroadcastReceiver.getMutableLiveDataLocation().setValue(locationIdentifier);
                    }
                    else {
                        //Log.e("loc", "dmo-" + currentUser.dmo_roles + " area-" +currentUser.area_of_assignment_roles+ " call asynctask" );
                        new CheckLocationLoopAsyncTask().execute(getContext());
                    }
                }
            }
        };
       assistant = new LocationAssistant(getContext(), this, LocationAssistant.Accuracy.HIGH, 5000, false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dtr_fragment_layout, container, false);
        recyclerView = view.findViewById(R.id.dtr_recycler_view);
        progressBar=view.findViewById(R.id.progressbar_dtr);

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            DateLog dateLog; String month;
            boolean deleted;
            boolean toDelete=false;

            @Override
            public int convertToAbsoluteDirection(int flags, int layoutDirection) { //1st tawgon nig swipe2
                if (swipeBack){
                    swipeBack = false;
                    return 0; }
                deleted=false;
                return super.convertToAbsoluteDirection(flags, layoutDirection);
            }

            @Override //call while nag swipe2, and after makadelete sa onSwiped
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive)
            {
                int position=viewHolder.getAdapterPosition();
                boolean uploaded=false;
                if(!deleted)
                {
                    dateLog = dtrAdapter.getItem(position);
                    if (dateLog != null )
                    {
                        List<TimeLogModel> list = dateLog.timeLogModels; // logs am/pm in/out at position
                        month = dateLog.date.split("-")[1];
                        for (TimeLogModel timeLogModel : list)
                        {    if(timeLogModel.uploaded==1){uploaded =true;} else{ uploaded=false;}     }
//SAME MONTH
                        if ((!month.equalsIgnoreCase(DateTimeUtility.getCurrentDate().split("-")[1]) ) && uploaded)
                        {
                            toDelete=true;
                            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                        }
                        else toDelete=false;
                    }
                }
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target)
            {
                //Log.e("swipe","onMove");
                return false;
            }

            @Override //after swiped
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                dateLog = dtrAdapter.getItem(viewHolder.getAdapterPosition());
                if (dateLog != null )
                {
                    if (toDelete)
                    {
                        dtrViewModel.deleteLogByDate(dateLog.date);
                        deleted=true;
                    }
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        txtLocationStatus = view.findViewById(R.id.location_status);
        txtLocationDate = view.findViewById(R.id.location_date);
        txtLocationTime = view.findViewById(R.id.location_time);
        locationStatusContainer = view.findViewById(R.id.location_status_container);

        dtrAdapter = new DtrAdapter();
        recyclerView.setAdapter(dtrAdapter);

        dtrViewModel.getDeleteMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s!=null){
                    Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show();
                }
            }
        });

        dtrViewModel.getMutableLiveDataList().observe(this, new Observer<List<TimeLogModel>>() {
            @Override
            public void onChanged(List<TimeLogModel> timeLogModels) {
                    dtrAdapter.setList(timeLogModels);
            }
        });

        dtrViewModel.getMutableUndertime().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean.booleanValue()) {//Log.e("undertime", "mutable undertime true");
                    displayDialogUndertime();
                } else {//Log.e("undertime", "mutable undertime false");
                    requestCameraAndStoragePermission();
                }
            }
        });

        dtrViewModel.getMutableTimeExists().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                displayAlreadyExistsDialog(s);
            }
        });

        dtrViewModel.getUploadMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                progressBar.setVisibility(View.GONE);
                List<String> array_message = Arrays.asList(getContext().getResources().getStringArray(R.array.no_retry_message));
                if(!array_message.contains(s)) {
                    Snackbar snackbar = Snackbar.make(view.findViewById(R.id.root), s, Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dtrViewModel.uploadLogs();
                        }
                    });
                    snackbar.show();
                    return;
                }
                Snackbar snackbar = Snackbar.make(view.findViewById(R.id.root), s, Snackbar.LENGTH_SHORT);
                snackbar.setText(s);
                snackbar.show();
            }
        });

        locationBroadcastReceiver.getMutableLiveDataLocation().observe(this, new Observer<LocationIdentifier>() {
            @Override
            public void onChanged(LocationIdentifier locationIdentifier) {
                txtLocationDate.setText(locationIdentifier.date);
                txtLocationTime.setText(locationIdentifier.time);
                txtLocationStatus.setText(locationIdentifier.message);
                locationStatusContainer.setBackgroundResource(locationIdentifier.colorResource);

                 txtLocationDate.setVisibility(locationIdentifier.visible);
                 txtLocationTime.setVisibility(locationIdentifier.visible);

            }
        });

        dtrViewModel.getListLiveData_locationAssignments().observe(this, new Observer<List<LocationModel>>() {
            @Override
            public void onChanged(List<LocationModel> locationModels) {
                list_location = locationModels;
            }
        });
        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.dtr_menu, menu);  //in/out , upload btn
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menuItem = menu.findItem(R.id.dtr_time);

        dtrViewModel.getLiveDataMenuTitle().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                menuItem.setTitle(s);
            }
        });
        dtrViewModel.getLiveDataMenuTitle().setValue(dtrViewModel.getLiveDataMenuTitle().getValue());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //onclick
        switch (item.getItemId()) {
            case R.id.dtr_time:
                SystemUtility.vibrateOnClick(getContext());
                softwareUpdateViewModel.checkForceUpdate();

                if(softwareUpdateViewModel.getMutableForceUpdate().getValue()!=null){
                     if(!softwareUpdateViewModel.getMutableForceUpdate().getValue().latest_version.equalsIgnoreCase(SystemUtility.getVersionName(getContext())) && softwareUpdateViewModel.getMutableForceUpdate().getValue().code==1) {
                        displayDialogNotUpdated(softwareUpdateViewModel.getMutableForceUpdate().getValue().message);
                        break;
                     }
                }
                if (locationBroadcastReceiver.getMutableLiveDataLocation().getValue().message.equalsIgnoreCase(getContext().getResources().getString(R.string.gps_not_enabled))) {
                    dtrViewModel.getUploadMessage().setValue("Please enable Location/GPS");
                }
                else if(!SystemUtility.isTimeAutomatic(getContext())) {
                    Toast.makeText(getContext(), "'Automatic Date and Time' and/or 'Automatic Timezone' must be ENABLED", Toast.LENGTH_SHORT).show();
                }
                else if(locationBroadcastReceiver.getMutableLiveDataLocation().getValue().message.equalsIgnoreCase(getContext().getResources().getString(R.string.location_outside)))
                {  Toast.makeText(getContext(), "You are outside the boundaries, please check the advisory for the designated areas", Toast.LENGTH_SHORT).show(); }

                else  {
                    dtrViewModel.timeValidate(); }
                break;

            case R.id.dtr_upload:
                SystemUtility.vibrateOnClick(getContext());
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Are you sure you want to upload?");
                builder.setNegativeButton("Cancel", null);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressBar.setVisibility(View.VISIBLE);
                        dtrViewModel.uploadLogs();//////
                    }
                });
                Dialog dialog = builder.create();
                dialog.show();
                break;

            case R.id.dtr_delete:
                displayDeleteDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayDeleteDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("You are about to delete all of your uploaded logs from the prior month on this device.\nDo you wish to continue?");
        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dtrViewModel.deleteByRangeDate(DateTimeUtility.getCurrentDate());
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
    }

    public void displayDialogUndertime() {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_undertime_layout);
        dialog.setCancelable(false);
        dialog.findViewById(R.id.dialog_undertime_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.dialog_undertime_continue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCameraAndStoragePermission();
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

    public void displayDialogNotUpdated(String message) {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_notupdated);

        TextView message_tv = dialog.findViewById(R.id.dialog_notupdated_message);
        message_tv.setText(message);

        dialog.findViewById(R.id.dialog_notupdated_continue).setOnClickListener(new View.OnClickListener() {
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
    public void onResume() {
        super.onResume();
        assistant.start();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.location.PROVIDERS_CHANGED");

        getContext().registerReceiver(locationBroadcastReceiver, intentFilter);

        requestLocationPermission();
    }

    @Override
    public void onStart() {
        super.onStart();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.location.PROVIDERS_CHANGED");
        getContext().registerReceiver(locationBroadcastReceiver, intentFilter);
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

        if (locationBroadcastReceiver.getMutableLiveDataLocation().getValue().message.equalsIgnoreCase("Location/GPS acquired")) {
            fileName = "timelog_" + DateTimeUtility.getFilenameDate(currentUser.getUserId()) + ".jpg";
            File imageFile = new File(timeLogFile, fileName);
            imageFile.getParentFile().mkdirs();

            filePath = imageFile.getAbsolutePath();
            timeLogModel = newTimelog();

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
               //Log.e("11", "ex " + e.getMessage());
                Toast.makeText(getContext(), "No activity can handle this request", Toast.LENGTH_SHORT).show();
            }

        }
        else if(locationBroadcastReceiver.getMutableLiveDataLocation().getValue().message.equalsIgnoreCase(getString(R.string.location_calibrating))) {
            if(currentUser.dmo_roles ==1){//Screenshots if no location is acquired

                Bitmap bitmap = BitmapDecoder.screenShotView((Activity) getContext());
                fileName = "screenshot_" + DateTimeUtility.getFilenameDate(currentUser.id) + ".jpg";
                File imageFolderFile = new File(screenShotFile, fileName);
                imageFolderFile.getParentFile().mkdirs();
                try {
                    OutputStream fout = new FileOutputStream(imageFolderFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
                    fout.flush();
                    fout.close();
                } catch (FileNotFoundException e) {
                    //Log.e("FNOE", e.getMessage());
                } catch (IOException e) {
                    //Log.e("IOE", e.getMessage());

                }
                filePath = imageFolderFile.getAbsolutePath();
                showTimeInDialog(null);
                dtrViewModel.insertTimeLog(newTimelog());
            }
            else{
                Toast.makeText(getContext(), "Please wait for the application to acquire location and try again", Toast.LENGTH_LONG).show();
            }
        }
        else if(locationBroadcastReceiver.getMutableLiveDataLocation().getValue().message.equalsIgnoreCase(getString(R.string.location_empty))) {
            Toast.makeText(getContext(), getString(R.string.location_empty) + " and try again", Toast.LENGTH_LONG).show();
        }
    }

    public void showTimeInDialog(String time) {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_timelog_layout);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCanceledOnTouchOutside(false);
        TextView dialogLogStatus = dialog.findViewById(R.id.dialog_timelog_status);
        TextView dialogLogTime = dialog.findViewById(R.id.dialog_timelog_time);

        String currentTime = DateTimeUtility.getCurrentTime();
        dialogLogStatus.setText(/*dtrViewModel.getLiveDataMenuTitle().getValue()*/ timeLogModel.status);

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        assistant.onActivityResult(requestCode, resultCode);

        switch (requestCode) {
            case 0:
                if (resultCode == Activity.RESULT_OK) {
                    dtrViewModel.insertTimeLog(timeLogModel);
                    showTimeInDialog(timeLogModel.time);
                    break;
                } else {
                    dispayActionCancelledDialog();
                }
        }
    }



    public TimeLogModel newTimelog() {
        TimeLogModel timeLogModel = new TimeLogModel();
        timeLogModel.id = 0;
        timeLogModel.date = DateTimeUtility.getCurrentDate();
        timeLogModel.time = DateTimeUtility.getCurrentTime();
        timeLogModel.status = dtrViewModel.getLiveDataMenuTitle().getValue();
        timeLogModel.latitude = locationBroadcastReceiver.getMutableLiveDataLocation().getValue().latitude;
        timeLogModel.longitude = locationBroadcastReceiver.getMutableLiveDataLocation().getValue().longitude;
        timeLogModel.fileName = fileName;
        timeLogModel.filePath = filePath;
        timeLogModel.mocked = dtrViewModel.getMockLocationSharedPref().getValue();
        timeLogModel.uploaded = 0;

        return timeLogModel;
    }

    @Override
    public void onPause() {
        assistant.stop();
        super.onPause();

        if (locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
        getContext().unregisterReceiver(locationBroadcastReceiver);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationSettingsBuilder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        locationSettingsBuilder.setAlwaysShow(true);

        locationSettingsRequest = locationSettingsBuilder.build();

        PendingResult result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, locationSettingsRequest);

        settingsClient = LocationServices.getSettingsClient(context);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        result.setResultCallback(this);
    }

    @Override
    public void onConnectionSuspended(int i) { }

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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }
/*
    class CheckLocationAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            double currentLatitude= Double.parseDouble(locationIdentifier.latitude);
            double currentLongitude=  Double.parseDouble(locationIdentifier.longitude);

            if(userSection.equals("26"))
            {
                double negros1Lat=9.324598; double negros1Long=123.295488;//3m
                double negros2Lat=9.324568; double negros2Long=123.295589; //8m

                float[] negros1=new float[1];
                Location.distanceBetween(negros1Lat, negros1Long, currentLatitude, currentLongitude, negros1);

                float[] negros2=new float[1];
                Location.distanceBetween(negros2Lat, negros2Long, currentLatitude, currentLongitude, negros2);

                if( negros1[0] <= 3 || negros2[0] <= 8)
                {
                    locationIdentifier.visible = View.GONE;
                    locationIdentifier.message = getContext().getResources().getString(R.string.location_acquired);
                    locationIdentifier.colorResource = R.color.location_acquired;
                }
                else
                {
                    locationIdentifier.visible = View.VISIBLE;
                    locationIdentifier.message = getContext().getResources().getString(R.string.location_outside);
                    locationIdentifier.colorResource = R.color.delete;
                }
            }
            else //others except negros
            {
                double refLat=10.308289554184235;      double refLong=123.89241318125381;//6.5
                double supplyLat=10.307250816452036;   double supplyLong=123.89204275226837;//12
                double centerLat=10.30738756635405; double centerLong=123.89330341980981; //47m
                double entranceLat=10.30774657014178; double entranceLong=123.89347537164254;//16m
                double exitLat=10.307210077158915; double exitLong=123.89368135704788;//19m
                double msdLat=10.307673999820983; double msdLong=123.89297111634774; //8m
                double nonComLat=10.308244; double nonComLong=123.907708; //5m
                double srpLat=10.262957; double srpLong=123.872609; //50m

                float[] refResult=new float[1];
                Location.distanceBetween(refLat, refLong, currentLatitude, currentLongitude, refResult);
                float[] supplyResult=new float[1];
                Location.distanceBetween(supplyLat, supplyLong, currentLatitude, currentLongitude, supplyResult);

                //doh compound
                float[] centerResult=new float[1];
                Location.distanceBetween(centerLat, centerLong, currentLatitude, currentLongitude, centerResult);

                float[] entranceResult=new float[1];
                Location.distanceBetween(entranceLat, entranceLong, currentLatitude, currentLongitude, entranceResult);

                float[] exitResult=new float[1];
                Location.distanceBetween(exitLat, exitLong, currentLatitude, currentLongitude, exitResult);

                float[] msdResult=new float[1];
                Location.distanceBetween(msdLat, msdLong, currentLatitude, currentLongitude, msdResult);

                float[] nonComResult = new float[1];
                Location.distanceBetween(nonComLat, nonComLong, currentLatitude, currentLongitude, nonComResult);

                float[] srpResult = new float[1];
                Location.distanceBetween(srpLat, srpLong, currentLatitude, currentLongitude, srpResult);

                if( centerResult[0]<=47 || msdResult[0] <=8 || entranceResult[0] <=16 || exitResult[0] <= 19 || (refResult[0] <= 6.5 && userSection.equals("51")) ||
                        (supplyResult[0] <= 12 && userSection.equals("12")) || (nonComResult[0] <= 6 && userSection.equals("28")) || (srpResult[0] <= 50 && userSection.equals("60")))
                {
                    locationIdentifier.visible = View.VISIBLE;
                    locationIdentifier.message = getContext().getResources().getString(R.string.location_acquired);
                    locationIdentifier.colorResource = R.color.location_acquired;
                }
                else
                {
                     locationIdentifier.visible = View.VISIBLE;
                    locationIdentifier.message = getContext().getResources().getString(R.string.location_outside);
                    locationIdentifier.colorResource = R.color.delete;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            locationBroadcastReceiver.getMutableLiveDataLocation().setValue(locationIdentifier);
            super.onPostExecute(aVoid);
        }
    }*/

    /*class CheckLocationLoopAsyncTask extends AsyncTask<Context, Void, Void> {

        @Override
        protected Void doInBackground(Context... contexts) {

            double currentLatitude= Double.parseDouble(locationIdentifier.latitude);
            double currentLongitude=  Double.parseDouble(locationIdentifier.longitude);

            if(userSection.equals("26"))
            {
                double negros1Lat=9.324598; double negros1Long=123.295488;//3m
                double negros2Lat=9.324568; double negros2Long=123.295589; //8m

                float[] negros1=new float[1];
                Location.distanceBetween(negros1Lat, negros1Long, currentLatitude, currentLongitude, negros1);

                float[] negros2=new float[1];
                Location.distanceBetween(negros2Lat, negros2Long, currentLatitude, currentLongitude, negros2);

                if( negros1[0] <= 3 || negros2[0] <= 8) {
                    locationIdentifier.visible = View.GONE;
                    locationIdentifier.message = contexts[0].getResources().getString(R.string.location_acquired);
                    locationIdentifier.colorResource = R.color.location_acquired;
                }
                else {
                     locationIdentifier.visible = View.VISIBLE;
                    locationIdentifier.message = contexts[0].getResources().getString(R.string.location_outside);
                    locationIdentifier.colorResource = R.color.delete;
                }
            }
            else //others
            {
                double refLat=10.308289554184235;      double refLong=123.89241318125381;//6.5
                double supplyLat=10.307250816452036;   double supplyLong=123.89204275226837;//12

                double centerLat=10.30738756635405;    double centerLong=123.89330341980981; //47m
                double entranceLat=10.30774657014178;  double entranceLong=123.89347537164254;//16m
                double exitLat=10.307210077158915;     double exitLong=123.89368135704788;//19m
                double msdLat=10.307673999820983;      double msdLong=123.89297111634774; //8m

                double nonComLat=10.308244;            double nonComLong=123.907708; //5m
                double srpLat=10.262957;               double srpLong=123.872609; //50m

                for(int x=0; x<8 ; x++) {
                    /Log.e("asyncloc", "x= "+x);
                    switch (x) {
                        //doh compound
                        case 0:
                            float[] centerResult = new float[1];
                            Location.distanceBetween(centerLat, centerLong, currentLatitude, currentLongitude, centerResult);
                            if (centerResult[0] <= 47) {
                                locationIdentifier.message = contexts[0].getResources().getString(R.string.location_acquired);
                                locationIdentifier.colorResource = R.color.location_acquired;
                                x=8;
                            } else {
                                locationIdentifier.message = contexts[0].getResources().getString(R.string.location_outside);
                            }
                            break;
                        case 1:
                            float[] entranceResult = new float[1];
                            Location.distanceBetween(entranceLat, entranceLong, currentLatitude, currentLongitude, entranceResult);
                            if (entranceResult[0] <= 16) {
                                locationIdentifier.message = contexts[0].getResources().getString(R.string.location_acquired);
                                locationIdentifier.colorResource = R.color.location_acquired;
                                x=8;
                            } else {
                                locationIdentifier.message = contexts[0].getResources().getString(R.string.location_outside);
                            }
                            break;
                        case 2:
                            float[] exitResult = new float[1];
                            Location.distanceBetween(exitLat, exitLong, currentLatitude, currentLongitude, exitResult);
                            if (exitResult[0] <= 19) {
                                locationIdentifier.message = contexts[0].getResources().getString(R.string.location_acquired);
                                locationIdentifier.colorResource = R.color.location_acquired;
                                x=8;
                            } else {
                                locationIdentifier.message = contexts[0].getResources().getString(R.string.location_outside);
                            }
                            break;
                        case 3:
                            float[] msdResult = new float[1];
                            Location.distanceBetween(msdLat, msdLong, currentLatitude, currentLongitude, msdResult);
                            if (msdResult[0] <= 8) {
                                locationIdentifier.message = contexts[0].getResources().getString(R.string.location_acquired);
                                locationIdentifier.colorResource = R.color.location_acquired;
                                x=8;
                            } else {
                                locationIdentifier.message = contexts[0].getResources().getString(R.string.location_outside);
                            }
                            break;
                        case 4:
                            float[] refResult = new float[1];
                            Location.distanceBetween(refLat, refLong, currentLatitude, currentLongitude, refResult);
                            if (refResult[0] <= 6.5 && userSection.equals("51")) {
                                locationIdentifier.message = contexts[0].getResources().getString(R.string.location_acquired);
                                locationIdentifier.colorResource = R.color.location_acquired;
                                x=8;
                            } else {
                                locationIdentifier.message = contexts[0].getResources().getString(R.string.location_outside);
                            }
                            break;
                        case 5:
                            float[] supplyResult = new float[1];
                            Location.distanceBetween(supplyLat, supplyLong, currentLatitude, currentLongitude, supplyResult);
                            if (supplyResult[0] <= 12 && userSection.equals("12")) {
                                x=8;
                                locationIdentifier.message = contexts[0].getResources().getString(R.string.location_acquired);
                                locationIdentifier.colorResource = R.color.location_acquired;
                            } else {
                                locationIdentifier.message = contexts[0].getResources().getString(R.string.location_outside);
                            }
                            break;
                        case 6:
                            float[] nonComResult = new float[1];
                            Location.distanceBetween(nonComLat, nonComLong, currentLatitude, currentLongitude, nonComResult);
                            if (nonComResult[0] <= 6 && userSection.equals("28")) {
                                locationIdentifier.message = contexts[0].getResources().getString(R.string.location_acquired);
                                locationIdentifier.colorResource = R.color.location_acquired;
                                x=8;
                            } else {
                                locationIdentifier.message = contexts[0].getResources().getString(R.string.location_outside);
                            }
                            break;
                        case 7: //(srpResult[0] <= 50 && userSection.equals("60"))
                            float[] srpResult = new float[1];
                            Location.distanceBetween(srpLat, srpLong, currentLatitude, currentLongitude, srpResult);
                            if (srpResult[0] <= 50 && userSection.equals("60")) {
                                locationIdentifier.message = contexts[0].getResources().getString(R.string.location_acquired);
                                locationIdentifier.colorResource = R.color.location_acquired;
                                x=8;
                            } else {
                                locationIdentifier.message = contexts[0].getResources().getString(R.string.location_outside);
                            }
                            break;

                    }
                    if (x==7 && !locationIdentifier.message.equals(contexts[0].getResources().getString(R.string.location_acquired))) {
                        locationIdentifier.message = contexts[0].getResources().getString(R.string.location_outside);
                        locationIdentifier.visible = View.VISIBLE;
                        locationIdentifier.colorResource = R.color.delete;
                    }
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            locationBroadcastReceiver.getMutableLiveDataLocation().setValue(locationIdentifier);
            super.onPostExecute(aVoid);
        }
    }*/

    class CheckLocationLoopAsyncTask extends AsyncTask<Context, Void, Void> {
        @Override
        protected Void doInBackground(Context... contexts) {
            double currentLatitude= Double.parseDouble(locationIdentifier.latitude);
            double currentLongitude=  Double.parseDouble(locationIdentifier.longitude);
           // List<LocationModel> list_location = dtrViewModel.getListLiveData_locationAssignments().getValue();
            //Log.e("asyncloc", "before if list_loc null" );
            if(currentUser.area_of_assignment_roles==1){
                if (list_location!=null){
                    if(list_location.size()!=0){
                        for(int x=0; x<list_location.size() ; x++) {
                            float[] result = new float[1];
                            Location.distanceBetween(Double.parseDouble(list_location.get(x).latitude), Double.parseDouble(list_location.get(x).longitude), currentLatitude, currentLongitude, result);
                            //Log.e("asyncloc", "fragment x= " +x );

                            if(result[0] <= Float.parseFloat(list_location.get(x).radius)){
                                locationIdentifier.message = contexts[0].getResources().getString(R.string.location_acquired);
                                locationIdentifier.colorResource = R.color.location_acquired;
                                locationIdentifier.visible = View.GONE;
                                x=list_location.size();
                            }
                            else {
                                locationIdentifier.message = contexts[0].getResources().getString(R.string.location_outside);
                                locationIdentifier.visible = View.GONE;
                                locationIdentifier.colorResource = R.color.delete;
                            }
                        }
                    }else {
                        locationIdentifier.message = contexts[0].getResources().getString(R.string.location_empty);
                        locationIdentifier.visible = View.GONE;
                        locationIdentifier.colorResource = R.color.delete;
                    }
                } else {
                    locationIdentifier.message = contexts[0].getResources().getString(R.string.location_empty);
                    locationIdentifier.visible = View.GONE;
                    locationIdentifier.colorResource = R.color.delete;
                }
            }else { // office personnel & area_of_assignment_roles=0
                locationIdentifier.message = contexts[0].getResources().getString(R.string.location_office_no_area_roles);
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

    //Location Assistant Listener

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

         displayDialogMockLocation();
        if(dtrViewModel.getMockLocationSharedPref().getValue()==null){
            dtrViewModel.insertMockLocationCreatedAt(DateTimeUtility.getCurrentDateTime());
        }

    }

    @Override
    public void onError(LocationAssistant.ErrorType type, String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
