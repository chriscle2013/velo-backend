package com.callerIdApplication.controller;

import com.callerIdApplication.entity.User;
import com.callerIdApplication.entity.Report;
import com.callerIdApplication.entity.NameAssignment;
import com.callerIdApplication.repostitory.UserDao;
import com.callerIdApplication.repostitory.ReportDao;
import com.callerIdApplication.repostitory.NameAssignmentDao;
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

    @Autowired
    private NameAssignmentDao nameAssignmentDao;

    /**
     * REGISTRO Y ACTUALIZACIÓN DE USUARIOS
     * Permite guardar datos de perfil (Nombre, Email, Trabajo)
     */
    @PostMapping("/user/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            String phoneNumber = payload.containsKey("phoneNumber") ? String.valueOf(payload.get("phoneNumber")) : null;
            String password = payload.containsKey("password") ? String.valueOf(payload.get("password")) : null;
            String userName = payload.containsKey("userName") ? String.valueOf(payload.get("userName")) : "Usuario Velo";
            String email = payload.containsKey("email") ? String.valueOf(payload.get("email")) : "";
            String work = payload.containsKey("work") ? String.valueOf(payload.get("work")) : "";

            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Número de teléfono requerido");
                return ResponseEntity.status(400).body(response);
            }

            // Normalización
            String cleanRegNumber = phoneNumber.replaceAll("[^0-9]", "");
            if (cleanRegNumber.length() == 12 && cleanRegNumber.startsWith("57")) {
                cleanRegNumber = cleanRegNumber.substring(2);
            }

            User existingUser = userDao.findByPhoneNumber(cleanRegNumber);
            User savedUser;

            if (existingUser != null) {
                if (password != null) existingUser.setPassword(password);
                existingUser.setUserName(userName);
                existingUser.setEmail(email);
                existingUser.setWork(work);
                savedUser = userDao.save(existingUser);
                response.put("message", "Perfil actualizado con éxito.");
            } else {
                User newUser = new User();
                newUser.setPhoneNumber(cleanRegNumber);
                newUser.setPassword(password != null ? password : "123456");
                newUser.setUserName(userName);
                newUser.setEmail(email);
                newUser.setWork(work);

                long timeSeed = System.currentTimeMillis() % 899999L;
                newUser.setUserId((int) (100000 + timeSeed));
                savedUser = userDao.save(newUser);
                response.put("message", "Usuario registrado exitosamente.");
            }

            response.put("status", "success");
            response.put("uuid", savedUser.getUuid()); 
            response.put("userId", savedUser.getUserId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error en persistencia: " + e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

    /**
     * INICIO DE SESIÓN
     * Devuelve los datos del perfil para restauración automática en la App
     */
    @PostMapping("/user/login")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody Map<String, String> credentials) {
        Map<String, Object> response = new HashMap<>();
        try {
            String phoneNumber = credentials.get("phoneNumber");
            String password = credentials.get("password");

            String cleanNumber = phoneNumber.replaceAll("[^0-9]", "");
            if (cleanNumber.length() == 12 && cleanNumber.startsWith("57")) {
                cleanNumber = cleanNumber.substring(2);
            }

            User user = userDao.findByPhoneNumber(cleanNumber);

            if (user != null && user.getPassword().equals(password)) {
                response.put("status", "success");
                response.put("uuid", user.getUuid()); 
                response.put("userName", user.getUserName());
                response.put("email", user.getEmail());
                response.put("work", user.getWork()); 
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Teléfono o contraseña incorrectos");
                return ResponseEntity.status(401).body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error interno: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * BÚSQUEDA DE IDENTIDAD VELO
     * Jerarquía: Spammer > Nombre sugerido por comunidad > Nombre de usuario
     */
    @GetMapping("/user/searchPerson/number={number}")
    public ResponseEntity<List<Map<String, Object>>> searchPerson(
            @PathVariable("number") String number,
            @RequestParam("key") String key) {
        
        List<Map<String, Object>> responseList = new ArrayList<>();
        Map<String, Object> responseMap = new HashMap<>();
        
        String cleanNumber = number.replaceAll("[^0-9]", "");
        if (cleanNumber.length() == 12 && cleanNumber.startsWith("57")) {
            cleanNumber = cleanNumber.substring(2);
        }
        
        try {
            // 1. Verificar si es un usuario registrado en Velo
            User foundUser = userDao.findByPhoneNumber(cleanNumber);
            String resolvedName = "Desconocido";
            if (foundUser != null) {
                resolvedName = foundUser.getUserName();
                if (foundUser.getWork() != null && !foundUser.getWork().isEmpty()) {
                    resolvedName += " (" + foundUser.getWork() + ")";
                }
            }

            // 2. Buscar si la comunidad ha sugerido un nombre (assignedName)
            String communityName = "";
            List<NameAssignment> communitySmsNames = nameAssignmentDao.findByPhoneNumberOrderByIdDesc(cleanNumber);
            if (communitySmsNames != null && !communitySmsNames.isEmpty()) {
                communityName = communitySmsNames.get(0).getAssignedName();
            }

            // 3. Verificar si el número está marcado como Spammer
            boolean isSpammer = false;
            String category = "";
            List<Report> reports = reportDao.findByPhoneNumber(cleanNumber);
            if (reports != null && !reports.isEmpty()) {
                Report firstReport = reports.get(0);
                isSpammer = firstReport.isSpammer(); 
                category = firstReport.getCategory();
            }
            
            responseMap.put("number", cleanNumber);
            responseMap.put("spammer", isSpammer);
            responseMap.put("name", resolvedName);
            responseMap.put("assignedName", communityName); // Se envía por separado para la App
            responseMap.put("category", category);
            
            responseList.add(responseMap);
            return ResponseEntity.ok(responseList);
            
        } catch (Exception e) {
            responseMap.put("number", cleanNumber);
            responseMap.put("name", "Error de red");
            responseList.add(responseMap);
            return ResponseEntity.status(500).body(responseList);
        }
    }

    /**
     * SUGERIR UN NOMBRE (COMUNIDAD)
     */
    @PostMapping("/name/assign")
    public ResponseEntity<Map<String, Object>> assignName(@RequestBody Map<String, String> payload, @RequestParam String key) {
        Map<String, Object> response = new HashMap<>();
        try {
            String phoneNumber = payload.get("phoneNumber");
            String assignedName = payload.get("assignedName");
            String assignedBy = payload.get("assignedBy");

            if (phoneNumber == null || assignedName == null) {
                response.put("status", "error");
                return ResponseEntity.badRequest().build();
            }

            NameAssignment assignment = new NameAssignment(phoneNumber, assignedName, assignedBy);
            nameAssignmentDao.save(assignment);

            response.put("status", "success");
            response.put("message", "Nombre sugerido guardado");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
