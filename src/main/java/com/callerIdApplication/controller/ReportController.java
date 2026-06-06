package com.callerIdApplication.controller;

import com.callerIdApplication.entity.Report;
import com.callerIdApplication.entity.Spam;
import com.callerIdApplication.repostitory.ReportDao;
import com.callerIdApplication.repostitory.SpamDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ReportController {

    @Autowired
    private ReportDao reportDao;

    @Autowired
    private SpamDao spamDao;

    @PostMapping("/report")
    public ResponseEntity<Map<String, Object>> reportNumber(@RequestBody Map<String, Object> reportData) {
        Map<String, Object> response = new HashMap<>();

        try {
            String phoneNumber = (String) reportData.get("phoneNumber");
            String category = (String) reportData.get("category");
            String comment = (String) reportData.get("comment");
            Boolean spammer = (Boolean) reportData.get("spammer");

            if (phoneNumber == null || phoneNumber.isEmpty()) {
                response.put("error", "Número de teléfono requerido");
                return ResponseEntity.badRequest().body(response);
            }

            // Guardar en tabla Report
            Report existingReport = reportDao.findByPhoneNumber(phoneNumber).stream().findFirst().orElse(null);

            Report report;
            if (existingReport != null) {
                report = existingReport;
                report.setCategory(category);
                report.setComment(comment);
                report.setSpammer(spammer != null ? spammer : true);
            } else {
                report = new Report(phoneNumber, category, comment);
                if (spammer != null) {
                    report.setSpammer(spammer);
                }
            }
            reportDao.save(report);

            // Actualizar tabla Spam
            List<Spam> spamList = spamDao.findBynumber(phoneNumber);
            Spam spam;
            if (spamList != null && !spamList.isEmpty()) {
                spam = spamList.get(0);
                spam.setSpammer(spammer != null ? spammer : true);
            } else {
                spam = new Spam();
                spam.setNumber(phoneNumber);
                spam.setName("SPAM");
                spam.setSpammer(spammer != null ? spammer : true);
            }
            spamDao.save(spam);

            response.put("success", true);
            response.put("message", "Reporte enviado correctamente");
            response.put("phoneNumber", phoneNumber);
            response.put("spammer", report.isSpammer());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Error al procesar reporte: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/report/{phoneNumber}")
    public ResponseEntity<Map<String, Object>> getReport(@PathVariable String phoneNumber) {
        Map<String, Object> response = new HashMap<>();

        try {
            Report report = reportDao.findByPhoneNumber(phoneNumber).stream().findFirst().orElse(null);

            if (report != null) {
                response.put("exists", true);
                response.put("phoneNumber", report.getPhoneNumber());
                response.put("category", report.getCategory());
                response.put("spammer", report.isSpammer());
            } else {
                response.put("exists", false);
                response.put("spammer", false);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
