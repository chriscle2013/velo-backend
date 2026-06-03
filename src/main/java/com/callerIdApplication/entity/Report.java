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
    
    // Getters
    public Long getId() { return id; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getCategory() { return category; }
    public String getComment() { return comment; }
    public boolean isSpammer() { return spammer; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setCategory(String category) { this.category = category; }
    public void setComment(String comment) { this.comment = comment; }
    public void setSpammer(boolean spammer) { this.spammer = spammer; }
}
