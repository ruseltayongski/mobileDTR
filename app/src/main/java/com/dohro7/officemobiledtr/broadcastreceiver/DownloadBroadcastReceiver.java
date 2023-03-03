package com.dohro7.officemobiledtr.broadcastreceiver;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;


import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.net.URISyntaxException;

public class DownloadBroadcastReceiver extends BroadcastReceiver {
    private MutableLiveData<Boolean> mutableDownloadCompleted = new MutableLiveData<>();
    private File apkFile; Intent intent;
    @Override
    public void onReceive(Context context, Intent intent)
    {


        File toInstall = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "dtr.apk");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri apkUri = FileProvider.getUriForFile(context,  context.getPackageName() + ".fileprovider", toInstall);//(activity, BuildConfig.APPLICATION_ID + ".fileprovider", toInstall);
            intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.setData(apkUri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Log.e("Download", "here1 + " + intent.getData());
        } else {
            Uri apkUri = Uri.fromFile(toInstall);
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Log.e("Download", "here2");
        }

        try {
            wait(1000);
            Log.e("Download", "here3");
            context.startActivity(intent);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


       /* if(intent.getAction().equalsIgnoreCase(DownloadManager.ACTION_DOWNLOAD_COMPLETE)){
            mutableDownloadCompleted.setValue(true);

            //////automatic install/download
            apkFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Apk");
            File file = new File(apkFile, "dtr.apk");
            Uri apkUri = Uri.fromFile(file);

            Intent installIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            // intent= new Intent(Intent.ACTION_INSTALL_PACKAGE);
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //installIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Log.e("Download", "complete??");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            {
                Log.e("Download", "NOUGAT AND  ABOVE");
                // apkUri = FileProvider.getUriForFile(context,  context.getPackageName() + ".provider", apkFile);
                //installIntent.setData(apkUri);

            }
            else
            {
                installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                Log.e("Download", "NOUGAT AND  BELOW");
            }

            if (intent.resolveActivity(context.getPackageManager()) != null)
            {
                Log.e("Download","complete here");
                context.startActivity(installIntent);
                context.unregisterReceiver(this);
                ((Activity)context).finish();
            }

//            apkFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Apk");
//            File file = new File(apkFile, "dtr.apk");
//            Uri apkUri = Uri.fromFile(file);
//            Intent installIntent = new Intent(Intent.ACTION_VIEW);
//            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            installIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
//            {
//                //   Toast.makeText(context, "NOUGAT AND  ABOVE", Toast.LENGTH_SHORT).show();
//                apkUri = FileProvider.getUriForFile(context,  context.getPackageName() + ".provider", apkFile);
//                installIntent.setData(apkUri);
//                Log.e("progress", "NOUGAT AND  ABOVE");
//            }
//
//            else
//                {
//                    // Toast.makeText(context, "NOUGAT BELOW", Toast.LENGTH_SHORT).show();
//                    installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
//                    Log.e("progress", "NOUGAT AND  BELOW");
//                 }
//
//            if (installIntent.resolveActivity(context.getPackageManager()) != null)
//            {
//                context.startActivity(installIntent);
//                //context.unregisterReceiver(this);
//                ((Activity)context).finish();
//            }

        }*/
    }

    public MutableLiveData<Boolean> getMutableDownloadCompleted(){
        return mutableDownloadCompleted;
    }
}
