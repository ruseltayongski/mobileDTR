package com.dohro7.officemobiledtr.view.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dohro7.officemobiledtr.model.AnnouncementModel;
import com.dohro7.officemobiledtr.model.UserModel;
import com.dohro7.officemobiledtr.scheduler.DailyTaskScheduler;
import com.dohro7.officemobiledtr.utility.DateTimeUtility;
import com.dohro7.officemobiledtr.utility.SystemUtility;
import com.dohro7.officemobiledtr.view.fragment.FlagFragment;
import com.dohro7.officemobiledtr.view.fragment.LocationFragment;
import com.dohro7.officemobiledtr.view.fragment.ResetFragment;
import com.dohro7.officemobiledtr.viewmodel.DashboardViewModel;
import com.dohro7.officemobiledtr.viewmodel.DtrViewModel;
import com.dohro7.officemobiledtr.viewmodel.LoginViewModel;
import com.dohro7.officemobiledtr.viewmodel.SoftwareUpdateViewModel;
import com.google.android.material.navigation.NavigationView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dohro7.officemobiledtr.R;
import com.dohro7.officemobiledtr.view.fragment.CTOFragment;
import com.dohro7.officemobiledtr.view.fragment.DtrFragment;
import com.dohro7.officemobiledtr.view.fragment.LeaveFragment;
import com.dohro7.officemobiledtr.view.fragment.OfficeOrderFragment;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private DtrFragment dtrFragment = new DtrFragment();
    private LeaveFragment leaveFragment = new LeaveFragment();
    private OfficeOrderFragment officeOrderFragment = new OfficeOrderFragment();
    private CTOFragment ctoFragment = new CTOFragment();
    private ResetFragment resetFragment = new ResetFragment();
    private LocationFragment locationFragment = new LocationFragment();
    private FlagFragment flagFragment = new FlagFragment();

    private  SoftwareUpdateViewModel softwareUpdateViewModel;
    private LoginViewModel loginViewModel;
    private DtrViewModel dtrViewModel;

