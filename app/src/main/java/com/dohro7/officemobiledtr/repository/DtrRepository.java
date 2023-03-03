package com.dohro7.officemobiledtr.repository;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dohro7.officemobiledtr.model.LocationModel;
import com.dohro7.officemobiledtr.model.TimeLogModel;
import com.dohro7.officemobiledtr.model.UploadResponse;
import com.dohro7.officemobiledtr.model.UserModel;
import com.dohro7.officemobiledtr.repository.local.AppDatabase;
import com.dohro7.officemobiledtr.repository.local.LocationDao;
import com.dohro7.officemobiledtr.repository.local.TimeLogDao;
import com.dohro7.officemobiledtr.repository.remote.RetrofitApi;
import com.dohro7.officemobiledtr.repository.remote.RetrofitClient;
import com.dohro7.officemobiledtr.repository.sharedpreference.MockLocationSharedPreference;
import com.dohro7.officemobiledtr.repository.sharedpreference.UserSharedPreference;
import com.dohro7.officemobiledtr.utility.BitmapDecoder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DtrRepository  {
    private LiveData<List<TimeLogModel>> listLiveData;
   // private LiveData<List<TimeLogModel>> listLiveData_notUploaded;
    private TimeLogDao timeLogDao;
    private MutableLiveData<String> uploadMessage = new MutableLiveData<>();
    private MutableLiveData<String> deleteMessage = new MutableLiveData<>();
    private List<TimeLogModel> logByDate = new ArrayList<>();
    private Context context;
    private File timeLogFile;
    private MockLocationSharedPreference mockLocationSharedPreference;
    private MutableLiveData<String> mutable_mockLocation;
    private MutableLiveData<UserModel> currentUser;

    private LocationDao locationDao;
    private LiveData<List<LocationModel>> listLiveData_locationAssignments = new MutableLiveData<>();

    public DtrRepository(Context context) {
        timeLogDao = AppDatabase.getInstance(context).timeLogDao();
        listLiveData = timeLogDao.getAllLogs();
        locationDao=AppDatabase.getInstance(context).locationDao();
        listLiveData_locationAssignments = locationDao.getLocationAssignments();

        this.context =context;

        timeLogFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Timelogs");
        mockLocationSharedPreference =MockLocationSharedPreference.getInstance(context);
        mutable_mockLocation=mockLocationSharedPreference.getMockLocationSharedPref();

        currentUser = UserSharedPreference.getInstance(context).getUserModel();
    }


    public MutableLiveData<String> getUploadMessage()
    {    return uploadMessage;   }

    public MutableLiveData<String> getDeleteMessage()
    {    return deleteMessage;   }

    public LiveData<UserModel> getCurrentUser() { return currentUser; }

    public LiveData<List<TimeLogModel>> getTimeLogs()
    {   return listLiveData;    }



    public LiveData<List<LocationModel>> getListLiveData_locationAssignments()
    {   return listLiveData_locationAssignments;    }

    public void deleteLogByDate(String date)
    {    new DeleteAsnycTask().execute(date);   }

    public void uploadLogs(JSONObject jsonObject) {
        Log.e("json", ""+jsonObject.toString());
        RetrofitApi retrofitApi = RetrofitClient.getRetrofitApi(context);
        Call<UploadResponse> callUploadLogs = retrofitApi.uploadTimelogs(jsonObject);
        callUploadLogs.enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                UploadResponse uploadResponse = response.body();
                if(uploadResponse!=null){
                    if (uploadResponse.code == 200){
                        uploadMessage.setValue(response.body().response);
                        new UploadAsyncTask().execute();
                        return;
                    }
                    Log.e("upload", "" + uploadResponse.response );
                    uploadMessage.setValue("Something went wrong, please contact system administrator: " + uploadResponse.response);
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


    public void getLocationAssignmentsFromServer(String userid) {
        Log.e("locationRepo", "before call");
        RetrofitApi retrofitApi = RetrofitClient.getRetrofitApi(context);
        Call<List<LocationModel>> callLocationAssignments= retrofitApi.getLocationAssignments(userid);
        callLocationAssignments.enqueue(new Callback<List<LocationModel>>() {
            @Override
            public void onResponse(Call<List<LocationModel>> call, Response<List<LocationModel>> response) {
                Log.e("locationRepo", "response.Code= " + response.code());

                if (response.code()==200)    {
                    new DeleteAllLocationAsnycTask().execute();
                    listLiveData_locationAssignments= new MutableLiveData<>();
                    if(response.body()!=null){
                        if(response.body().size()!=0){
                            for(LocationModel model: response.body()){
                                Log.e("locationRepo", "Name: " + model.name + " lat:" +model.latitude + " long:"+model.longitude + " rad:"+model.radius);
                                new InsertLocationAssignmentsAsyncTask().execute(model);
                            }
                            deleteMessage.setValue("Area of Assignment is now Updated");
                        }else{
                            deleteMessage.setValue("You have no assigned area on the server! please contact admin to update");
                        }

                    }
                    return;
                }
            }

            @Override
            public void onFailure(Call<List<LocationModel>> call, Throwable t) {
                if (t instanceof IOException) {
                    uploadMessage.setValue("No network connection");
                } else {
                    uploadMessage.setValue(t.getMessage());
                }
            }
        });
    }
    public void insertTimeLog(TimeLogModel timeLogModel)
    {   new InsertAsyncTask().execute(timeLogModel);     }



    public void insertMockLocationCreatedAt(String value){
        mockLocationSharedPreference.insertMockLocationCreatedAt(value);
    }

    public MutableLiveData<String> getMockLocationSharedPref(){
        return mutable_mockLocation;
    }

    public void deleteByRangeDate(String dateFrom, String dateTo)
    {   String[] date = {dateFrom, dateTo};
        new DeleteByRangeDateAsnycTask().execute(date);
    }


    class UploadAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            timeLogDao.uploadLogs();
            return null;
        }
    }

    class InsertLocationAssignmentsAsyncTask extends AsyncTask<LocationModel, Void, Void> {

        @Override
        protected Void doInBackground(LocationModel... model) {
            locationDao.insertLocation(model[0]);
            return null;
        }
    }

    class DeleteAsnycTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... date) {
            logByDate = timeLogDao.getLogByDate(date[0]);

            for(TimeLogModel model : logByDate) {
                String fileName = model.fileName;

                File imageFile = new File(timeLogFile, fileName);
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
            timeLogDao.deleteLogByDate(date[0]);
            return null;
        }
    }

    class DeleteByRangeDateAsnycTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... date) {
            if (timeLogDao.getCountOfPriorLogs(date[0], date[1]) > 0 ){

                logByDate = timeLogDao.getLogsByRangeOfDate(date[0], date[1]);

                int count=0; String date3="0000-00-00";

                if(timeLogDao.getCountOfNotUploadedLogsByDate(logByDate.get(0).date)>0){

                    count=timeLogDao.getCountOfUploadedLogsByDate(logByDate.get(0).date); //count of logs included in logByDate, to remove

                   if(count>0){
                       date3=logByDate.get(0).date;
                       logByDate.subList(0, count).clear();
                   }
                }

               for(TimeLogModel model : logByDate) {
                    String fileName = model.fileName;

                    File imageFile = new File(timeLogFile, fileName);
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

                timeLogDao.deleteLogsByRangeOfDate(date[0], date[1], date3);
                return true;
            }
            else {
                return false; //no logs
            }
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            super.onPostExecute(bool);
            if (bool){
                deleteMessage.setValue("Successfully deleted ALL of your uploaded logs from prior month!");
            }else {
                deleteMessage.setValue("Nothing to delete! Possible reasons: \n1.No logs from prior month\n2.logs from prior month are not yet uploaded, please upload");
            }

        }
    }

    class InsertAsyncTask extends AsyncTask<TimeLogModel, Void, Void> {

        @Override
        protected Void doInBackground(TimeLogModel... timeLogModels) {
            timeLogDao.insertLogs(timeLogModels[0]);
            //Log.e("mock", "mocked=" + timeLogModels[0].mocked + " date time " + timeLogModels[0].date + " " +timeLogModels[0].time);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mockLocationSharedPreference.insertMockLocationCreatedAt(null);
            super.onPostExecute(aVoid);
        }

    }

    class DeleteAllLocationAsnycTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            locationDao.deleteAllLocations();
            return null;
        }
    }

    public void uploading()
    {   new UploadingAsnycTask().execute(); }

    class UploadingAsnycTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {

             if (timeLogDao.getCountOfNotUploadedLogs() > 0 ){
                List<TimeLogModel> notUploadedLogs = new ArrayList<>();
                notUploadedLogs = timeLogDao.getNotUploadedLogs();

                JSONObject data = new JSONObject();
                JSONArray logs = new JSONArray();
                try{
                    for(TimeLogModel model : notUploadedLogs){
                        JSONObject timeLogs = new JSONObject();
                        timeLogs.put("userid", getCurrentUser().getValue().id);
                        timeLogs.put("time", model.time);
                        timeLogs.put("event", model.status);
                        timeLogs.put("date", model.date);

                        if(getCurrentUser().getValue().dmo_roles==1){
                            timeLogs.put("remark", "MOBILE");
                        }else {
                            timeLogs.put("remark", "OFFICE");
                        }

                        timeLogs.put("edited", "0");
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
                uploadMessage.setValue("Nothing to upload");
            }

        }
    }


}
