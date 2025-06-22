package com.example.proyecto_final_hoteleros.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.proyecto_final_hoteleros.database.entities.FileStorageEntity;

import java.util.List;

@Dao
public interface FileStorageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertFile(FileStorageEntity fileStorage);

    @Update
    void updateFile(FileStorageEntity fileStorage);

    @Delete
    void deleteFile(FileStorageEntity fileStorage);

    @Query("SELECT * FROM file_storage WHERE id = :id")
    FileStorageEntity getFileById(int id);

    @Query("SELECT * FROM file_storage WHERE registration_id = :registrationId")
    List<FileStorageEntity> getFilesByRegistrationId(int registrationId);

    @Query("SELECT * FROM file_storage WHERE registration_id = :registrationId AND file_type = :fileType")
    FileStorageEntity getFileByRegistrationIdAndType(int registrationId, String fileType);

    @Query("SELECT * FROM file_storage WHERE file_type = :fileType")
    List<FileStorageEntity> getFilesByType(String fileType);

    @Query("DELETE FROM file_storage WHERE registration_id = :registrationId")
    void deleteFilesByRegistrationId(int registrationId);

    @Query("DELETE FROM file_storage WHERE registration_id = :registrationId AND file_type = :fileType")
    void deleteFileByRegistrationIdAndType(int registrationId, String fileType);

    @Query("SELECT COUNT(*) FROM file_storage WHERE registration_id = :registrationId")
    int getFileCountByRegistrationId(int registrationId);

    @Query("SELECT SUM(file_size) FROM file_storage WHERE registration_id = :registrationId")
    long getTotalFileSizeByRegistrationId(int registrationId);

    @Query("SELECT * FROM file_storage ORDER BY created_at DESC")
    List<FileStorageEntity> getAllFiles();
}