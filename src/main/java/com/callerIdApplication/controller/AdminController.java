package com.callerIdApplication.controller;

import com.callerIdApplication.entity.Report;
import com.callerIdApplication.entity.User;
import com.callerIdApplication.entity.SearchHistory;
import com.callerIdApplication.entity.SmsReport;
import com.callerIdApplication.entity.Spam;
import com.callerIdApplication.repostitory.UserDao;
import com.callerIdApplication.repostitory.SessionDao;
import com.callerIdApplication.repostitory.ReportDao;
import com.callerIdApplication.repostitory.HistoryDao;
import com.callerIdApplication.repostitory.SmsDao;
import com.callerIdApplication.repostitory.SpamDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserDao userDao;
    
    @Autowired
    private SessionDao sessionDao;
    
    @Autowired
    private ReportDao reportDao;

    @Autowired
    private HistoryDao historyDao; 

    @Autowired
    private SmsDao smsDao; 

    @Autowired
    private SpamDao spamDao; 

    private static final String ADMIN_PASSWORD = "admin123";

    private boolean isAdmin(HttpSession session) {
        return session.getAttribute("admin_logged") != null;
    }

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
        if (!isAdmin(session)) return "redirect:/admin/login";

        model.addAttribute("totalUsers", userDao.count());
        model.addAttribute("activeSessions", sessionDao.count());
        model.addAttribute("totalReports", reportDao.count());
        model.addAttribute("totalSmsSpam", smsDao.count());

        // Feed de actividad reciente ordenado por fecha descendente
        List<SearchHistory> recentActivity = historyDao.findAllByOrderBySearchDateDesc();
        model.addAttribute("recentHistory", recentActivity.stream()
                .limit(10)
                .collect(Collectors.toList()));

        model.addAttribute("page", "admin/dashboard");
        return "admin/layout";
    }

    @GetMapping("/numbers")
    public String listNumbers(Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        model.addAttribute("users", userDao.findAll());
        model.addAttribute("page", "admin/numbers");
        return "admin/layout";
    }

    // 🛡️ VERIFICAR USUARIO (Escudo Velo)
    @PostMapping("/user/{id}/verify")
    public String verifyUser(@PathVariable Integer id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        
        Optional<User> userOpt = userDao.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getUserName() != null && !user.getUserName().contains("🛡️")) {
                user.setUserName("🛡️ " + user.getUserName());
                userDao.save(user);
            }
        }
        return "redirect:/admin/numbers";
    }

    // 🚨 BANEAR / DESBANEAR GLOBAL (Lógica de Interruptor)
    @PostMapping("/user/{id}/toggle-spam")
    public String toggleUserSpam(@PathVariable Integer id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";

        Optional<User> userOpt = userDao.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String phone = user.getPhoneNumber();
            
            List<Spam> spamList = spamDao.findBynumber(phone);
            Spam spamRecord = (spamList != null && !spamList.isEmpty()) ? spamList.get(0) : null;

            if (spamRecord != null && spamRecord.getSpammer()) {
                // 🟢 ACCIÓN: DESBANEAR
                spamRecord.setSpammer(false);
                spamRecord.setName("USUARIO RECUPERADO");
                spamDao.save(spamRecord);
                
                if (user.getUserName().contains("🚨 SPAMMER: ")) {
                    user.setUserName(user.getUserName().replace("🚨 SPAMMER: ", ""));
                }
            } else {
                // 🔴 ACCIÓN: BANEAR
                if (spamRecord == null) {
                    spamRecord = new Spam();
                    spamRecord.setNumber(phone);
                }
                spamRecord.setName("SPAMMER REPORTADO");
                spamRecord.setSpammer(true);
                spamDao.save(spamRecord);
                
                if (!user.getUserName().contains("🚨 SPAMMER: ")) {
                    user.setUserName("🚨 SPAMMER: " + user.getUserName());
                }
            }
            userDao.save(user);
        }
        return "redirect:/admin/numbers";
    }

    @GetMapping("/reports")
    public String listReports(Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        model.addAttribute("reports", reportDao.findAll());
        model.addAttribute("page", "admin/reports");
        return "admin/layout";
    }

    // 🔒 MODERACIÓN DE REPORTES: Sincronizar con tabla SPAM
    @PostMapping("/reports/{id}/toggle-spam")
    public String toggleReportSpam(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        try {
            Optional<Report> reportOpt = reportDao.findById(id);
            if (reportOpt.isPresent()) {
                Report report = reportOpt.get();
                boolean newStatus = !report.isSpammer();
                report.setSpammer(newStatus);
                reportDao.save(report);

                // Sincronizar con la tabla SPAM para que la app lo detecte globalmente
                List<Spam> spamList = spamDao.findBynumber(report.getPhoneNumber());
                Spam spam = (spamList != null && !spamList.isEmpty()) ? spamList.get(0) : new Spam();
                spam.setNumber(report.getPhoneNumber());
                spam.setSpammer(newStatus);
                spam.setName(newStatus ? "SPAM: " + report.getCategory() : "SEGURO");
                spamDao.save(spam);
            }
        } catch (Exception e) {
            System.out.println("Error en moderación de reporte: " + e.getMessage());
        }
        return "redirect:/admin/reports";
    }

    @GetMapping("/sms-reports")
    public String listSmsReports(Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        model.addAttribute("smsReports", smsDao.findAll());
        model.addAttribute("page", "admin/sms-reports");
        return "admin/layout";
    }

    // 🛡️ MARCAR REMITENTE DE SMS COMO SPAMMER GLOBAL
    @PostMapping("/sms-reports/{id}/mark-global-spam")
    public String markSmsSenderAsSpam(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        
        Optional<SmsReport> smsOpt = smsDao.findById(id);
        if (smsOpt.isPresent()) {
            SmsReport sms = smsOpt.get();
            List<Spam> spamList = spamDao.findBynumber(sms.getPhoneNumber());
            Spam spam = (spamList != null && !spamList.isEmpty()) ? spamList.get(0) : new Spam();
            
            spam.setNumber(sms.getPhoneNumber());
            spam.setSpammer(true);
            spam.setName("SPAM SMS: " + sms.getCategory());
            spamDao.save(spam);
        }
        return "redirect:/admin/sms-reports";
    }

    @PostMapping("/sms-reports/{id}/delete")
    public String deleteSmsReport(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        smsDao.deleteById(id);
        return "redirect:/admin/sms-reports";
    }

    @GetMapping("/fix-uuid/{phoneNumber}")
    @ResponseBody
    public String fixUuid(@PathVariable String phoneNumber) {
        try {
            User user = userDao.findByphoneNumber(phoneNumber);
            if (user != null) {
                if (user.getUuid() == nullEste es el código completo y definitivo para tu **`AdminController.java`**. He consolidado todas las funciones de **Velo** (Dashboard, Moderación de Usuarios, Gestión de Spam y Control de SMS) asegurando que los tipos de datos sean compatibles con tus DAOs para evitar errores de compilación en Render.

### `AdminController.java` (Versión Final de Moderación Total)
