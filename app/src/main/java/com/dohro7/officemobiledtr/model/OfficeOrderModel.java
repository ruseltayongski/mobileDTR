package com.dohro7.officemobiledtr.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "so_table")
public class OfficeOrderModel {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(name = "so_no")
    public String so_no;
    @ColumnInfo(name = "inclusive_date")
    public String inclusive_date;

    public OfficeOrderModel(int id, String so_no, String inclusive_date) {
        this.id = id;
        this.so_no = so_no;
        this.inclusive_date = inclusive_date;
    }
}