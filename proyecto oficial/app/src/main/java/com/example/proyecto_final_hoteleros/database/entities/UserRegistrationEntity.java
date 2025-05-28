package com.example.proyecto_final_hoteleros.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "user_registration_data")
public class UserRegistrationEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "user_type")
    public String userType;

    @ColumnInfo(name = "nombres")
    public String nombres;

    @ColumnInfo(name = "apellidos")
    public String apellidos;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "fecha_nacimiento")
    public String fechaNacimiento;

    @ColumnInfo(name = "telefono")
    public String telefono;

    @ColumnInfo(name = "tipo_documento")
    public String tipoDocumento;

    @ColumnInfo(name = "numero_documento")
    public String numeroDocumento;

    @ColumnInfo(name = "direccion")
    public String direccion;

    @ColumnInfo(name = "placa_vehiculo")
    public String placaVehiculo;

    @ColumnInfo(name = "password")
    public String password;

    @ColumnInfo(name = "is_completed")
    public boolean isCompleted;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    // Constructor vacío requerido por Room
    public UserRegistrationEntity() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isCompleted = false;
    }

    // Constructor con parámetros
    public UserRegistrationEntity(String userType, String nombres, String apellidos,
                                  String email, String fechaNacimiento, String telefono,
                                  String tipoDocumento, String numeroDocumento, String direccion,
                                  String placaVehiculo, String password) {
        this();
        this.userType = userType;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.email = email;
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.direccion = direccion;
        this.placaVehiculo = placaVehiculo;
        this.password = password;
    }

    public void updateTimestamp() {
        this.updatedAt = System.currentTimeMillis();
    }
}