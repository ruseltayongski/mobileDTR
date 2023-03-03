package com.dohro7.officemobiledtr.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dohro7.officemobiledtr.model.LocationModel;
import com.dohro7.officemobiledtr.model.TimeLogModel;
import com.dohro7.officemobiledtr.model.UserModel;
import com.dohro7.officemobiledtr.repository.DtrRepository;
import com.dohro7.officemobiledtr.repository.sharedpreference.DtrEventSharedPreference;
import com.dohro7.officemobiledtr.repository.sharedpreference.UserSharedPreference;
import com.dohro7.officemobiledtr.utility.BitmapDecoder;
import com.dohro7.officemobiledtr.utility.DateTimeUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DtrViewModel extends AndroidViewModel {
    private DtrRepository dtrRepository;
    private MutableLiveData<String> uploadMessage;
    private MutableLiveData<String> deleteMessage;
    private MutableLiveData<String> mutableLiveMenuTitle;
    private MutableLiveData<String> mutableTimeExists;

    private LiveData<List<TimeLogModel>> liveDataList;
    private MutableLiveData<Boolean> mutableUndertime;
    private UserSharedPreference userSharedPreference;
    private LiveData<UserModel> userModel;
    private DtrEventSharedPreference dtrEventSharedPreference;
    private MutableLiveData<String> mutable_mockLocation;

    private LiveData<List<LocationModel>> listLiveData_locationAssignments;


    public DtrViewModel(@NonNull Application application) {
        super(application);
        dtrRepository = new DtrRepository(application);
        liveDataList = dtrRepository.getTimeLogs();
        mutableLiveMenuTitle = new MutableLiveData<>();
        mutableTimeExists = new MutableLiveData<>();
        mutableUndertime = new MutableLiveData<>();
        uploadMessage = dtrRepository.getUploadMessage();
        deleteMessage = dtrRepository.getDeleteMessage();
        userModel=dtrRepository.getCurrentUser();
        listLiveData_locationAssignments = dtrRepository.getListLiveData_locationAssignments();
        dtrEventSharedPreference = DtrEventSharedPreference.getInstance(application);

        mutable_mockLocation=dtrRepository.getMockLocationSharedPref();

        mutableLiveMenuTitle.setValue("IN");
        String menuTitle = dtrEventSharedPreference.getMenuTitle();
        if (menuTitle == null) {
            return;
        }
        if (!dtrEventSharedPreference.getDtrLastDate().equalsIgnoreCase(DateTimeUtility.getCurrentDate())) {
            return;
        }
        if (menuTitle != null) mutableLiveMenuTitle.setValue(menuTitle);

    }

    public MutableLiveData<String> getUploadMessage()
    {   return uploadMessage;    }

    public MutableLiveData<String> getDeleteMessage()
    {   return deleteMessage;    }

    public LiveData<UserModel> getCurrentUser()
    {   return userModel;   }

    public MutableLiveData<String> getMutableTimeExists()
    { return mutableTimeExists; }

    public MutableLiveData<Boolean> getMutableUndertime()
    { return mutableUndertime; }

    public MutableLiveData<String> getLiveDataMenuTitle()
    { return mutableLiveMenuTitle; }

    public LiveData<List<TimeLogModel>> getMutableLiveDataList()
    { return liveDataList; }

    public String getSection()
    { return userModel.getValue().getSection(); }

    public void timeValidate() {
        TimeLogModel timeLogModel = new TimeLogModel();
        timeLogModel.time = DateTimeUtility.getCurrentTime();
        timeLogModel.date = DateTimeUtility.getCurrentDate();
        timeLogModel.status = mutableLiveMenuTitle.getValue();
        List<TimeLogModel> dbList = getLogsByDateAndStatus(timeLogModel.date, timeLogModel.status);
        List<TimeLogModel> dbListOut = getLogsByDateAndStatus(timeLogModel.date, "OUT");

        for (TimeLogModel data : dbList) {

            if (timeLogModel.status.equalsIgnoreCase("IN") && timeLogModel.getHour() < 12 && data.getHour() < 12) { // AM IN EXISTS
                mutableTimeExists.setValue("You have already timed IN");
                return;
            }
            if (timeLogModel.status.equalsIgnoreCase("OUT") && dbList.size() == 1 && isUndertime(timeLogModel)) { //Prompts undertime, dbList.size() ==1 - naa nay am-out
                mutableUndertime.setValue(true);
                return;
            }
            if (timeLogModel.status.equalsIgnoreCase("OUT") && timeLogModel.getHour() < 17 && data.getHour() < 17) { //AM OUT EXISTS
                mutableTimeExists.setValue("You have already timed OUT");
                return;
            }
            if (timeLogModel.status.equalsIgnoreCase("IN") && timeLogModel.getHour() > 11 &&  ( data.getHour() > 11 || (dbListOut.size()==1 && Integer.parseInt(dbListOut.get(0).time.split(":")[0])> 15) )) { //PM IN EXISTS
                mutableTimeExists.setValue("You have already timed IN");
                return;
            }
            if (timeLogModel.status.equalsIgnoreCase("OUT") && timeLogModel.getHour() > 16 && data.getHour() > 16) { //PM OUT EXISTS
                mutableTimeExists.setValue("You have already timed OUT");
                return;
            }
        }

        if (isUndertime(timeLogModel)) {
            mutableUndertime.setValue(true);
            return;
        }
        mutableUndertime.setValue(false);
    }

    public void insertTimeLog(TimeLogModel timeLogModel)
    {
        dtrRepository.insertTimeLog(timeLogModel);

        if (timeLogModel.status.equalsIgnoreCase("IN")) {
            mutableLiveMenuTitle.setValue("OUT");
        }
        else //out
        {
            mutableLiveMenuTitle.setValue("IN");
        }

        dtrEventSharedPreference.insertUpdateMenuStatus(mutableLiveMenuTitle.getValue(), DateTimeUtility.getCurrentDate());
    }

    public List<TimeLogModel> getLogsByDateAndStatus(String date, String status) {
        List<TimeLogModel> list = new ArrayList<>();
        for (TimeLogModel timeLogModel : liveDataList.getValue()) {
            if (timeLogModel.date.equalsIgnoreCase(date) && timeLogModel.status.equalsIgnoreCase(status)) {
                list.add(timeLogModel);
            }
        }
        return list;
    }

    public boolean isUndertime(TimeLogModel timeLogModel) {
// Testing, check current time
//        int currentTime = Integer.parseInt(timeLogModel.time.split(":")[0]);
//        Log.e("undertime", "currentTime= " + currentTime);
//
//        return true;
        boolean status=false;
        int currentTime = Integer.parseInt(timeLogModel.time.split(":")[0]);//Integer.parseInt(DateTimeUtility.getCurrentTime().split(":")[0]);
        int prevTimeIn;
        List<TimeLogModel> list = getLogsByDateAndStatus(timeLogModel.date, "IN");
      //  Log.e("undertime", "list size= "+list.size());

        if(list.size() == 1 )
        {
            prevTimeIn=(Integer.parseInt(list.get(0).time.split(":")[0]));
            //Log.e("undertime","prev time(0) IN= "+list.get(0).time+ " hr: " + prevTimeIn);

            if(prevTimeIn<12) //prev log = am in
            {
                //Log.e("undertime", "AM IN, CurrentTime=  "+currentTime);
                if(timeLogModel.status.equalsIgnoreCase("OUT") && (currentTime < 12))
                {
                    Log.e("undertime", "AM OUT UNDERTIME");
                    status=true;
                }
            }
            else //prev log = pm in
            {
                //Log.e("undertime", "PM IN, CurrentTime=  "+currentTime );
                if(timeLogModel.status.equalsIgnoreCase("OUT") && (currentTime < 17))
                {//  Log.e("undertime", "PM OUT UNDERTIME");
                    status=true;
                }
            }
        }
        else if(list.size()==2)//if list sa In is 2, wholeday
        {
           // Log.e("undertime", "wholeday, current time=" + currentTime);
            if(timeLogModel.status.equalsIgnoreCase("OUT") && (currentTime < 17))
            {
         //       Log.e("undertime", "wholeday PM OUT UNDERTIME");
                status=true;
            }
        }
        return status;
    }

    public void uploadLogs() {
        dtrRepository.uploading();
/*      try { //OLD version of uploading
          if(liveDataList.getValue()!=null){
              if (liveDataList.getValue().size() > 0) {
                  JSONObject data = new JSONObject();
                  JSONArray logs = new JSONArray();
                  for (int i = 0; i < liveDataList.getValue().size(); i++) {
                      if(liveDataList.getValue().get(i).uploaded!=1)
                      {
                          JSONObject timeLogs = new JSONObject();
                          timeLogs.put("userid", getCurrentUser().getValue().id);
                          timeLogs.put("time", liveDataList.getValue().get(i).time);
                          timeLogs.put("event", liveDataList.getValue().get(i).status);
                          timeLogs.put("date", liveDataList.getValue().get(i).date);
                          timeLogs.put("remark", "OFFICE");
                          timeLogs.put("edited", "0");
                          timeLogs.put("latitude", liveDataList.getValue().get(i).latitude + "");
                          timeLogs.put("longitude", liveDataList.getValue().get(i).longitude + "");
                          timeLogs.put("filename", liveDataList.getValue().get(i).fileName + "");
                          timeLogs.put("mocked_created_at", liveDataList.getValue().get(i).mocked);
                          String imagePath ="";
                          try {
                              imagePath = BitmapDecoder.convertBitmapToString(liveDataList.getValue().get(i).filePath);
                          }
                          catch (Exception ex){
                              imagePath = "corrupted_image";
                          }
                          timeLogs.put("image", imagePath);
                          logs.put(timeLogs);
                      }
                  }
                  if(logs.length()>0) {
                      data.put("logs", logs);
                      dtrRepository.uploadLogs(data);
                  }
                  else {
                      uploadMessage.setValue("Nothing to upload");
                  }
              }
              else {
                  uploadMessage.setValue("Nothing to upload");
              }
          }else {
              uploadMessage.setValue("Nothing to upload");
          }

        }
      catch (JSONException e) {
                e.printStackTrace();
      }*/
    }

    public void deleteLogByDate(String date) {
        dtrRepository.deleteLogByDate(date);
    }

    public void deleteByRangeDate(String currentdate) {
        String[] split = currentdate.split("-"); //YYYY-MM-DD
        String dateFrom = split[0] + "-" + split[1] + "-01";
        dtrRepository.deleteByRangeDate(dateFrom, currentdate);
    }

    public void insertMockLocationCreatedAt(String dateTime){
        dtrRepository.insertMockLocationCreatedAt(dateTime);
    }

    public MutableLiveData<String> getMockLocationSharedPref(){
        return dtrRepository.getMockLocationSharedPref();
    }

//Area of assignments
    public LiveData<List<LocationModel>> getListLiveData_locationAssignments()
    {   return listLiveData_locationAssignments;    }

    public void getLocationAssignmentsFromServer(String userid){
        dtrRepository.getLocationAssignmentsFromServer(userid);
    }
}


