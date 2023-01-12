package com.example.doorlock.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Message.class}, version = 1, exportSchema = false)
public abstract class MqttDatabase extends RoomDatabase {

    public abstract MessageDao messageDao();

    private static MqttDatabase INSTANCE;

    public static MqttDatabase getDbInstance(Context context){
        if(INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),MqttDatabase.class,"History")
                    .allowMainThreadQueries().build();
        }
        return INSTANCE;
    }
}
