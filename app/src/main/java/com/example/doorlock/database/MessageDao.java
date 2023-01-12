package com.example.doorlock.database;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MessageDao {

    @Query("SELECT * FROM message")
    List<Message>getMessages();

    @Insert
    void insertMessage(Message...messages);

    @Delete
    void delete(Message message);
}
