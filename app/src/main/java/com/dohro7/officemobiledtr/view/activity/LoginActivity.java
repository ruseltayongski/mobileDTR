package com.dohro7.officemobiledtr.view.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.dohro7.officemobiledtr.R;
import com.dohro7.officemobiledtr.model.UserModel;
import com.dohro7.officemobiledtr.utility.DateTimeUtility;
import com.dohro7.officemobiledtr.utility.BitmapDecoder;
import com.dohro7.officemobiledtr.viewmodel.LoginViewModel;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Set;


import android.app.Dialog;
import android.widget.TextView;
import android.widget.EditText;
import android.view.WindowManager;
import android.view.Gravity;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private LoginViewModel loginViewModel;
    private String imei;
    private Button loginButton;
    private ProgressBar progressBar;
    private ProgressBar progress_Bar;
  //  private ImageButton settingsButton;
    private int recentClick;
    private String update;
    Dialog dialog;
    Dialog ipdialog;

    private Button updateImeiBtn;
    private Button updateIPBtn;

    EditText userid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.imei_dialog_layout);
        ipdialog = new Dialog(this);
        ipdialog.setContentView(R.layout.dialog_update_ip);

        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);

        loginViewModel.getCurrentUser().observe(this, new Observer<UserModel>() {
            @Override
            public void onChanged(UserModel userModel) {
                if (userModel != null) {
                    Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    setContentView(R.layout.login_main);

                    loginButton = findViewById(R.id.login_btn);
                    progressBar = findViewById(R.id.progressbar);
                    //settingsButton = findViewById(R.id.settings_btn);

                    loginButton.setOnClickListener(LoginActivity.this);
                    //settingsButton.setOnClickListener(LoginActivity.this);

                    updateImeiBtn=(Button)findViewById(R.id.update_imeibtn);
                    updateIPBtn = (Button)findViewById(R.id.update_ip_btn);

                    updateImeiBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) { settingButton(); }});

                    updateIPBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) { ipButton(); }});
                }
            }
        });

        loginViewModel.getLoginErrorMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                setButtonEnabled(true);
                setProgressVisibility(View.GONE);
                dialog.dismiss();
                Snackbar snackbar = Snackbar.make(findViewById(R.id.root), s, Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       // Log.e("log", "recent onClick " + recentClick);
                        switch (recentClick)
                        {
                            case 0: Log.e("log", "recent login btn ");login();  break;
                            case 1: Log.e("log", "recent login btn ");settingButton();  break;
                        }
                    }
                });

                snackbar.show();
            }
        });

        loginViewModel.getUpdateMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                switch (s){
                    case "Successfully IMEI":
                        dialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                        setButtonEnabled(true);
                        break;
                    case "Failed!":
                        userid.requestFocus();
                        userid.setError("IMEI update failed");
                        setProgressVisibility(View.GONE);
                        setButtonEnabled(true);
                        break;
                }
                setButtonEnabled(true);
                setProgressVisibility(View.GONE);
            }
        });

       /* Toast.makeText(getContext(), ""+mac(getContext()) ,Toast.LENGTH_LONG).show();
        Log.e("bluetooth", "   " + mac (this));*/

    }

    public void ipButton()
    {
        final EditText ipAddress = ipdialog.findViewById(R.id.dialog_ip);

        final Button updateIPbtn = ipdialog.findViewById(R.id.dialog_update_ip);

        progress_Bar= ipdialog.findViewById(R.id.dialog_ip_progress_bar);
        progressBar.bringToFront();

        updateIPbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("ip","IP button clicked");
                try
                {
                    if (ipAddress.getText().toString().trim().isEmpty())
                    {
                        progress_Bar.setVisibility(View.GONE);
                        ipAddress.setError("Required");
                        ipAddress.requestFocus();
                        setButtonEnabled(true);
                    }
                    else
                    {
                        progress_Bar.setVisibility(View.VISIBLE);
                        loginViewModel.InsertIp(ipAddress.getText().toString());
                        // loginViewModel.getUpdateMessage()
                        ipdialog.dismiss();
                    }progress_Bar.setVisibility(View.GONE);
                }
                catch(Exception e)
                {
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

        setButtonEnabled(true);
        progressBar.setVisibility(View.GONE);

    }

    //onclick
    @SuppressLint("HardwareIds")
    public void login() {
        //Check if permission is granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 0);
            return;
        }
        setButtonEnabled(false);
        setProgressVisibility(View.VISIBLE);

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
        //Log.e("IMEI", imei);
        //  imei = "869881031722476";       ////////////////imei static
        loginViewModel.login(imei);
    }

    //onclick
   // @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("HardwareIds")
    public void settingButton()
    {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 0);
            return;
        }

        setButtonEnabled(false);

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

        TextView mImei = dialog.findViewById(R.id.imei);
        mImei.setText(imei);

        final Button updateImei = dialog.findViewById(R.id.update_imei);

        progress_Bar= dialog.findViewById(R.id.progress_bar);
        progressBar.bringToFront();
        userid = dialog.findViewById(R.id.userid);

        updateImei.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {
                    if (userid.getText().toString().trim().isEmpty())
                    {
                        progress_Bar.setVisibility(View.GONE);
                        userid.setError("Required");
                        userid.requestFocus();
                        setButtonEnabled(true);
                    }
                    else
                    {
                        progress_Bar.setVisibility(View.VISIBLE);
                        loginViewModel.updateImei(imei,userid.getText().toString());

                        // loginViewModel.getUpdateMessage()
                    }progress_Bar.setVisibility(View.GONE);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    //Log.e("error update imei", "error updating imei");
                }
            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialog.getWindow().setAttributes(lp);
        dialog.show();

        setButtonEnabled(true);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        setButtonEnabled(true);
        // login();
    }

    //Uncomment if you want to perform screen shot
  /*  @Override
    public void onBackPressed() {
        File screenShotFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Screenshot");
        Bitmap bitmap = BitmapDecoder.screenShotView(this);
        String fileName = "screenshot_login.jpg";
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
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.login_btn: recentClick=0; login();  break;
            case R.id.settings_btn: recentClick=1; settingButton();
                progressBar.setVisibility(View.GONE); break;
        }
    }

    public void setButtonEnabled(boolean enabled){
        loginButton.setEnabled(enabled);
        updateImeiBtn.setEnabled(enabled);
        updateIPBtn.setEnabled(enabled);
        //settingsButton.setEnabled(enabled);

    }

    public void setProgressVisibility(int visibility){
        progressBar.setVisibility(visibility);
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
