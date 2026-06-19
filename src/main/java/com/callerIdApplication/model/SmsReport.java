package com.callerid.backend.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sms_reports")
public class SmsReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phoneNumber;      // El número que envió el SMS
    @Column(columnDefinition = "TEXT")
    private String messageBody;      // El texto del mensaje
    private String category;         // "Fraude", "Spam", "Phishing"
    private String reportedBy;       // El número del usuario que reporta
    private LocalDateTime reportDate;

    // Getters y Setters
    public SmsReport() { this.reportDate = LocalDateTime.now(); }
    public Long getId() { return id; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getMessageBody() { return messageBody; }
    public void setMessageBody(String messageBody) { this.messageBody = messageBody; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }
}
