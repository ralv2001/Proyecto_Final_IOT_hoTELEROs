package com.example.proyecto_final_hoteleros.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.proyecto_final_hoteleros.database.entities.UserRegistrationEntity;

import java.util.List;

@Dao
public interface UserRegistrationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertUserRegistration(UserRegistrationEntity userRegistration);

    @Update
    void updateUserRegistration(UserRegistrationEntity userRegistration);

    @Delete
    void deleteUserRegistration(UserRegistrationEntity userRegistration);

    @Query("SELECT * FROM user_registration_data WHERE id = :id")
    UserRegistrationEntity getUserRegistrationById(int id);

    @Query("SELECT * FROM user_registration_data WHERE email = :email")
    UserRegistrationEntity getUserRegistrationByEmail(String email);

    @Query("SELECT * FROM user_registration_data WHERE user_type = :userType")
    List<UserRegistrationEntity> getUserRegistrationsByType(String userType);

    @Query("SELECT * FROM user_registration_data WHERE is_completed = :isCompleted")
    List<UserRegistrationEntity> getUserRegistrationsByStatus(boolean isCompleted);

    @Query("SELECT * FROM user_registration_data ORDER BY created_at DESC LIMIT 1")
    UserRegistrationEntity getLatestUserRegistration();

    @Query("SELECT * FROM user_registration_data ORDER BY created_at DESC")
    List<UserRegistrationEntity> getAllUserRegistrations();

    @Query("DELETE FROM user_registration_data WHERE is_completed = 0 AND created_at < :timestamp")
    void deleteIncompleteRegistrationsOlderThan(long timestamp);

    @Query("DELETE FROM user_registration_data WHERE id = :id")
    void deleteUserRegistrationById(int id);

    @Query("UPDATE user_registration_data SET is_completed = :isCompleted, updated_at = :timestamp WHERE id = :id")
    void updateCompletionStatus(int id, boolean isCompleted, long timestamp);
}