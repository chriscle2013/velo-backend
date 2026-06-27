package com.callerIdApplication.repostitory;

import com.callerIdApplication.entity.SmsReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SmsDao extends JpaRepository<SmsReport, Long> {
    List<SmsReport> findByPhoneNumber(String phoneNumber);
}
