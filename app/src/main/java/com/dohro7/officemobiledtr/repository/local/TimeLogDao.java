package com.dohro7.officemobiledtr.repository.local;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.dohro7.officemobiledtr.model.TimeLogModel;

import java.util.List;

@Dao
public interface TimeLogDao {

    @Insert
    void insertLogs(TimeLogModel timeLogModel);

    @Query("SELECT * from time_log ORDER BY id ASC")
    LiveData<List<TimeLogModel>> getAllLogs();

    @Query("DELETE FROM time_log WHERE date = :date")
    void deleteLogByDate(String date);

    @Query("UPDATE time_log SET uploaded = 1")
    void uploadLogs();

    @Query("SELECT * from time_log  WHERE date = :date")
    List<TimeLogModel> getLogByDate(String date);

    @Query("DELETE FROM time_log WHERE uploaded=1 and date != :dateNotIncluded and date NOT  BETWEEN :dateFrom and :dateTo")
    void deleteLogsByRangeOfDate(String dateFrom, String dateTo, String dateNotIncluded);

    @Query("SELECT *  FROM time_log WHERE uploaded=1 and date NOT  BETWEEN :dateFrom and :dateTo ORDER BY date DESC")
    List<TimeLogModel>  getLogsByRangeOfDate(String dateFrom, String dateTo);

    @Query("SELECT COUNT(*) FROM time_log WHERE uploaded=1 and date NOT  BETWEEN :dateFrom and :dateTo")
    int getCountOfPriorLogs(String dateFrom, String dateTo);

    @Query("SELECT COUNT(*) FROM time_log WHERE uploaded=1 and date = :date")
    int getCountOfUploadedLogsByDate(String date);

    @Query("SELECT COUNT(*) FROM time_log WHERE uploaded=0 and date = :date")
    int getCountOfNotUploadedLogsByDate(String date);


    @Query("SELECT COUNT(*) FROM time_log WHERE uploaded=0")
    int getCountOfNotUploadedLogs();

    @Query("SELECT * FROM time_log WHERE uploaded=0")
    List<TimeLogModel> getNotUploadedLogs();
}
