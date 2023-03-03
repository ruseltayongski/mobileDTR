package com.dohro7.officemobiledtr.repository;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dohro7.officemobiledtr.model.LeaveModel;
import com.dohro7.officemobiledtr.model.UploadResponse;
import com.dohro7.officemobiledtr.repository.remote.RetrofitApi;
import com.dohro7.officemobiledtr.repository.remote.RetrofitClient;
import com.dohro7.officemobiledtr.repository.local.AppDatabase;
import com.dohro7.officemobiledtr.repository.local.LeaveDao;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpCookie;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaveRepository {
    private LeaveDao leaveDao;
    private LiveData<List<LeaveModel>> listLiveData;
    private MutableLiveData<String> uploadMessage = new MutableLiveData<>();
    private Context context;
    public LeaveRepository(Context context)
    {
        this.leaveDao = AppDatabase.getInstance(context).leaveDao();
        this.listLiveData = leaveDao.getLeaves();

        this.context=context;
    }

    public LiveData<List<LeaveModel>> getListLiveData()
    {   return listLiveData;    }

    public MutableLiveData<String> getUploadMessage()
    {   return uploadMessage;   }

    public void insertLeave(LeaveModel leaveModel)
    {   new InsertAsyncTask().execute(leaveModel);   }

    public void deleteLeave(LeaveModel leaveModel)
    {   new DeleteAsyncTask().execute(leaveModel);  }

    public void uploadLeaves(JSONObject jsonObject)
    {
        RetrofitApi retrofitApi = RetrofitClient.getRetrofitApi(context);
        Call<UploadResponse> stringCall = retrofitApi.uploadLeaves(jsonObject);
        stringCall.enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                UploadResponse uploadResponse = response.body();
                if(uploadResponse!=null){
                    if (uploadResponse.code == 200) {
                        Log.e("Message", response.body().response);
                        uploadMessage.setValue(uploadResponse.response);
                        new DeleteAllAsyncTask().execute();
                        return;
                    }
                }
                uploadMessage.setValue("Something went wrong, please contact system administrator");
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                if (t instanceof IOException) {
                    uploadMessage.setValue("No network connection");
                }
                else {   uploadMessage.setValue(t.getMessage());     }
            }
        });
    }


    class InsertAsyncTask extends AsyncTask<LeaveModel, Void, Void>
    {

        @Override
        protected Void doInBackground(LeaveModel... leaveModels)
        {
            leaveDao.insertLeave(leaveModels[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            uploadMessage.setValue("New item added");
        }
    }

    class DeleteAllAsyncTask extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected Void doInBackground(Void... voids) {
            leaveDao.deleteAllLeave();
            return null;
        }
    }

    class DeleteAsyncTask extends AsyncTask<LeaveModel, Void, Void>
    {

        @Override
        protected Void doInBackground(LeaveModel... leaveModels)
        {
            leaveDao.deleteLeave(leaveModels[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            uploadMessage.setValue("Item deleted");
        }
    }
}
