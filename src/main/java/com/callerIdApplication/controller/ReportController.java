package com.callerIdApplication.controller;

import com.callerIdApplication.entity.Report;
import com.callerIdApplication.repostitory.ReportDao;
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
            
            // Verificar si ya existe un reporte para este número
            Report existingReport = null;
            try {
                existingReport = reportDao.findByPhoneNumber(phoneNumber).stream().findFirst().orElse(null);
            } catch (Exception e) {
                // Si el método no existe, creamos uno nuevo
            }
            
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
    
    // ========== NUEVO ENDPOINT PARA MARCAR NÚMEROS COMO SEGUROS ==========
    
    @GetMapping("/set-safe/{phoneNumber}")
    public String setNumberAsSafe(@PathVariable String phoneNumber) {
        try {
            List<Report> reports = reportDao.findByPhoneNumber(phoneNumber);
            if (!reports.isEmpty()) {
                for (Report report : reports) {
                    report.setSpammer(false);
                    reportDao.save(report);
                }
                return "✅ Número " + phoneNumber + " marcado como SEGURO (spammer = false)";
            } else {
                // Si no existe reporte, crear uno con spammer = false
                Report newReport = new Report(phoneNumber, "manual", "Marcado como seguro desde admin");
                newReport.setSpammer(false);
                reportDao.save(newReport);
                return "✅ Número " + phoneNumber + " creado y marcado como SEGURO";
            }
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }
    
    // Endpoint para marcar como spam
    @GetMapping("/set-spam/{phoneNumber}")
    public String setNumberAsSpam(@PathVariable String phoneNumber) {
        try {
            List<Report> reports = reportDao.findByPhoneNumber(phoneNumber);
            if (!reports.isEmpty()) {
                for (Report report : reports) {
                    report.setSpammer(true);
                    reportDao.save(report);
                }
                return "🚨 Número " + phoneNumber + " marcado como SPAM (spammer = true)";
            } else {
                Report newReport = new Report(phoneNumber, "manual", "Marcado como spam desde admin");
                newReport.setSpammer(true);
                reportDao.save(newReport);
                return "🚨 Número " + phoneNumber + " creado y marcado como SPAM";
            }
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }
}
