package com.dohro7.officemobiledtr.repository.remote;


import androidx.lifecycle.LiveData;

import com.dohro7.officemobiledtr.model.AnnouncementModel;
import com.dohro7.officemobiledtr.model.ForceUpdateResponse;
import com.dohro7.officemobiledtr.model.LocationIdentifier;
import com.dohro7.officemobiledtr.model.LocationModel;
import com.dohro7.officemobiledtr.model.ResponseBody;
import com.dohro7.officemobiledtr.model.SoftwareResponseBody;
import com.dohro7.officemobiledtr.model.UploadResponse;

import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface RetrofitApi {

    @FormUrlEncoded
    @POST("/dtr/mobileV2/add-logs")
    Call<UploadResponse> uploadTimelogs(@Field("data") JSONObject jsonObject);

    @FormUrlEncoded
    @POST("/dtr/mobileV2/add-flags")
    Call<UploadResponse> uploadFlagLogs(@Field("data") JSONObject jsonObject);

    @FormUrlEncoded
    @POST("/dtr/mobileV2/add-leave")
    Call<UploadResponse> uploadLeaves(@Field("data") JSONObject jsonObject);

    @FormUrlEncoded
    @POST("/dtr/mobileV2/add-so")
    Call<UploadResponse> uploadSo(@Field("data") JSONObject jsonObject);

    @FormUrlEncoded
    @POST("/dtr/mobileV2/add-cdo")
    Call<UploadResponse> uploadCto(@Field("data") JSONObject jsonObject);

    @POST("/dtr/mobileV2/login1")
    Call<ResponseBody> login(@Query("imei") String imei);

    @POST("/dtr/mobileV2/imei") //Update IMEI
    Call<UploadResponse> imei(@Query("imei") String imei, @Query("userid") String userid);

    @POST("/dtr/mobile/reset_password") //used to reset password, userid=current logged in userid, reset_userid=UserId to Reset, returns fullname if success
    Call<String> resetPassword(@Query("userid") String userid, @Query("reset_userid") String reset_userid);

    @POST("/dtr/mobile/check_username") //check if userid to reset is existing, returns fullname
    Call<String> checkUsername(@Query("reset_userid") String reset_userid);

    @GET("/dtr/mobile/office/announcement")
    Call<AnnouncementModel> checkAnnouncement();

    @GET("/dtr/mobile/get/version") //Used to check for Software updates details, and if force or not
    Call<ForceUpdateResponse> checkForceUpdate();

    @POST("/dtr/mobileV3/area_of_assignment")
    Call<List<LocationModel>> getLocationAssignments(@Query("userid") String userid);
}
