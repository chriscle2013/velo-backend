package com.callerIdApplication.controller;

import com.callerIdApplication.entity.User;
import com.callerIdApplication.repostitory.SessionDao;
import com.callerIdApplication.repostitory.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserDao userDao;
    
    @Autowired
    private SessionDao sessionDao;
    
    // Contraseña de administrador (cámbiala después)
    private static final String ADMIN_PASSWORD = "admin123";
    
    @GetMapping("/login")
    public String showLoginForm() {
        return "admin/login";
    }
    
    @PostMapping("/login")
    public String doLogin(@RequestParam String password, HttpSession session) {
        if (ADMIN_PASSWORD.equals(password)) {
            session.setAttribute("admin_logged", true);
            return "redirect:/admin/dashboard";
        }
        return "redirect:/admin/login?error=true";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        if (session.getAttribute("admin_logged") == null) {
            return "redirect:/admin/login";
        }
        
        long totalUsers = userDao.count();
        long activeSessions = sessionDao.count();
        
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("activeSessions", activeSessions);
        return "admin/dashboard";
    }
    
    @GetMapping("/numbers")
    public String listNumbers(Model model, HttpSession session) {
        if (session.getAttribute("admin_logged") == null) {
            return "redirect:/admin/login";
        }
        
        Iterable<User> allUsers = userDao.findAll();
        model.addAttribute("users", allUsers);
        return "admin/numbers";
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }
}
