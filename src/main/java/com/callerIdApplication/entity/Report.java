package com.callerIdApplication.entity;

import javax.persistence.*;

@Entity
@Table(name = "report")
public class Report {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String phoneNumber;
    private String category;
    private String comment;
    private boolean spammer;
    
    public Report() {}
    
    public Report(String phoneNumber, String category, String comment) {
        this.phoneNumber = phoneNumber;
        this.category = category;
        this.comment = comment;
        this.spammer = true;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    
    public boolean isSpammer() { return spammer; }
    public void setSpammer(boolean spammer) { this.spammer = spammer; }
}
