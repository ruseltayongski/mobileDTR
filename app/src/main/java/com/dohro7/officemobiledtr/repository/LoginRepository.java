package com.dohro7.officemobiledtr.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dohro7.officemobiledtr.model.ResponseBody;
import com.dohro7.officemobiledtr.model.UploadResponse;
import com.dohro7.officemobiledtr.model.UserModel;
import com.dohro7.officemobiledtr.repository.remote.RetrofitApi;
import com.dohro7.officemobiledtr.repository.remote.RetrofitClient;
import com.dohro7.officemobiledtr.repository.sharedpreference.IpAddressSharedPreference;
import com.dohro7.officemobiledtr.repository.sharedpreference.UserSharedPreference;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginRepository {
    private MutableLiveData<UserModel> currentUser;
    private UserSharedPreference userSharedPreference;
    private MutableLiveData<String> loginErrorMessage;
    private RetrofitApi retrofitApi;
    private MutableLiveData<String> updateMessage; //r
    private IpAddressSharedPreference ipAddressSharedPreference;
    private MutableLiveData<String> mutable_ip;
    private Context context;

    public LoginRepository(Context context) {
        userSharedPreference = UserSharedPreference.getInstance(context); //if naay UserSharedPreference else create
        currentUser = userSharedPreference.getUserModel();
        ipAddressSharedPreference=IpAddressSharedPreference.getInstance(context);
        mutable_ip=ipAddressSharedPreference.getIpAddress_mutable();
        loginErrorMessage = new MutableLiveData<>();
        retrofitApi = RetrofitClient.getRetrofitApi(context);
        this.context = context;
        updateMessage = new MutableLiveData<>(); //r
    }

    public LiveData<UserModel> getCurrentUser() { return currentUser; }

    public LiveData<String> getLoginErrorMessage() { return loginErrorMessage; }

    public LiveData<String> getUpdateMessage() { return updateMessage; }

    public void login(String imei) {
       /* UserModel userModel = new UserModel("00000001", "Romaine", "Lorena", "reset_password", "42", "DMO");
        userSharedPreference.insertUser(userModel);
        currentUser.setValue(userModel);*/

       retrofitApi = RetrofitClient.getRetrofitApi(this.context);
        try{
            Call<ResponseBody> userViewModelCall = retrofitApi.login(imei);
            userViewModelCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    ResponseBody responseBody = response.body();
                   if(responseBody!=null){
                       if (responseBody.code == 200) {
                           userSharedPreference.insertUser(responseBody.response);
                           currentUser.setValue(responseBody.response);
                           return;
                       }
                       if (responseBody.code == 201) {
                           loginErrorMessage.setValue("ID not registered, please \'Register ID\'");
                           return;
                       }
                       if (response.code() == 500 || responseBody.code == 500) {
                           loginErrorMessage.setValue("Server error, please contact system administrator\nResponseCode:500");
                       }
                   }else {
                       loginErrorMessage.setValue(response.message() + ", please contact system administrator");
                   }


                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    if (t instanceof IOException)
                    { loginErrorMessage.setValue("No network connection"); }
                    else { loginErrorMessage.setValue(t.getMessage()); }
                    Log.e("onFailure", t.getMessage());
                }
            });
        }
        catch (Exception e){
            loginErrorMessage.setValue(e.getMessage());
        }
    }

    public void updateImei(String imei, String userid) {
        Call<UploadResponse> userUpdateImeilCall = retrofitApi.imei(imei,userid);
        userUpdateImeilCall.enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                UploadResponse responseBody = response.body();
                if(responseBody!=null){
                    if(responseBody.code == 200){
                        updateMessage.setValue(responseBody.response);
                        return;
                    }
                    updateMessage.setValue(responseBody.response);
                }else {
                    updateMessage.setValue(response.message() + ", please contact system administrator");
                }
            }
            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                if (t instanceof IOException)
                { loginErrorMessage.setValue("No network connection"); }
                else { loginErrorMessage.setValue(t.getMessage()); }
                Log.e("onFailure", t.getMessage());
            }
        });
    }
    public void InsertIp(String ipAddress ) {
        ipAddressSharedPreference.insertUpdateIP(ipAddress);
        retrofitApi = RetrofitClient.getRetrofitApi(this.context);
        updateMessage.setValue("Your current IP address now is: " + ipAddress);
    }

    public MutableLiveData<String> getIpAddress_mutable(){
        return mutable_ip;
    }

}
