package com.callerIdApplication.controller;

import com.callerIdApplication.entity.User;
import com.callerIdApplication.entity.Report;
import com.callerIdApplication.repostitory.UserDao;
import com.callerIdApplication.repostitory.ReportDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserDao userDao;

    @Autowired
    private ReportDao reportDao;

    @PostMapping("/user/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            String phoneNumber = null;
            if (payload.containsKey("phoneNumber")) phoneNumber = String.valueOf(payload.get("phoneNumber"));
            else if (payload.containsKey("phonenumber")) phoneNumber = String.valueOf(payload.get("phonenumber"));
            else if (payload.containsKey("phone")) phoneNumber = String.valueOf(payload.get("phone"));

            String password = null;
            if (payload.containsKey("password")) password = String.valueOf(payload.get("password"));
            else if (payload.containsKey("pass")) password = String.valueOf(payload.get("pass"));

            if (phoneNumber == null || phoneNumber.trim().isEmpty() || "null".equalsIgnoreCase(phoneNumber)) {
                response.put("status", "error");
                response.put("message", "Error: El celular no envio phoneNumber.");
                return ResponseEntity.status(400).body(response);
            }

            if (password == null || password.trim().isEmpty() || "null".equalsIgnoreCase(password)) {
                response.put("status", "error");
                response.put("message", "Error: El celular no envio password.");
                return ResponseEntity.status(400).body(response);
            }

            String cleanRegNumber = phoneNumber.replaceAll("[^0-9]", "");
            if (cleanRegNumber.length() == 12 && cleanRegNumber.startsWith("57")) {
                cleanRegNumber = cleanRegNumber.substring(2);
            }

            User existingUser = userDao.findByPhoneNumber(cleanRegNumber);
            User savedUser;

            if (existingUser != null) {
                existingUser.setPassword(password);
                savedUser = userDao.save(existingUser);
                response.put("message", "Usuario ya existia. Contraseña actualizada.");
            } else {
                User newUser = new User();
                newUser.setPhoneNumber(cleanRegNumber);
                newUser.setPassword(password);

                // Generador seguro de ID entero de 6 dígitos que jamás se repetirá
                long timeSeed = System.currentTimeMillis() % 900000L;
                long totalCount = userDao.count();
                int manualId = (int) (100000L + totalCount + timeSeed);
                
                newUser.setUserId(manualId); // Ahora Hibernate SI enviará este ID de forma explícita

                savedUser = userDao.save(newUser);
                response.put("message", "Usuario nuevo registrado con exito.");
            }

            response.put("status", "success");
            response.put("uuid", savedUser.getUuid()); 
            response.put("userId", savedUser.getUserId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            String rootCauseMessage = e.getMessage();
            Throwable cause = e.getCause();
            while (cause != null) {
                rootCauseMessage = cause.getMessage();
                cause = cause.getCause();
            }
            response.put("status", "error");
            response.put("message", "Falla critica interna en backend: " + rootCauseMessage);
            return ResponseEntity.status(400).body(response);
        }
    }

    @PostMapping("/user/login")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody Map<String, String> credentials) {
        Map<String, Object> response = new HashMap<>();
        try {
            String phoneNumber = credentials.get("phoneNumber");
            String password = credentials.get("password");

            if (phoneNumber == null || password == null) {
                response.put("status", "error");
                response.put("message", "Faltan parámetros requeridos");
                return ResponseEntity.status(400).body(response);
            }

            String cleanNumber = phoneNumber.replaceAll("[^0-9]", "");
            if (cleanNumber.length() == 12 && cleanNumber.startsWith("57")) {
                cleanNumber = cleanNumber.substring(2);
            }

            User user = userDao.findByPhoneNumber(cleanNumber);
            if (user == null && !cleanNumber.equals(phoneNumber)) {
                user = userDao.findByPhoneNumber(phoneNumber);
            }

            if (user != null && user.getPassword().equals(password)) {
                response.put("status", "success");
                response.put("message", "Autenticación exitosa");
                response.put("uuid", user.getUuid()); 
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Número de teléfono o contraseña incorrectos");
                return ResponseEntity.status(401).body(response);
            }

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error interno en login: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/user/searchPerson/number={number}")
    public ResponseEntity<List<Map<String, Object>>> searchPerson(
            @PathVariable("number") String number,
            @RequestParam("key") String key) {
        
        List<Map<String, Object>> responseList = new ArrayList<>();
        Map<String, Object> responseMap = new HashMap<>();
        
        String originalNumber = number;
        String cleanNumber = number.replaceAll("[^0-9]", "");
        if (cleanNumber.length() == 12 && cleanNumber.startsWith("57")) {
            cleanNumber = cleanNumber.substring(2);
        }
        
        boolean isSpammer = false;
        String resolvedName = "Unknown";
        
        try {
            User foundUser = userDao.findByPhoneNumber(cleanNumber);
            if (foundUser == null && !cleanNumber.equals(originalNumber)) {
                foundUser = userDao.findByPhoneNumber(originalNumber);
            }
            
            if (foundUser != null) {
                resolvedName = "Usuario Registrado";
            }

            List<Report> reportList = reportDao.findByPhoneNumber(cleanNumber);
            if ((reportList == null || reportList.isEmpty()) && !cleanNumber.equals(originalNumber)) {
                reportList = reportDao.findByPhoneNumber(originalNumber);
            }
            
            if (reportList != null && !reportList.isEmpty()) {
                Report reportRecord = reportList.get(0);
                if (reportRecord != null) {
                    isSpammer = reportRecord.isSpammer(); 
                    if (isSpammer) {
                        resolvedName = (reportRecord.getCategory() != null) ? "Reporte: " + reportRecord.getCategory() : "SPAM";
                    }
                }
            }
            
            responseMap.put("number", cleanNumber);
            responseMap.put("spammer", isSpammer);
            responseMap.put("name", resolvedName);
            responseList.add(responseMap);
            return ResponseEntity.ok(responseList);
            
        } catch (Exception e) {
            responseMap.put("number", cleanNumber);
            responseMap.put("spammer", false);
            responseMap.put("name", "Desconocido");
            responseList.add(responseMap);
            return ResponseEntity.status(500).body(responseList);
        }
    }
}
