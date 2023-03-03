package com.dohro7.officemobiledtr.view.activity;

import android.app.DownloadManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.dohro7.officemobiledtr.R;
import com.dohro7.officemobiledtr.broadcastreceiver.DownloadBroadcastReceiver;
import com.dohro7.officemobiledtr.model.ForceUpdateResponse;
import com.dohro7.officemobiledtr.utility.SystemUtility;
import com.dohro7.officemobiledtr.viewmodel.LoginViewModel;
import com.dohro7.officemobiledtr.viewmodel.SoftwareUpdateViewModel;

public class SoftwareUpdateActivity extends AppCompatActivity {
    private SoftwareUpdateViewModel softwareUpdateViewModel;
    private DownloadBroadcastReceiver downloadBroadcastReceiver = new DownloadBroadcastReceiver();

    private CardView updatedContainer;
    private CardView newContainer;
    private Group progressContainer;
    private ProgressBar downloadProgressbar;
    private Button retry_webview_btn;
    private TextView softwareDetails;
    private TextView currentVersion;
    private TextView softwareTitle;
    private TextView downloading_text;
    private TextView softwareDetailsVersion;
    private LoginViewModel loginViewModel;
    private String webviewURL ="";
    private Group retryContainer;
    private  WebView myWebView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.software_update_layout);                 // set layout with progress bar

        updatedContainer = findViewById(R.id.software_update_updated_container);  ///system is updated
        newContainer = findViewById(R.id.software_update_new_container);            //theres a new updates container
        progressContainer = findViewById(R.id.software_update_progress_container);  //progress container, group (software_update_progressbar,software_update_progress_text) // progress bar and checking updates text
        downloadProgressbar = findViewById(R.id.downloading_progress);//progress bar pn new update container
        retry_webview_btn =findViewById(R.id.software_retry_webview);
        softwareDetails = findViewById(R.id.software_details_list); //Textview
        softwareTitle=findViewById(R.id.software_details_title); //Textview title
        /**uncomment this if webview is deleted*/// downloading_text =findViewById(R.id.downloading_text);//textview, downloading text
        softwareDetailsVersion=findViewById(R.id.software_details_version);

        retryContainer=findViewById(R.id.software_update_retry_container); //group container, retry btn and text

        currentVersion = findViewById(R.id.software_update_version); //Textview for current/updated version

        softwareUpdateViewModel = ViewModelProviders.of(this).get(SoftwareUpdateViewModel.class);
        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);

        softwareUpdateViewModel.getMutableForceUpdate().observe(this, new Observer<ForceUpdateResponse>() {
            @Override
            public void onChanged(ForceUpdateResponse model) {
                progressContainer.setVisibility(View.GONE);

                if (model.latest_version.equalsIgnoreCase( SystemUtility.getVersionName(SoftwareUpdateActivity.this)) ) { //is updated
                    newContainer.setVisibility(View.GONE);
                    currentVersion.setText("Version: " + model.latest_version);
                    updatedContainer.setVisibility(View.VISIBLE);

                } else { //new updates
                    updatedContainer.setVisibility(View.GONE);
                    softwareDetailsVersion.setText("Current version: " + SystemUtility.getVersionName(SoftwareUpdateActivity.this) + "\nLatest version: " + model.latest_version);
                    softwareDetails.setText(model.message);
                    newContainer.setVisibility(View.VISIBLE);
                }
            }
        });

     /*   downloadBroadcastReceiver.getMutableDownloadCompleted().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    //Log.e("Download",  "Complete softActivity " +aBoolean.booleanValue() + "");
                }
            }
        });*/

        softwareUpdateViewModel.getMutableDownloadPercentage().observe(this, new Observer<Double>() {
            @Override
            public void onChanged(Double aDouble) {  //download percentage
                /**uncomment this if webview is deleted*///  downloadProgressbar.setProgress((int) aDouble.doubleValue());
                //r
                if(aDouble.doubleValue() == 100)
                {
                    Toast.makeText(getApplicationContext(), "Download Completed", Toast.LENGTH_SHORT).show();

                    /**uncomment this if webview is deleted*/
                    /* downloadProgressbar.setVisibility(View.GONE);
                    downloading_text.setVisibility(View.GONE);*/
                    softwareTitle.setText("To Install");
                    softwareDetails.setText(getString(R.string.toInstall));
                }
                //r
            }
        });


        //r
        softwareUpdateViewModel.getMutableDownloadStatus().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean bool) { ///if on failed check updates
                if(!bool)
                {
                    progressContainer.setVisibility(View.GONE);
                    retryContainer.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "Check Internet Connection",Toast.LENGTH_SHORT).show();
                }
            }
        });

        //r
        /**uncomment this if webview is deleted*/
        //software_details_button = button on software_version_update_layout, button download and install
        /*findViewById(R.id.software_details_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                softwareUpdateViewModel.downloadApkFromServer();
                //r
                  downloadProgressbar.setVisibility(View.VISIBLE);
                downloading_text.setVisibility(View.VISIBLE);
                findViewById(R.id.software_details_button).setVisibility(View.GONE);
                //r
            }
        });*/

        findViewById(R.id.software_update_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressContainer.setVisibility(View.VISIBLE);
                retryContainer.setVisibility(View.GONE);
                softwareUpdateViewModel.checkForceUpdate();
            }
        });
        softwareUpdateViewModel.checkForceUpdate();



        //WEBVIEW

        myWebView = findViewById(R.id.software_webview);
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
                if(downloadProgressbar!=null && downloadProgressbar.getVisibility()==View.VISIBLE)
                {
                    downloadProgressbar.setVisibility(View.GONE);
                    retry_webview_btn.setVisibility(View.VISIBLE);
                }
            }
        });

        retry_webview_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadProgressbar.setVisibility(View.VISIBLE);
                retry_webview_btn.setVisibility(View.INVISIBLE);
                myWebView.loadUrl(webviewURL);
            }
        });

        loginViewModel.getIpAddress_mutable().observe(this,  new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s!=null){
                    if(!s.trim().isEmpty()){
                        webviewURL ="http://" + s.trim() +    "/dtr/download_apk";
                    }
                    else{
                        webviewURL ="http://" +getString(R.string.default_ip) +    "/dtr/download_apk";
                    }
                }else {
                    webviewURL ="http://" +getString(R.string.default_ip) +    "/dtr/download_apk";
                }
                downloadProgressbar.setVisibility(View.VISIBLE);
                retry_webview_btn.setVisibility(View.INVISIBLE);
                myWebView.loadUrl(webviewURL);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(downloadBroadcastReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(downloadBroadcastReceiver);
    }
    //Uncomment if you want to perform screen shot
    /*@Override
    public void onBackPressed() {
        File screenShotFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Screenshot");
        Bitmap bitmap = BitmapDecoder.screenShotView(this);
        String fileName = "screenshot_software_update_updated.jpg";
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
    }
    */
}
