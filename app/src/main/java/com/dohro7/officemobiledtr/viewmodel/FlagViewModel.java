package com.dohro7.officemobiledtr.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dohro7.officemobiledtr.model.FlagModel;
import com.dohro7.officemobiledtr.model.UserModel;
import com.dohro7.officemobiledtr.repository.FlagRepository;
import com.dohro7.officemobiledtr.utility.DateTimeUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FlagViewModel extends AndroidViewModel {
    private FlagRepository flagRepository;
    private MutableLiveData<String> mutableMessage;

    private LiveData<List<FlagModel>> liveDataList;
    private LiveData<UserModel> userModel;
    private MutableLiveData<String> mutable_mockLocation;

    public FlagViewModel(Application application) {
        super(application);
        flagRepository = new FlagRepository(application);
        liveDataList = flagRepository.getFlag();

        mutableMessage = flagRepository.getMutableMessage();

        userModel=flagRepository.getCurrentUser();

    }

    public MutableLiveData<String> getMutableMessage()
    {   return mutableMessage;    }


    public LiveData<UserModel> getCurrentUser()
    {   return userModel;   }

    public LiveData<List<FlagModel>> getMutableLiveDataList()
    { return liveDataList; }


    public void insertMockLocationCreatedAt(String dateTime){
        flagRepository.insertMockLocationCreatedAt(dateTime);
    }

    public MutableLiveData<String> getMockLocationSharedPref(){
        return flagRepository.getMockLocationSharedPref();
    }

    public void insertFlagLog(FlagModel model) {
        flagRepository.insertFlag(model);
    }

    public String timeValidate(){
        FlagModel flagModel = new FlagModel();
        flagModel.time = DateTimeUtility.getCurrentTime();
        flagModel.date = DateTimeUtility.getCurrentDate();
        if(DateTimeUtility.getDayOfTheWeek()==6){ //friday
            if(flagModel.getHour()==16 && flagModel.getMinutes()<=5 ){ //4:00-4:05pm
                flagModel.remarks="FLAG_RETREAT";
                flagModel.edited = "9";
            }else if(flagModel.getHour()<16){//before 4
                return "You are too early for the flag retreat attendance";
            }else {
                return "You are late for the flag retreat attendance";
            }
        }else if(DateTimeUtility.getDayOfTheWeek()!=7 && DateTimeUtility.getDayOfTheWeek()!=1){
            if((flagModel.getHour()==7 && flagModel.getMinutes()>=55) || (flagModel.getHour()==8 && flagModel.getMinutes() <=10)){
                flagModel.remarks="FLAG_MORNING";
                flagModel.edited = "8";
            }else if((flagModel.getHour()==7 && flagModel.getMinutes()<55) || flagModel.getHour()<7){
                return "You are too early for the flag attendance";
            }else {
                return "You are late for the flag attendance";
            }
        }else {
            return "Currently not allowed to have an attendance: Today is weekend!";
        }

        for (FlagModel model : liveDataList.getValue()) {
            if (model.date.equalsIgnoreCase(flagModel.date) && model.remarks.equalsIgnoreCase(flagModel.remarks)) {
                if(flagModel.remarks.equalsIgnoreCase("FLAG_MORNING")){
                    return "You already have Flag Ceremony Attendance" ;
                }else {
                    return "You already have Flag retreat Attendance" ;
                }

            }
        }
        return "";
    }


    public void upload(){
        flagRepository.upload();
    }
}
