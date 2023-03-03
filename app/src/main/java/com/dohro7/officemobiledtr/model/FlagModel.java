package com.dohro7.officemobiledtr.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "flag_tbl")
public class FlagModel {
    @PrimaryKey (autoGenerate = true)
    public int id;

    @ColumnInfo(name="date")
    public String date;

    @ColumnInfo(name = "time")
    public String time;

    @ColumnInfo(name = "filePath")
    public String filePath;

    @ColumnInfo(name = "fileName")
    public String fileName;

    @ColumnInfo(name = "latitude")
    public String latitude;

    @ColumnInfo(name = "longitude")
    public String longitude;

    @ColumnInfo(name = "mocked_created_at")
    public String mocked;

    @ColumnInfo(name = "remarks")
    public String remarks;

    @ColumnInfo(name = "edited")
    public String edited;

    public FlagModel(){}

    public int getHour() {
        return Integer.parseInt(time.split(":")[0]);
    }
    public int getMinutes() {
        return Integer.parseInt(time.split(":")[1]);
    }
}
