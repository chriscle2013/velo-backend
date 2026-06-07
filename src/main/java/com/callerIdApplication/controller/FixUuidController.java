package com.callerIdApplication.controller;

import com.callerIdApplication.entity.User;
import com.callerIdApplication.repostitory.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FixUuidController {

    @Autowired
    private UserDao userDao;

    // Endpoint 1: Permite auditar el UUID real de un número de teléfono en formato de texto plano
    @GetMapping("/ver-uuid/{phoneNumber}")
    public String verUuid(@PathVariable String phoneNumber) {
        try {
            User user = userDao.findByphoneNumber(phoneNumber);
            if (user != null) {
                String uuid = user.getUuid();
                if (uuid == null || uuid.isEmpty()) {
                    return "ℹ️ El usuario con teléfono " + phoneNumber + " NO tiene ningún UUID asignado todavía.";
                } else {
                    return "✅ El UUID actual en Base de Datos de " + phoneNumber + " es: " + uuid;
                }
            }
            return "❌ No se encontró ningún usuario con el número de teléfono: " + phoneNumber;
        } catch (Exception e) {
            return "❌ Ocurrió un error al procesar la solicitud: " + e.getMessage();
        }
    }

    // Endpoint 2: Permite cambiar o forzar de forma manual el UUID de un usuario mediante el navegador web
    @GetMapping("/fix-uuid/{phoneNumber}/{nuevoUuid}")
    public String fixUuid(@PathVariable String phoneNumber, @PathVariable String nuevoUuid) {
        try {
            User user = userDao.findByphoneNumber(phoneNumber);
            if (user == null) {
                return "❌ Error: No se encontró ningún usuario con el teléfono " + phoneNumber;
            }

            // Asignación forzada de la llave
            user.setUuid(nuevoUuid);
            userDao.save(user);
            return "🚀 Éxito Absoluto: El UUID del usuario " + phoneNumber + " ha sido modificado manualmente a: " + nuevoUuid;
        } catch (Exception e) {
            return "❌ Error fatal al intentar actualizar el UUID: " + e.getMessage();
        }
    }
}
