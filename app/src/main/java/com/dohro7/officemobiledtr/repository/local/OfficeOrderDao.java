package com.dohro7.officemobiledtr.repository.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.dohro7.officemobiledtr.model.OfficeOrderModel;

import java.util.List;

@Dao
public interface OfficeOrderDao {

    @Insert
    void insertOfficeOrder(OfficeOrderModel officeOrderModel);

    @Delete
    void deleteOfficerOrder(OfficeOrderModel officeOrderModel);

    @Query("SELECT * FROM so_table")
    LiveData<List<OfficeOrderModel>> getOfficeOrder();

    @Query("DELETE FROM so_table")
    void deleteAllOfficerOrder();
}
