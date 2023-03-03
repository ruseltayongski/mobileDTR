package com.dohro7.officemobiledtr.repository;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dohro7.officemobiledtr.model.FlagModel;
import com.dohro7.officemobiledtr.model.UploadResponse;
import com.dohro7.officemobiledtr.model.UserModel;
import com.dohro7.officemobiledtr.repository.local.AppDatabase;
import com.dohro7.officemobiledtr.repository.local.FlagDao;
import com.dohro7.officemobiledtr.repository.remote.RetrofitApi;
import com.dohro7.officemobiledtr.repository.remote.RetrofitClient;
import com.dohro7.officemobiledtr.repository.sharedpreference.MockLocationSharedPreference;
import com.dohro7.officemobiledtr.repository.sharedpreference.UserSharedPreference;
import com.dohro7.officemobiledtr.utility.BitmapDecoder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FlagRepository {
    private LiveData<List<FlagModel>> listLiveData;

    private FlagDao flagDao;
    private MutableLiveData<String> mutableMessage = new MutableLiveData<>();
    private MutableLiveData<String> deleteMessage = new MutableLiveData<>();
    private Context context;
    private File flagFile;

    private MockLocationSharedPreference mockLocationSharedPreference;
    private MutableLiveData<String> mutable_mockLocation;
    private MutableLiveData<UserModel> currentUser;

    public FlagRepository(Context context){
        flagDao = AppDatabase.getInstance(context).flagDao();
        listLiveData = flagDao.getFlag();

        this.context =context;

        flagFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Flag");

        mockLocationSharedPreference =MockLocationSharedPreference.getInstance(context);
        mutable_mockLocation=mockLocationSharedPreference.getMockLocationSharedPref();

        currentUser = UserSharedPreference.getInstance(context).getUserModel();
    }

    public MutableLiveData<String> getMutableMessage()
    {    return mutableMessage;   }

    public MutableLiveData<String> getDeleteMessage()
    {    return deleteMessage;   }

    public LiveData<UserModel> getCurrentUser() { return currentUser; }

    public LiveData<List<FlagModel>> getFlag()
    {   return listLiveData;    }

    public MutableLiveData<String> getMockLocationSharedPref(){
        return mutable_mockLocation;
    }

    public void deleteFlag(FlagModel model)
    {    new  DeleteAsyncTask().execute(model);   }

    public void deleteAllFlag()
    {    new  DeleteAllAsyncTask().execute();   }

    public void insertFlag(FlagModel model)
    {   new InsertAsyncTask().execute(model);   }

    public void insertMockLocationCreatedAt(String value){
        mockLocationSharedPreference.insertMockLocationCreatedAt(value);
    }

    public void upload()
    {   new UploadingAsnycTask().execute(); }

    public void uploadLogs(JSONObject jsonObject) {
        Log.e("json", ""+jsonObject.toString());
        RetrofitApi retrofitApi = RetrofitClient.getRetrofitApi(context); //TODO: change API
        Call<UploadResponse> callUploadLogs = retrofitApi.uploadFlagLogs(jsonObject);
        callUploadLogs.enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                UploadResponse uploadResponse = response.body();
                if(uploadResponse!=null){
                    if (uploadResponse.code == 200){
                        mutableMessage.setValue(response.body().response);
                        new DeleteAllAsyncTask().execute();
                        return;
                    }
                    Log.e("upload", "" + uploadResponse.response );
                    mutableMessage.setValue("Something went wrong, please contact system administrator: " + uploadResponse.response);
                }
                mutableMessage.setValue("Something went wrong, please contact system administrator");

            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                if (t instanceof IOException) {
                    mutableMessage.setValue("No network connection");
                } else {
                    mutableMessage.setValue(t.getMessage());
                }
            }
        });
    }

    class UploadingAsnycTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {

            if (listLiveData.getValue().size() > 0 ){

                JSONObject data = new JSONObject();
                JSONArray logs = new JSONArray();
                try{
                    for(FlagModel model : listLiveData.getValue()){
                        JSONObject timeLogs = new JSONObject();
                        timeLogs.put("userid", getCurrentUser().getValue().id);
                        timeLogs.put("time", model.time);
                        timeLogs.put("event","");
                        timeLogs.put("date", model.date);
                        timeLogs.put("remark", model.remarks);
                        timeLogs.put("edited", model.edited);
                        timeLogs.put("latitude", model.latitude + "");
                        timeLogs.put("longitude", model.longitude + "");
                        timeLogs.put("filename", model.fileName + "");
                        timeLogs.put("mocked_created_at", model.mocked+"");
                        String imagePath ="";
                        try {
                            imagePath = BitmapDecoder.convertBitmapToString(model.filePath);
                        }
                        catch (Exception ex){
                            imagePath = "corrupted_image";
                        }
                        timeLogs.put("image", imagePath);
                        logs.put(timeLogs);
                    }

                    if(logs.length()>0) {
                        data.put("logs", logs);
                        uploadLogs(data);
                        return true;
                    }
                    else {
                        return false;
                    }
                }
                catch (Exception ex){
                    ex.printStackTrace();
                    return false;
                }
            }
            else {
                return false; //no logs
            }
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            super.onPostExecute(bool);
            if (!bool){
                mutableMessage.setValue("Nothing to upload");
            }

        }
    }


    class InsertAsyncTask extends AsyncTask<FlagModel, Void, Void> {
        @Override
        protected Void doInBackground(FlagModel... models) {
            flagDao.insertFlag(models[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mockLocationSharedPreference.insertMockLocationCreatedAt(null);
            super.onPostExecute(aVoid);
        }
    }

    class DeleteAsyncTask extends AsyncTask<FlagModel, Void, Void> {
        @Override
        protected Void doInBackground(FlagModel... models)
        {
            flagDao.deleteFLag(models[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            mutableMessage.setValue("Item deleted");
        }
    }

    class DeleteAllAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            for (FlagModel model : listLiveData.getValue()){
                String fileName = model.fileName;

                File imageFile = new File(flagFile, fileName);
                try {
                    imageFile.delete();
                } catch (Exception e) {
                    Log.e("delete", "Exception = " + e.getMessage());
                }
                if(imageFile.exists()){
                    try {
                        imageFile.getCanonicalFile().delete();
                    } catch (IOException e) {
                        Log.e("delete", "IOExeption = " + e.getMessage());
                    }
                    if(imageFile.exists()){
                        context.deleteFile(model.fileName);
                    }
                }
            }
            flagDao.deleteAllFlag();
            return null;
        }
    }

}
