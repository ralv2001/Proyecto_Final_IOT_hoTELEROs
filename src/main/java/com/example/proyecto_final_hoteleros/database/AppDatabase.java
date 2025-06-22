package com.example.proyecto_final_hoteleros.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.proyecto_final_hoteleros.database.dao.FileStorageDao;
import com.example.proyecto_final_hoteleros.database.dao.UserRegistrationDao;
import com.example.proyecto_final_hoteleros.database.entities.FileStorageEntity;
import com.example.proyecto_final_hoteleros.database.entities.UserRegistrationEntity;

@Database(
        entities = {UserRegistrationEntity.class, FileStorageEntity.class},
        version = 2,  // ← INCREMENTAR A 2
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "hoteleros_database";
    private static volatile AppDatabase INSTANCE;

    public abstract UserRegistrationDao userRegistrationDao();
    public abstract FileStorageDao fileStorageDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME
                            )
                            .fallbackToDestructiveMigration() // Esto limpiará la DB anterior
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}