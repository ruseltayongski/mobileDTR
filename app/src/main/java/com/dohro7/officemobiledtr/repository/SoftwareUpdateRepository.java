package com.dohro7.officemobiledtr.repository;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dohro7.officemobiledtr.model.AnnouncementModel;
import com.dohro7.officemobiledtr.model.ForceUpdateResponse;
import com.dohro7.officemobiledtr.model.SoftwareResponseBody;
import com.dohro7.officemobiledtr.model.SoftwareUpdateModel;
import com.dohro7.officemobiledtr.repository.remote.RetrofitApi;
import com.dohro7.officemobiledtr.repository.remote.RetrofitClient;
import com.dohro7.officemobiledtr.repository.sharedpreference.IpAddressSharedPreference;

import java.io.File;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SoftwareUpdateRepository {
    private MutableLiveData<SoftwareUpdateModel> mutableSoftwareModel;
    private MutableLiveData<Double> mutableDownloadPercentage;

    private MutableLiveData<Boolean> mutableDownloadStatus;
    private MutableLiveData<AnnouncementModel> mutableAnnouncement;
    private MutableLiveData<ForceUpdateResponse> mutableForceUpdate;
    private MutableLiveData<String> mutable_errorMessage;
    private RetrofitApi retrofitApi;
    private DownloadManager downloadManager;
    private long id;
    private Handler handler = new Handler();
    private File apkFile;
    private Context context;
    private IpAddressSharedPreference ipAddressSharedPreference;
    private Runnable downloadRunnable = new Runnable() {
        @Override
        public void run() {

            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(id);

            Cursor c = downloadManager.query(query);
            if (c.moveToFirst()) {
                double progress = 0.0;
                int sizeIndex = c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
                int downloadedIndex = c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                long size = c.getInt(sizeIndex);
                long downloaded = c.getInt(downloadedIndex);

                if (size != -1) progress = downloaded * 100.0 / size;
                mutableDownloadPercentage.setValue(progress);

                Log.e("Progress", progress + " " + downloadedIndex);
                if (c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS)) != DownloadManager.STATUS_SUCCESSFUL) {
                    handler.postDelayed(this, 2000);
                }
            }
        }
    };

    public SoftwareUpdateRepository(Context context) {
        mutableSoftwareModel = new MutableLiveData<>();

        mutableDownloadPercentage = new MutableLiveData<>();
        downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        apkFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Apk");
        ipAddressSharedPreference=IpAddressSharedPreference.getInstance(context);
        mutableDownloadStatus=new MutableLiveData<>();
        this.context=context;
        mutableAnnouncement= new MutableLiveData<>();
        mutableForceUpdate = new MutableLiveData<>();
        mutable_errorMessage = new MutableLiveData<>();
    }

    public MutableLiveData<Double> getMutableDownloadPercentage()
    { return mutableDownloadPercentage; }

    public MutableLiveData<SoftwareUpdateModel> getMutableSoftwareModel()
    { return mutableSoftwareModel; }

    public MutableLiveData<Boolean> getMutableDownloadStatus()  //r
    { return mutableDownloadStatus; }


    public void downloadApkFromServer() { //download

        File file = new File(apkFile, "dtr.apk");
        if(file.exists())   {  file.delete();  }
        Uri apkUri = Uri.fromFile(file);
        // DownloadManager.Request request = new DownloadManager.Request(Uri.parse("http://192.168.100.17/dtr/public/apk/dtr.apk")); //local
        // DownloadManager.Request request = new DownloadManager.Request(Uri.parse("http://203.177.67.122/dtr/public/apk/dtr_3_0.apk")); //testing
        final String link= "http://" + ipAddressSharedPreference.getIpAddress_mutable().getValue().trim() +"/dtr/public/apk/dtr.apk" ;
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(link));//final /dtr. "http://222.127.126.35/dtr/public/apk/dtr_office.apk"
        request.setTitle("dtr.apk");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setDestinationUri(apkUri);
        id = downloadManager.enqueue(request);

        handler.postDelayed(downloadRunnable, 5000);
        Log.e("Downloading", file.getAbsolutePath());


        //set BroadcastReceiver to install app when .apk is downloaded
     /*  BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {

                Intent install = new Intent(Intent.ACTION_VIEW);
                install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                install.setDataAndType(uri,
                        downloadManager.getMimeTypeForDownloadedFile(id));
                startActivity(install);

                context.unregisterReceiver(this);
                install.finish();
            }
        };
        //register receiver for when .apk download is complete
        context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));*/




    }

   /* public void checkSoftwareUpdate() { //request
        retrofitApi = RetrofitClient.getRetrofitApi(context);
        Call<SoftwareResponseBody> softwareUpdateModelCall = retrofitApi.checkSoftwareUpdate();
        softwareUpdateModelCall.enqueue(new Callback<SoftwareResponseBody>() {
            @Override
            public void onResponse(Call<SoftwareResponseBody> call, Response<SoftwareResponseBody> response)   {
                 mutableSoftwareModel.setValue(response.body().softwareUpdateModel);
                Log.e("Response", response.body().softwareUpdateModel.versionName); //log version name
            }

            @Override
            public void onFailure(Call<SoftwareResponseBody> call, Throwable t)
            { mutableDownloadStatus.setValue(false);  }
        });
    }*/

    public void checkAnnouncement() { //request
        retrofitApi = RetrofitClient.getRetrofitApi(context);
        Log.e("Announcement" , "checkAnnouncementRepo");
        try{
            Call<AnnouncementModel> softwareUpdateModelCall = retrofitApi.checkAnnouncement();
            softwareUpdateModelCall.enqueue(new Callback<AnnouncementModel>() {
                @Override
                public void onResponse(Call<AnnouncementModel> call, Response<AnnouncementModel> response)    {
                    if(response!=null){
                        if(response.body()!=null)
                        mutableAnnouncement.setValue(response.body());
                    }
                }

                @Override
                public void onFailure(Call<AnnouncementModel> call, Throwable t)  {
                    Log.e("AnnouncementModel " , "on Failed");
                }
            });
        }
        catch (Exception e){
            mutable_errorMessage.setValue(e.getMessage());
        }
    }

    public MutableLiveData<String> getMutable_errorMessage()  //r
    { return mutable_errorMessage; }

    public LiveData<AnnouncementModel> getMutableAnnouncement()  //r
    { return mutableAnnouncement; }

//checkForceUpdate()
public void checkForceUpdate() { //request
    retrofitApi = RetrofitClient.getRetrofitApi(context);
    try {
        Call<ForceUpdateResponse> softwareUpdateModelCall = retrofitApi.checkForceUpdate();
        softwareUpdateModelCall.enqueue(new Callback<ForceUpdateResponse>() {
            @Override
            public void onResponse(Call<ForceUpdateResponse> call, Response<ForceUpdateResponse> response) {
                if(response!=null){
                    if(response.body()!=null){
                    mutableForceUpdate.setValue(response.body());
                    }
                }
            }

            @Override
            public void onFailure(Call<ForceUpdateResponse> call, Throwable t) {
                Log.e("ForceUpdateResponse " , "on Failed");
            }
        });
    }
    catch (Exception e){
        mutable_errorMessage.setValue(e.getMessage());
    }
}
    public LiveData<ForceUpdateResponse> getMutableForceUpdate()  //r
    { return mutableForceUpdate; }
}
