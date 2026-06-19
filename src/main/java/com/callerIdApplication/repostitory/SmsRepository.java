package com.callerid.backend.repository;

import com.callerid.backend.model.SmsReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SmsRepository extends JpaRepository<SmsReport, Long> {
    // Buscar si un mensaje similar ya ha sido reportado
    List<SmsReport> findByPhoneNumber(String phoneNumber);
}
