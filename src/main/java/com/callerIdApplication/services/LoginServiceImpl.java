package com.callerIdApplication.services;

import java.time.LocalDateTime;

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
public class LoginServiceImpl implements LoginService{

    @Autowired
    private UserDao cDao;
    
    @Autowired
    private SessionDao sDao;
    
    @Override
    public String logIntoAccount(LoginDTO dto) throws LoginException {
        
        // 1. Buscar al usuario por número de teléfono
        User existingCustomer = cDao.findByphoneNumber(dto.getPhoneNumber());
        
        if (existingCustomer == null) {
            throw new LoginException("Please Enter a valid mobile number");
        }
        
        // 2. Validar la contraseña
        if (!existingCustomer.getPassword().equals(dto.getPassword())) {
            throw new LoginException("Please Enter a valid password");
        }
        
        // 3. Generar un nuevo UUID
        String key = RandomString.make(6);
        
        // 4. Buscar si ya existe una sesión para este usuario y eliminarla (para evitar duplicados)
        CurrentUserSession existingSession = sDao.findByUserId(existingCustomer.getUserId());
        if (existingSession != null) {
            sDao.delete(existingSession);
        }
        
        // 5. Crear y guardar la nueva sesión
        CurrentUserSession currentUserSession = new CurrentUserSession();
        currentUserSession.setUserId(existingCustomer.getUserId());
        currentUserSession.setLocalDateTime(LocalDateTime.now());
        currentUserSession.setUuid(key);
        sDao.save(currentUserSession);
        
        // 6. Devolver SOLO el UUID (String)
        return key;
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
