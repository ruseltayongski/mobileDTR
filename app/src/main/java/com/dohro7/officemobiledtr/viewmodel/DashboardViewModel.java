package com.dohro7.officemobiledtr.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.dohro7.officemobiledtr.model.UserModel;
import com.dohro7.officemobiledtr.repository.LoginRepository;

public class DashboardViewModel extends AndroidViewModel {
    private LoginRepository loginRepository;
    private LiveData<UserModel> userModel;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        loginRepository = new LoginRepository(application);
        userModel = loginRepository.getCurrentUser();
    }

    public LiveData<UserModel> getUserModel() { return userModel; }
    public void checkUserModel(String imei){loginRepository.login(imei);}
}
