package com.callerIdApplication.controller;
import com.callerIdApplication.entity.Report;
import com.callerIdApplication.entity.User;
import com.callerIdApplication.repostitory.ReportDao;
import com.callerIdApplication.repostitory.SessionDao;
import com.callerIdApplication.repostitory.UserDao;
import com.callerIdApplication.model.PhoneNumber;
import com.callerIdApplication.model.SmsSpamReport;
import com.callerIdApplication.repostitory.PhoneNumberRepository;
import com.callerIdApplication.repostitory.SmsSpamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private UserDao userDao;
    
    @Autowired
    private SessionDao sessionDao;
    
    @Autowired
    private ReportDao reportDao;
    
    private static final String ADMIN_PASSWORD = "admin123";

    @Autowired 
    private PhoneNumberRepository phoneNumberRepository;

    @Autowired 
    private SmsSpamRepository smsSpamRepository;
    
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
        long totalReports = reportDao.count();
        
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("activeSessions", activeSessions);
        model.addAttribute("totalReports", totalReports);
        model.addAttribute("page", "admin/dashboard");
        return "admin/layout";
    }
    
    @GetMapping("/numbers")
    public String listNumbers(Model model, HttpSession session) {
        if (session.getAttribute("admin_logged") == null) {
            return "redirect:/admin/login";
        }
        
        Iterable<User> allUsers = userDao.findAll();
        model.addAttribute("users", allUsers);
        model.addAttribute("page", "admin/numbers");
        return "admin/layout";
    }
    
    @GetMapping("/reports")
    public String listReports(Model model, HttpSession session) {
        if (session.getAttribute("admin_logged") == null) {
            return "redirect:/admin/login";
        }
        
        List<Report> allReports = reportDao.findAll();
        model.addAttribute("reports", allReports);
        model.addAttribute("page", "admin/reports");
        return "admin/layout";
    }

    @GetMapping("/sms-reports")
    public String listSmsReports(Model model, HttpSession session) {
        if (session.getAttribute("admin_logged") == null) return "redirect:/admin/login";
        
        model.addAttribute("smsReports", smsSpamRepository.findAll());
        model.addAttribute("page", "admin/sms-reports"); // Necesitarás crear este archivo .html en tus plantillas
        return "admin/layout";
    }
    @PostMapping("/reports/{id}/toggle-spam")
    public String toggleSpam(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("admin_logged") == null) {
            return "redirect:/admin/login";
        }
        
        try {
            Optional<Report> reportOpt = reportDao.findById(id);
            if (reportOpt.isPresent()) {
                Report report = reportOpt.get();
                boolean newStatus = !report.isSpammer();
                report.setSpammer(newStatus);
                reportDao.save(report);
            }
        } catch (Exception e) {
            System.out.println("Error toggling spam: " + e.getMessage());
        }
        
        return "redirect:/admin/reports";
    }
    // 2. NUEVO: Marcar/Desmarcar Spam en Números
    @PostMapping("/numbers/{id}/toggle-spam")
    public String toggleNumberSpam(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("admin_logged") == null) return "redirect:/admin/login";
        
        phoneNumberRepository.findById(id).ifPresent(n -> {
            n.setSpam(!n.isSpam());
            phoneNumberRepository.save(n);
        });
        return "redirect:/admin/numbers";
    }
    // 3. NUEVO: Aprobar/Rechazar SMS
    @PostMapping("/sms-reports/{id}/status")
    public String updateSmsStatus(@PathVariable Long id, @RequestParam String status, HttpSession session) {
        if (session.getAttribute("admin_logged") == null) return "redirect:/admin/login";
        
        smsSpamRepository.findById(id).ifPresent(s -> {
            s.setStatus(status);
            smsSpamRepository.save(s);
        });
        return "redirect:/admin/sms-reports";
    }
    // ========== NUEVO ENDPOINT PARA ASIGNAR UUID FIJO ==========
    @GetMapping("/fix-uuid/{phoneNumber}")
    @ResponseBody
    public String fixUuid(@PathVariable String phoneNumber) {
        try {
            User user = userDao.findByphoneNumber(phoneNumber);
            if (user != null) {
                if (user.getUuid() == null || user.getUuid().isEmpty()) {
                    String newUuid = java.util.UUID.randomUUID().toString().substring(0, 8);
                    user.setUuid(newUuid);
                    userDao.save(user);
                    return "✅ UUID asignado: " + newUuid + " para el número " + phoneNumber;
                } else {
                    return "ℹ️ El usuario ya tiene UUID: " + user.getUuid();
                }
            }
            return "❌ Usuario no encontrado con número: " + phoneNumber;
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }
    // ========== FIN DEL ENDPOINT ==========
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }
}
