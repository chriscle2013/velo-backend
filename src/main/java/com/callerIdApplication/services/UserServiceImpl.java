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

import net.bytebuddy.utility.RandomString;

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
        
        // Usar el UUID que ya tiene el usuario (NO generar uno nuevo)
        String uuid = existingCustomer.getUuid();
        
        if (uuid == null || uuid.isEmpty()) {
            // Si por alguna razón no tiene UUID, generar uno
            uuid = RandomString.make(8);
            existingCustomer.setUuid(uuid);
            cDao.save(existingCustomer);
        }
        
        // Eliminar sesión anterior si existe
        CurrentUserSession existingSession = sDao.findByUserId(existingCustomer.getUserId());
        if (existingSession != null) {
            sDao.delete(existingSession);
        }
        
        // Crear nueva sesión
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
