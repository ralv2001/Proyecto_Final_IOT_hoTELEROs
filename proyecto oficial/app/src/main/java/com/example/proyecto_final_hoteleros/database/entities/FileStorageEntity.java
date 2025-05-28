package com.example.proyecto_final_hoteleros.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;

@Entity(tableName = "file_storage",
        foreignKeys = @ForeignKey(
                entity = UserRegistrationEntity.class,
                parentColumns = "id",
                childColumns = "registration_id",
                onDelete = ForeignKey.CASCADE
        ))
public class FileStorageEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "registration_id")
    public int registrationId;

    @ColumnInfo(name = "file_type")
    public String fileType; // "PDF" o "PHOTO"

    @ColumnInfo(name = "original_name")
    public String originalName;

    @ColumnInfo(name = "stored_path")
    public String storedPath;

    @ColumnInfo(name = "file_size")
    public long fileSize;

    @ColumnInfo(name = "mime_type")
    public String mimeType;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    // Constantes para tipos de archivo
    public static final String FILE_TYPE_PDF = "PDF";
    public static final String FILE_TYPE_PHOTO = "PHOTO";

    // Constructor vacío requerido por Room
    public FileStorageEntity() {
        this.createdAt = System.currentTimeMillis();
    }

    // Constructor con parámetros
    public FileStorageEntity(int registrationId, String fileType, String originalName,
                             String storedPath, long fileSize, String mimeType) {
        this();
        this.registrationId = registrationId;
        this.fileType = fileType;
        this.originalName = originalName;
        this.storedPath = storedPath;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
    }

    // Métodos helper
    public boolean isPdf() {
        return FILE_TYPE_PDF.equals(fileType);
    }

    public boolean isPhoto() {
        return FILE_TYPE_PHOTO.equals(fileType);
    }
}