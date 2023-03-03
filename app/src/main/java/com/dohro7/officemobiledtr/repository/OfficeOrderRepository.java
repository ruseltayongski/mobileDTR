package com.dohro7.officemobiledtr.repository;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dohro7.officemobiledtr.model.OfficeOrderModel;
import com.dohro7.officemobiledtr.model.UploadResponse;
import com.dohro7.officemobiledtr.repository.local.AppDatabase;
import com.dohro7.officemobiledtr.repository.local.OfficeOrderDao;
import com.dohro7.officemobiledtr.repository.remote.RetrofitApi;
import com.dohro7.officemobiledtr.repository.remote.RetrofitClient;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OfficeOrderRepository {
    private OfficeOrderDao officeOrderDao;
    private LiveData<List<OfficeOrderModel>> listLiveData;
    private MutableLiveData<String> uploadMessage = new MutableLiveData<>();
    private Context context;
    public OfficeOrderRepository(Context context) {
        this.officeOrderDao = AppDatabase.getInstance(context).officeOrderDao();
        this.listLiveData = officeOrderDao.getOfficeOrder();
        this.context=context;
    }


    public MutableLiveData<String> getUploadMessage() {
        return uploadMessage;
    }

    public LiveData<List<OfficeOrderModel>> getListLiveData() {
        return listLiveData;
    }

    public void uploadSo(JSONObject object) {
        RetrofitApi retrofitApi = RetrofitClient.getRetrofitApi(context);
        Call<UploadResponse> callUploadLogs = retrofitApi.uploadSo(object);
        callUploadLogs.enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                UploadResponse uploadResponse = response.body();
                if(uploadResponse!=null){
                    if (uploadResponse.code == 200) {
                        Log.e("Message", response.body().response);
                        uploadMessage.setValue(uploadResponse.response);
                        new DeleteAllAsnycTask().execute();
                        return;
                    }
                }
                uploadMessage.setValue("Something went wrong, please contact system administrator");
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                if (t instanceof IOException) {
                    uploadMessage.setValue("No network connection");
                } else {
                    uploadMessage.setValue(t.getMessage());
                }


            }
        });

    }

    public void insertOfficeOrder(OfficeOrderModel officeOrderModel) {
        new InsertAsyncTask().execute(officeOrderModel);
    }

    public void deleteOfficeOrder(OfficeOrderModel officeOrderModel) {
        new DeleteAsyncTask().execute(officeOrderModel);
    }

    class InsertAsyncTask extends AsyncTask<OfficeOrderModel, Void, Void> {

        @Override
        protected Void doInBackground(OfficeOrderModel... officeOrderModels) {
            officeOrderDao.insertOfficeOrder(officeOrderModels[0]);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            uploadMessage.setValue("New item added");
        }
    }

    class DeleteAllAsnycTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            officeOrderDao.deleteAllOfficerOrder();
            return null;
        }
    }

    class DeleteAsyncTask extends AsyncTask<OfficeOrderModel, Void, Void> {

        @Override
        protected Void doInBackground(OfficeOrderModel... officeOrderModels) {
            officeOrderDao.deleteOfficerOrder(officeOrderModels[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            uploadMessage.setValue("Item deleted");
        }
    }
}
