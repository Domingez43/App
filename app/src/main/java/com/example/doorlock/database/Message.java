package com.example.doorlock.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Message {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "message")
    public String message;
}
