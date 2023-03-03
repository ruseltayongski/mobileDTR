package com.dohro7.officemobiledtr.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dohro7.officemobiledtr.model.UserModel;
import com.dohro7.officemobiledtr.repository.remote.RetrofitApi;
import com.dohro7.officemobiledtr.repository.remote.RetrofitClient;
import com.dohro7.officemobiledtr.repository.sharedpreference.UserSharedPreference;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetRepository
{
    private RetrofitApi retrofitApi;
    private MutableLiveData<String> username;
    private MutableLiveData<String> resetErrorMessage;
    private MutableLiveData<UserModel> currentUser;
    private MutableLiveData<String> checkedUsername;
    private Context context;
    public ResetRepository(Context context) {
        UserSharedPreference userSharedPreference = UserSharedPreference.getInstance(context);
        currentUser = userSharedPreference.getUserModel();
        checkedUsername= new MutableLiveData<>();
        resetErrorMessage = new MutableLiveData<>();
        username = new MutableLiveData<>();
        this.context=context;
    }

    public void resetPassword(String userid, String reset_userid) {
        retrofitApi = RetrofitClient.getRetrofitApi(context);
        Call<String> call = retrofitApi.resetPassword(userid,reset_userid);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if(response!=null){
                    username.setValue(response.body());
                }
                else {
                    username.setValue("Something went wrong, please contact administrator");
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t)
            { resetErrorMessage.setValue(t.getMessage()); }
        });
    }

    public void checkUsername(final String reset_userid) {
        retrofitApi = RetrofitClient.getRetrofitApi(context);
        Call<String> call = retrofitApi.checkUsername(reset_userid);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if(response!=null){
                    checkedUsername.setValue(response.body());
                }else {
                    username.setValue("Something went wrong, please contact administrator");
                }
             /*   try
                { checkedUsername.setValue(response.body());
                    Log.e("reset","response = "+response.body());
                }
                catch (Exception ex) { ex.printStackTrace(); }*/
            }
            @Override
            public void onFailure(Call<String> call, Throwable t)
            { resetErrorMessage.setValue(t.getMessage()); }
        });
    }

    public LiveData<String> getCheckedUsername() {    return checkedUsername;    }

    public LiveData<String> getUsername() { return username; }

    public LiveData<String> getResetErrorMessage() { return resetErrorMessage; }

    public LiveData<UserModel> getCurrentUser() { return currentUser; }
}
