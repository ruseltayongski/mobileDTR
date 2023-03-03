package com.dohro7.officemobiledtr.viewmodel;

import android.app.Application;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.dohro7.officemobiledtr.model.UserModel;
import com.dohro7.officemobiledtr.repository.ResetRepository;

public class ResetViewModel extends AndroidViewModel
{
    private LiveData<String> username;
    private ResetRepository resetRepository;
    private LiveData<UserModel> currentUser;
    private LiveData<String> resetErrorMessage;
    private LiveData<String> checkedUsername;

    public ResetViewModel(@NonNull Application application)
    {
        super(application);
        resetRepository=new ResetRepository(application);
        username=resetRepository.getUsername();
        currentUser=resetRepository.getCurrentUser();
        resetErrorMessage=resetRepository.getResetErrorMessage();
        checkedUsername=resetRepository.getCheckedUsername();

    }

    public void resetPassword(String userid,String reset_userid)
    {   resetRepository.resetPassword(userid,reset_userid);  }

    public void checkUsername(String reset_userid)
    { resetRepository.checkUsername(reset_userid);}

    public LiveData<String> getUsername() {   return  username;   }

    public LiveData<String> getResetErrorMessage() {   return  resetErrorMessage;   }

    public LiveData<UserModel> getCurrentUser() {   return  currentUser;   }

    public LiveData<String> getCheckedUsername() { return checkedUsername; }

}
