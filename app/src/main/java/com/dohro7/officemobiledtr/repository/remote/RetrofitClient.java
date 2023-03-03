package com.dohro7.officemobiledtr.repository.remote;

import android.content.Context;
import android.util.Log;

import com.dohro7.officemobiledtr.R;
import com.dohro7.officemobiledtr.repository.sharedpreference.IpAddressSharedPreference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static RetrofitApi retrofitApi;
    private static String BASE_URL ="";//= "http://124.6.144.162";//"http://203.177.67.122";  ////FINAL URL -  w/ dtr_3_0 for software update testing
    // private static final String BASE_URL = "http://192.168.100.66:8000"; // URL for debugging

    private static final String default_url= "http://49.157.74.3"; //"http://192.168.110.76:8000" ;
    private static IpAddressSharedPreference ipAddressSharedPreference;

    public static RetrofitApi getRetrofitApi(Context context) {
        //assign Base URL
        ipAddressSharedPreference=IpAddressSharedPreference.getInstance(context);
   /*     if(ipAddressSharedPreference.getIpAddress_mutable().getValue()!=null){
            if(!ipAddressSharedPreference.getIpAddress_mutable().getValue().equalsIgnoreCase("")) {
                BASE_URL="http://"+ipAddressSharedPreference.getIpAddress_mutable()+"/";
            } else {
                BASE_URL=default_url;
            }
        }
        else {
            BASE_URL=default_url;
        }*/
        if(ipAddressSharedPreference.getIpAddress_mutable().getValue()!=null)
        { BASE_URL="http://"+ipAddressSharedPreference.getIpAddress_mutable().getValue().trim(); }
        else
        { BASE_URL=default_url; }

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            Log.e("ip retrofit",""+BASE_URL);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            retrofitApi = retrofit.create(RetrofitApi.class);

        return retrofitApi;
    }
}