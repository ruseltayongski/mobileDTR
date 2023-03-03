package com.dohro7.officemobiledtr.model;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cto_tbl")
public class CtoModel {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(name = "inclusive_date")
    public String inclusive_date;

    public CtoModel(int id, String inclusive_date) {
        this.id = id;
        this.inclusive_date = inclusive_date;
    }
}
