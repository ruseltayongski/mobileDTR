package com.dohro7.officemobiledtr.repository.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.dohro7.officemobiledtr.model.CtoModel;
import com.dohro7.officemobiledtr.model.FlagModel;
import com.dohro7.officemobiledtr.model.LeaveModel;
import com.dohro7.officemobiledtr.model.LocationModel;
import com.dohro7.officemobiledtr.model.OfficeOrderModel;
import com.dohro7.officemobiledtr.model.TimeLogModel;

@Database(entities = {
        TimeLogModel.class, OfficeOrderModel.class, LeaveModel.class, CtoModel.class, LocationModel.class, FlagModel.class}, version = 3, exportSchema = false)


public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract TimeLogDao timeLogDao();

    public abstract OfficeOrderDao officeOrderDao();

    public abstract LeaveDao leaveDao();

    public abstract CtoDao ctoDao();

    public abstract LocationDao locationDao();

    public abstract FlagDao flagDao();


    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, AppDatabase.class, "mob_db").fallbackToDestructiveMigration().build();
        }
        return instance;
    }
}
