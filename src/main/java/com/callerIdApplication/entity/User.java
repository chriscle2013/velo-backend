package com.callerIdApplication.entity;


import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "app_user")
public class User {


    @Id
    @Column(name = "user_id")
    private Integer userId;


    @Column(name = "phone_number", unique = true, nullable = false)
    private String phoneNumber;


    @Column(name = "password", nullable = false)
    private String password;


    @Column(name = "user_name", nullable = false) // RE-ACTIVADO FÍSICAMENTE (Requerido por DB)
    private String userName;


    @Column(name = "email", nullable = false)      // RE-ACTIVADO FÍSICAMENTE (Requerido por DB)
    private String email;


    @Transient
    private String uuid;


    @Transient
    private boolean isActive = true;


    @Transient
    private List<Contact> contacts = new ArrayList<>();


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


    public String getUuid() {
        if (this.uuid == null || this.uuid.isEmpty()) {
            return "USR" + (this.phoneNumber != null ? this.phoneNumber : "0000");
        }
        return uuid;
    }


    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


    public boolean isActive() {
        return isActive;
    }


    public void setActive(boolean active) {
        this.isActive = active;
    }


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
