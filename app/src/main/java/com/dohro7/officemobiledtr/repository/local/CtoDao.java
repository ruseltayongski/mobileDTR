package com.dohro7.officemobiledtr.repository.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.dohro7.officemobiledtr.model.CtoModel;

import java.util.List;

@Dao
public interface CtoDao {

    @Insert
    void insertCto(CtoModel ctoModel);

    @Query("SELECT * FROM cto_tbl")
    LiveData<List<CtoModel>> getCto();

    @Query("DELETE FROM cto_tbl")
    void deleteAllCto();

    @Delete
    void deleteCto(CtoModel ctoModel);
}
