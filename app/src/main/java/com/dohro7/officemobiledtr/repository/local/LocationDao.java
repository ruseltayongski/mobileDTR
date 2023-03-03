package com.dohro7.officemobiledtr.repository.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.dohro7.officemobiledtr.model.LocationModel;

import java.util.List;

@Dao
public interface LocationDao {

    @Insert
    void insertLocation(LocationModel model);

    @Query("SELECT * FROM location_tbl " )
    LiveData<List<LocationModel>> getLocationAssignments();

    @Query("DELETE from location_tbl")
    void deleteAllLocations();

}
