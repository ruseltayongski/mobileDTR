package com.dohro7.officemobiledtr.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dohro7.officemobiledtr.model.AnnouncementModel;
import com.dohro7.officemobiledtr.model.ForceUpdateResponse;
import com.dohro7.officemobiledtr.model.SoftwareUpdateModel;
import com.dohro7.officemobiledtr.repository.SoftwareUpdateRepository;

public class SoftwareUpdateViewModel extends AndroidViewModel {
    private MutableLiveData<SoftwareUpdateModel> mutableUpdateModel;
    private SoftwareUpdateRepository softwareUpdateRepository;
    private MutableLiveData<Double> mutableDownloadPercentage;
    private MutableLiveData<String> mutable_errorMessage;

    //r
    private MutableLiveData<Boolean> mutableDownloadStatus;
    private LiveData<AnnouncementModel> mutableAnnouncement;
    private LiveData<ForceUpdateResponse> mutableForceUpdate;
    //r

    public SoftwareUpdateViewModel(@NonNull Application application)
    {
        super(application);
        softwareUpdateRepository = new SoftwareUpdateRepository(application);
        mutableUpdateModel = softwareUpdateRepository.getMutableSoftwareModel();
        mutableDownloadPercentage = softwareUpdateRepository.getMutableDownloadPercentage();

        mutableDownloadStatus = softwareUpdateRepository.getMutableDownloadStatus();
        mutableAnnouncement = softwareUpdateRepository.getMutableAnnouncement();
        mutableForceUpdate=softwareUpdateRepository.getMutableForceUpdate();
        mutable_errorMessage = softwareUpdateRepository.getMutable_errorMessage();
    }

   /* public MutableLiveData<SoftwareUpdateModel> getMutableUpdateModel()
    { return mutableUpdateModel; }*/

    public MutableLiveData<Double> getMutableDownloadPercentage()
    { return mutableDownloadPercentage; }

   /* public void checkSoftwareUpdate()
    { softwareUpdateRepository.checkSoftwareUpdate();}*/

    public void downloadApkFromServer()
    {   softwareUpdateRepository.downloadApkFromServer();     }


    public MutableLiveData<Boolean> getMutableDownloadStatus()
    { return mutableDownloadStatus; }

    public MutableLiveData<String> getMutable_errorMessage()  //r
    { return mutable_errorMessage; }

//announcement
    public void checkAnnouncement()
    { softwareUpdateRepository.checkAnnouncement();}

    public LiveData<AnnouncementModel> getMutableAnnouncement()
    { return mutableAnnouncement; }

  //force update
 public void checkForceUpdate()
 { softwareUpdateRepository.checkForceUpdate();}

    public LiveData<ForceUpdateResponse> getMutableForceUpdate()
    {
        return  softwareUpdateRepository.getMutableForceUpdate();
    }

}