boolean updated=true;

    // private SoftwareUpdateActivity softwareUpdateFragment=new SoftwareUpdateActivity();//////doesnt exist on original

    private FragmentTransaction ft;
    private DashboardViewModel dashboardViewModel;
    private TextView username;
    private TextView userId;
    private TextView versionNumber;
    private TextView currentIp_tv;
    private TextView userRegion;
    private String currentUserId;

    //Uncomment if you want to perform screen shot
   /* @Override
    public void onBackPressed() {
        File screenShotFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Screenshot");
        Bitmap bitmap = BitmapDecoder.screenShotView(this);
        String fileName = "screenshot_drawer.jpg";
        File imageFolderFile = new File(screenShotFile, fileName);
        imageFolderFile.getParentFile().mkdirs();
        try {
            OutputStream fout = new FileOutputStream(imageFolderFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
            fout.flush();
            fout.close();
        } catch (FileNotFoundException e) {
            Log.e("FNOE", e.getMessage());
        } catch (IOException e) {
            Log.e("IOE", e.getMessage());
        }
        Toast.makeText(this, "Screen captured", Toast.LENGTH_SHORT).show();
    }*/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_main);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.navigation_view);
        drawerLayout = findViewById(R.id.drawerlayout);

        toolbar.setTitle("Daily Time Record");
        toolbar.setTitleTextColor(Color.WHITE);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        //navigationView.getMenu().findItem(R.id.reset_menu).setVisible(false);  //Hide Reset DTR password MENU

        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.dashboard_content, dtrFragment).commit();

        dtrViewModel = ViewModelProviders.of(this).get(DtrViewModel.class);

       //SOFTWARE UPDATE - change into announcement
        softwareUpdateViewModel = ViewModelProviders.of(this).get(SoftwareUpdateViewModel.class);
        softwareUpdateViewModel.checkAnnouncement();

        softwareUpdateViewModel.getMutableAnnouncement().observe(this, new Observer<AnnouncementModel>() {
            @Override
            public void onChanged(AnnouncementModel announcementModel) {
                Log.e("updated", "Check");
                if(announcementModel!=null){
                    if (announcementModel.code==1 && announcementModel.message!=null) {   //has announcement
                        displayDialogNotUpdated(announcementModel.message);
                    }
                }
            }
        });

        softwareUpdateViewModel.getMutable_errorMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Log.e("updated", "Check");
                if(s!=null){
                    Toast.makeText(getBaseContext(), s, Toast.LENGTH_SHORT).show();
                }
            }
        });

        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);

        loginViewModel.getIpAddress_mutable().observe(this,  new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s!=null){
                    if(!s.trim().isEmpty()){
                        final String text=getString(R.string.current_ip_label)+"\n"+s;
                        currentIp_tv.setText(text);
                    }
                    else{
                        loginViewModel.InsertIp(getString(R.string.default_ip));
                    }
                }else {
                    loginViewModel.InsertIp(getString(R.string.default_ip));
                }
            }
        });

        loginViewModel.getUpdateMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Toast.makeText(getBaseContext(), s, Toast.LENGTH_SHORT).show();
            }
        });

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.addHeaderView(navigationHeader());

        dashboardViewModel = ViewModelProviders.of(this).get(DashboardViewModel.class);

        dtrViewModel.getDeleteMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s!=null){
                    Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                }
            }
        });
        dashboardViewModel.getUserModel().observe(this, new Observer<UserModel>() {
            @Override
            public void onChanged(UserModel userModel) {
                if (userModel != null)
                {
                    currentUserId=userModel.getUserId();

                    if(toolbar.getTitle().equals("Daily Time Record")){
                        toolbar.setTitle("" + currentUserId);
                    }

                    username.setText(userModel.getName());
                    userId.setText("User ID: "+userModel.getUserId());
                    userRegion.setText(userModel.getRegion().replace("_"," : ").toUpperCase());
                    versionNumber.setText("MobileDTR version " + SystemUtility.getVersionName(DashboardActivity.this));

                    if(userModel.getAuthority()!=null)
                    {
                        if(userModel.getAuthority().equalsIgnoreCase("reset_password"))
                        { navigationView.getMenu().findItem(R.id.reset_menu).setVisible(true); }
                        else {navigationView.getMenu().findItem(R.id.reset_menu).setVisible(false);}
                    }
                    else {navigationView.getMenu().findItem(R.id.reset_menu).setVisible(false);}
                }
                else{
                    username.setText("");
                    userId.setText("");
                    versionNumber.setText("");
                    userRegion.setText("");
                }
            }
        });
        //new DailyTaskScheduler().setAlarm(this);
    }

    public void displayDialogNotUpdated(String announcement) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_announcement);
        final TextView announcementMsgs= dialog.findViewById(R.id.dialog_announcement);
        announcementMsgs.setText(announcement);
        dialog.findViewById(R.id.dialog_announcement_ok).setOnClickListener(new View.OnClickListener() {
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


    public View navigationHeader() {
        View view = LayoutInflater.from(this).inflate(R.layout.user_layout, null, false);
        username = view.findViewById(R.id.user_name);
        userId=view.findViewById(R.id.user_id);
        versionNumber=view.findViewById(R.id.version_number);
        userRegion = view.findViewById(R.id.user_region);
        currentIp_tv =view.findViewById(R.id.current_ip);
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (drawerLayout.isDrawerOpen(GravityCompat.START))
                {   drawerLayout.closeDrawer(GravityCompat.START);  }
                else
                {
                    if(SystemUtility.isConnectedToInternet(this))
                    { refreshUserModel(); }
                    drawerLayout.openDrawer(GravityCompat.START);
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        ft = getSupportFragmentManager().beginTransaction();
        switch (menuItem.getItemId()) {
            case R.id.dtr_menu:
                toolbar.setTitle("" + currentUserId);
                ft.replace(R.id.dashboard_content, dtrFragment).commit();
                break;
            case R.id.leave_menu:
                toolbar.setTitle("Leave");
                ft.replace(R.id.dashboard_content, leaveFragment).commit();
                break;
            case R.id.so_menu:
                toolbar.setTitle("Office Order");
                ft.replace(R.id.dashboard_content, officeOrderFragment).commit();
                break;
            case R.id.cto_menu:
                toolbar.setTitle("Compensatory Time Off");
                ft.replace(R.id.dashboard_content, ctoFragment).commit();
                break;
            case R.id.flag_menu:
                toolbar.setTitle("Flag Attendance");
                ft.replace(R.id.dashboard_content, flagFragment).commit();
                break;
            case R.id.reset_menu:
                toolbar.setTitle("RESET DTR PASSWORD");
                ft.replace(R.id.dashboard_content,resetFragment).commit();
                break;
            case R.id.software_update_menu:
                Intent softwareUpdateIntent = new Intent(this, SoftwareUpdateActivity.class);
                startActivity(softwareUpdateIntent);
               /* toolbar.setTitle("Software Update");
                ft.replace(R.id.dashboard_content, softwareUpdateFragment).commit(); //for fragment
               */
                break;
            case R.id.change_ip_menu:
                displayUpdateIpAddressDialog();
                break;
            case R.id.location_menu:
                toolbar.setTitle("Area of Assignments");
                ft.replace(R.id.dashboard_content, locationFragment).commit();
                /*dtrViewModel.getLocationAssignmentsFromServer(currentUserId);*/
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }

    private void displayDeleteDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

    public void displayUpdateIpAddressDialog()
    {
        final Dialog ipdialog = new Dialog(this);
        ipdialog.setContentView(R.layout.dialog_update_ip);
        final EditText ipAddress = ipdialog.findViewById(R.id.dialog_ip);

        final Button updateIPbtn = ipdialog.findViewById(R.id.dialog_update_ip);

        updateIPbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("ip","IP button clicked");
                try
                {
                    if (ipAddress.getText().toString().trim().isEmpty())
                    {
                        ipAddress.setError("Required");
                        ipAddress.requestFocus();
                    }
                    else
                    {
                        loginViewModel.InsertIp(ipAddress.getText().toString());
                        ipdialog.dismiss();
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                    Log.e("error update ip", "error updating ip address");
                }
            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(ipdialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        ipdialog.getWindow().setAttributes(lp);
        ipdialog.show();
    }

    @SuppressLint("HardwareIds")
    public void refreshUserModel()
    {
        String imei="";
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 0);
            return;
        }

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O  && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ) { ///O/P
            imei = telephonyManager.getImei();
        }
        else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) { //Q=10
            imei=mac();
        }
        else if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){ //R=11+
            try{
                imei = Settings.Secure.getString(
                        getApplicationContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID);
            }
            catch (Exception e){
                Toast.makeText(getApplicationContext(), ""+e.getMessage(), Toast.LENGTH_SHORT ).show();
            }
        }
        else {
            imei = telephonyManager.getDeviceId();
        }
        dashboardViewModel.checkUserModel(imei);
    }

    public String mac()
    {
//        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
//
//        if (pairedDevices.size() > 0) {
//            // There are paired devices. Get the name and address of each paired device.
//            for (BluetoothDevice device : pairedDevices) {
//                String deviceName = device.getName();
//                String deviceHardwareAddress = device.getAddress(); // MAC addressturn
//                return deviceHardwareAddress;
//            }
//        }
//        return "";
        try {

            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
//            Log.e("count","" + all.toArray().length);
            for (NetworkInterface nif : all) {
//                Log.e("NetworkName","" + nif.getDisplayName() + " : " + nif.getName());
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null)
                {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }


}
