package com.callerIdApplication.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "phone_number", unique = true, nullable = false)
    private String phoneNumber;

    @Column(name = "password", nullable = false)
    private String password;

    // @Transient evita que Hibernate intente buscar o insertar esta columna en PostgreSQL
    @Transient
    private String uuid;

    @Transient
    private String userName;

    @Transient
    private String email;

    @Transient
    private boolean isActive = true;

    // CORRECCIÓN DE TIPO: Usamos la entidad Contact propia del proyecto mapeada como Transient
    @Transient
    private List<Contact> contacts = new ArrayList<>();

    // Constructor obligatorio para JPA
    public User() {
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUuid() {
        // Garantiza que la app móvil reciba un identificador único aunque no esté en la BD física
        if (this.uuid == null || this.uuid.isEmpty()) {
            return "USR" + (this.phoneNumber != null ? this.phoneNumber : "0000");
        }
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    // GETTER Y SETTER CORREGIDOS CON EL TIPO DE DATO EXIGIDO POR USERSERVICEIMPL
    public List<Contact> getContacts() {
        if (this.contacts == null) {
            this.contacts = new ArrayList<>();
        }
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }
}
