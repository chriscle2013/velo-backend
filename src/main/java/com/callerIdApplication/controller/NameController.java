package com.callerIdApplication.controller;

import com.callerIdApplication.entity.User;
import com.callerIdApplication.entity.Spam; // Asegúrate de que tu entidad de reportes se llame Spam o SpamNumber
import com.callerIdApplication.repostitory.UserDao;
import com.callerIdApplication.repostitory.SpamDao; // Asegúrate de que coincida con tu repositorio de Spam (ej: sDao o spamDao)
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/name")
@CrossOrigin(origins = "*")
public class NameController {

    @Autowired
    private UserDao userDao;

    @Autowired
    private SpamDao spamDao; // Repositorio comunitario para guardar etiquetas y reportes externos de llamadas

    // Estructura de transferencia de datos idéntica a lo enviado por Volley desde Android
    public static class NameAssignmentDTO {
        private String phoneNumber;
        private String assignedName;
        private String assignedBy;

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getAssignedName() {
            return assignedName;
        }

        public void setAssignedName(String assignedName) {
            this.assignedName = assignedName;
        }

        public String getAssignedBy() {
            return assignedBy;
        }

        public void setAssignedBy(String assignedBy) {
            this.assignedBy = assignedBy;
        }
    }

    @PostMapping("/assign")
    public ResponseEntity<Map<String, Object>> assignName(@RequestBody NameAssignmentDTO dto) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. Validaciones preventivas de los datos obligatorios enviados por la App
            if (dto.getPhoneNumber() == null || dto.getPhoneNumber().isEmpty() ||
                dto.getAssignedName() == null || dto.getAssignedName().isEmpty()) {
                
                response.put("status", "error");
                response.put("message", "Parámetros inválidos: teléfono o nombre ausente.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // 2. Estandarización del número de teléfono (Remover espacios y códigos de país +57)
            String cleanNumber = dto.getPhoneNumber().replaceAll("[^0-9]", "");
            if (cleanNumber.length() == 12 && cleanNumber.startsWith("57")) {
                cleanNumber = cleanNumber.substring(2);
            }

            // 3. BUSQUEDA ARQUITECTURA TRUECALLER:
            // Verificamos si el número de teléfono corresponde a un usuario de la aplicación
            User registeredUser = userDao.findByphoneNumber(cleanNumber);

            if (registeredUser != null) {
                // Si es un usuario real de la app, actualizamos el nombre de su perfil público
                registeredUser.setUserName(dto.getAssignedName());
                userDao.save(registeredUser);
            } else {
                // MODELO DIRECTORIO COMUNITARIO (Truecaller):
                // Al ser un número externo desconocido que llama, creamos o actualizamos su sugerencia 
                // en la tabla de Spam/Directorio Comunitario sin crear cuentas fantasmas.
                Spam communityReport = spamDao.findByphoneNumber(cleanNumber);
                
                if (communityReport != null) {
                    // Si el número ya tenía una sugerencia o queja previa, actualizamos la etiqueta con la nueva sugerencia
                    communityReport.setReason(dto.getAssignedName()); // O el campo de tu entidad Spam para el texto descriptivo
                    spamDao.save(communityReport);
                } else {
                    // Si es la primera vez que la comunidad lo reporta/sugiere, creamos el registro en la tabla comunitaria
                    Spam newSpamRecord = new Spam();
                    newSpamRecord.setPhoneNumber(cleanNumber);
                    newSpamRecord.setReason(dto.getAssignedName()); // Asignamos el nombre sugerido como la razón o identificación
                    // Si tu entidad Spam requiere campos obligatorios adicionales como contador, puedes inicializarlos aquí de forma segura
                    
                    spamDao.save(newSpamRecord);
                }
            }

            // 4. RESPUESTA EXITOSA SIN CONFLICTOS DE POSTGRESQL
            response.put("status", "success");
            response.put("message", "Sugerencia de nombre guardada en el directorio comunitario con éxito.");
            response.put("assignedName", dto.getAssignedName());
            response.put("phoneNumber", cleanNumber);

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            // Protección total de la API ante cualquier eventualidad
            response.put("status", "error");
            response.put("message", "Error en el procesamiento del directorio: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
