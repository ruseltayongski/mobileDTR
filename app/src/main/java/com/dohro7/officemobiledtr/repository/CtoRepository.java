package com.dohro7.officemobiledtr.repository;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dohro7.officemobiledtr.model.CtoModel;
import com.dohro7.officemobiledtr.model.ResponseBody;
import com.dohro7.officemobiledtr.model.UploadResponse;
import com.dohro7.officemobiledtr.repository.remote.RetrofitApi;
import com.dohro7.officemobiledtr.repository.remote.RetrofitClient;
import com.dohro7.officemobiledtr.repository.local.AppDatabase;
import com.dohro7.officemobiledtr.repository.local.CtoDao;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CtoRepository {

    private CtoDao ctoDao;
    private LiveData<List<CtoModel>> listLiveData;
    private MutableLiveData<String> uploadMessage = new MutableLiveData<>();
    private Context context;

    public CtoRepository(Context context) {
        this.ctoDao = AppDatabase.getInstance(context).ctoDao();
        this.listLiveData = ctoDao.getCto();
        this.context=context;
    }

    public MutableLiveData<String> getUploadMessage() {
        return uploadMessage;
    }

    public LiveData<List<CtoModel>> getListLiveData() {
        return listLiveData;
    }

    public void uploadCto(JSONObject jsonObject) {
        RetrofitApi retrofitApi = RetrofitClient.getRetrofitApi(context);
        Call<UploadResponse> uploadCall = retrofitApi.uploadCto(jsonObject);
        uploadCall.enqueue(new Callback<UploadResponse>() {
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
                } else {
                    uploadMessage.setValue(t.getMessage());
                }
            }
        });

    }

    public void insertCto(CtoModel ctoModel) {
        new InsertAsyncTask().execute(ctoModel);
    }

    public void deleteCto(CtoModel ctoModel) {
        new DeleteAsyncTask().execute(ctoModel);
    }

    class InsertAsyncTask extends AsyncTask<CtoModel, Void, Void> {

        @Override
        protected Void doInBackground(CtoModel... ctoModels) {
            ctoDao.insertCto(ctoModels[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            uploadMessage.setValue("New item added");
        }
    }

    class DeleteAllAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            ctoDao.deleteAllCto();
            return null;
        }
    }

    class DeleteAsyncTask extends AsyncTask<CtoModel, Void, Void> {

        @Override
        protected Void doInBackground(CtoModel... ctoModels) {
            ctoDao.deleteCto(ctoModels[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            uploadMessage.setValue("Item deleted");
        }
    }


}
