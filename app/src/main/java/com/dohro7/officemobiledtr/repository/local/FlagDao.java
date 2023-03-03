package com.dohro7.officemobiledtr.repository.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.dohro7.officemobiledtr.model.FlagModel;

import java.util.List;

@Dao
public interface FlagDao {

    @Insert
    void insertFlag(FlagModel flagModel);

    @Delete
    void deleteFLag(FlagModel flagModel);

    @Query("DELETE FROM flag_tbl")
    void deleteAllFlag();

    @Query("SELECT * FROM flag_tbl")
    LiveData<List<FlagModel>> getFlag();
}
