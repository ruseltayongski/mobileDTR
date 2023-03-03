package com.dohro7.officemobiledtr.repository.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.dohro7.officemobiledtr.model.LeaveModel;

import java.util.List;

@Dao
public interface LeaveDao {

    @Insert
    void insertLeave(LeaveModel leaveModel);

    @Delete
    void deleteLeave(LeaveModel leaveModel);

    @Query("DELETE FROM leave_tbl")
    void deleteAllLeave();

    @Query("SELECT * FROM leave_tbl")
    LiveData<List<LeaveModel>> getLeaves();
}