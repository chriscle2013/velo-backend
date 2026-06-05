package com.callerIdApplication.services;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.callerIdApplication.entity.CurrentUserSession;
import com.callerIdApplication.entity.LoginDTO;
import com.callerIdApplication.entity.User;
import com.callerIdApplication.exceptions.LoginException;
import com.callerIdApplication.repostitory.SessionDao;
import com.callerIdApplication.repostitory.UserDao;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private UserDao cDao;
    
    @Autowired
    private SessionDao sDao;
    
    @Override
    public String logIntoAccount(LoginDTO dto) throws LoginException {
        
        User existingCustomer = cDao.findByphoneNumber(dto.getPhoneNumber());
        
        if (existingCustomer == null) {
            throw new LoginException("Please Enter a valid mobile number");
        }
        
        if (!existingCustomer.getPassword().equals(dto.getPassword())) {
            throw new LoginException("Please Enter a valid password");
        }
        
        // ⭐ REUTILIZAR EL UUID EXISTENTE - NUNCA GENERAR UNO NUEVO
        String uuid = existingCustomer.getUuid();
        
        // Si por alguna razón no tiene UUID (usuario antiguo), se asigna uno
        if (uuid == null || uuid.isEmpty()) {
            // Esto solo debería pasar con usuarios creados antes de implementar esta funcionalidad
            uuid = java.util.UUID.randomUUID().toString().substring(0, 8);
            existingCustomer.setUuid(uuid);
            cDao.save(existingCustomer);
        }
        
        // Eliminar sesión anterior si existe
        CurrentUserSession existingSession = sDao.findByUserId(existingCustomer.getUserId());
        if (existingSession != null) {
            sDao.delete(existingSession);
        }
        
        // Crear nueva sesión con el mismo UUID
        CurrentUserSession currentUserSession = new CurrentUserSession();
        currentUserSession.setUserId(existingCustomer.getUserId());
        currentUserSession.setLocalDateTime(LocalDateTime.now());
        currentUserSession.setUuid(uuid);
        sDao.save(currentUserSession);
        
        return uuid;
    }

    @Override
    public String logOutFromAccount(String key) throws LoginException {
        
        CurrentUserSession validCustomerSession = sDao.findByUuid(key);
        
        if (validCustomerSession == null) {
            throw new LoginException("User Not Logged In with this number");
        }
        
        sDao.delete(validCustomerSession);
        
        return "Logged Out !";
    }
}
