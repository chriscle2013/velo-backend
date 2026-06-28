package com.callerIdApplication.controller;

import com.callerIdApplication.entity.NameAssignment;
import com.callerIdApplication.repostitory.NameAssignmentDao;
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
    private NameAssignmentDao nameAssignmentDao;

    /**
     * Clase de Transferencia de Datos (DTO) 
     * Mapea exactamente el JSON enviado por la App Android
     */
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

    /**
     * ENDPOINT: Sugerir nombre para un número
     * Guarda la información en la base de datos de la comunidad Velo
     */
    @PostMapping("/assign")
    public ResponseEntity<Map<String, Object>> assignName(@RequestBody NameAssignmentDTO dto) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. Validación de parámetros
            if (dto.getPhoneNumber() == null || dto.getPhoneNumber().isEmpty() ||
                dto.getAssignedName() == null || dto.getAssignedName().isEmpty()) {
                
                response.put("status", "error");
                response.put("message", "Datos incompletos");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // 2. Normalización del número de teléfono
            String cleanNumber = dto.getPhoneNumber().replaceAll("[^0-9]", "");
            if (cleanNumber.length() == 12 && cleanNumber.startsWith("57")) {
                cleanNumber = cleanNumber.substring(2);
            }

            // 3. Persistencia en la tabla de la comunidad (name_assignments)
            // Esto permite que el buscador encuentre el nombre sugerido
            NameAssignment assignment = new NameAssignment(
                cleanNumber, 
                dto.getAssignedName(), 
                dto.getAssignedBy()
            );
            
            nameAssignmentDao.save(assignment);

            // 4. Respuesta exitosa
            response.put("status", "success");
            response.put("message", "Sugerencia guardada correctamente");
            response.put("assignedName", dto.getAssignedName());
            response.put("phoneNumber", cleanNumber);

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error al procesar sugerencia: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
