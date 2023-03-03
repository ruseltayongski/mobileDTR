package com.dohro7.officemobiledtr.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dohro7.officemobiledtr.model.UserModel;
import com.dohro7.officemobiledtr.repository.LoginRepository;

public class LoginViewModel extends AndroidViewModel {
    private LoginRepository loginRepository;
    private LiveData<UserModel> currentUser;
    private LiveData<String> loginErrorMessage;
    private LiveData<String> updateMessage;
    private MutableLiveData<String> mutable_ip;
    public LoginViewModel(@NonNull Application application)
    {
        super(application);
        loginRepository = new LoginRepository(application);
        loginErrorMessage = loginRepository.getLoginErrorMessage();
        currentUser = loginRepository.getCurrentUser();

        updateMessage = loginRepository.getUpdateMessage();
        mutable_ip=loginRepository.getIpAddress_mutable();
    }

    public LiveData<UserModel> getCurrentUser()
    {   return currentUser;     }

    public LiveData<String> getLoginErrorMessage()
    {   return loginErrorMessage;   }

    public LiveData<String> getUpdateMessage()
    {   return updateMessage;   }

    public void login(String imei)
    {   loginRepository.login(imei);   }


    public void updateImei(String imei,String userid)
    {   loginRepository.updateImei(imei,userid);    }

    public void InsertIp(String ipAddress )
    {   loginRepository.InsertIp(ipAddress );    }

    public MutableLiveData<String> getIpAddress_mutable(){
        return mutable_ip;
    }
}
