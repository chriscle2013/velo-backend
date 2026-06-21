package com.callerIdApplication.repostitory;

import com.velo.model.SmsSpamReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SmsSpamRepository extends JpaRepository<SmsSpamReport, Long> {
    // Permite listar los reportes por estado (ej: buscar solo los PENDING)
    List<SmsSpamReport> findByStatus(String status);
}
