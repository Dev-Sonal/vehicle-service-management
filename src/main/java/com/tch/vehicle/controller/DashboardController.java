package com.tch.vehicle.controller;

import com.tch.vehicle.entity.Vehicle;
import com.tch.vehicle.entity.ServiceRecord;
import com.tch.vehicle.entity.ServiceStatus;
import com.tch.vehicle.service.VehicleService;
import com.tch.vehicle.service.ServiceRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private ServiceRecordService serviceRecordService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication, HttpSession session) {
        // Check if welcome message should be shown
        Boolean showWelcome = (Boolean) session.getAttribute("welcome");
        if (showWelcome != null && showWelcome) {
            model.addAttribute("welcome", true);
            // Remove the welcome flag from session after showing it
            session.removeAttribute("welcome");
        }
        
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return adminDashboard(model);
        } else {
            return advisorDashboard(model);
        }
    }

    private String adminDashboard(Model model) {
        // Get vehicles due for service
        List<Vehicle> dueVehicles = vehicleService.getVehiclesDueForService(LocalDate.now());
        model.addAttribute("dueVehicles", dueVehicles);

        // Get recent service records
        List<ServiceRecord> recentServices = serviceRecordService.getRecentServiceRecords();
        model.addAttribute("recentServices", recentServices);

        // Get service statistics
        long totalVehicles = vehicleService.getTotalVehicles();
        long vehiclesDueCount = dueVehicles.size();
        long servicesInProgress = serviceRecordService.getServiceCountByStatus(ServiceStatus.IN_PROGRESS);
        long completedServices = serviceRecordService.getServiceCountByStatus(ServiceStatus.COMPLETED);

        model.addAttribute("totalVehicles", totalVehicles);
        model.addAttribute("vehiclesDueCount", vehiclesDueCount);
        model.addAttribute("servicesInProgress", servicesInProgress);
        model.addAttribute("completedServices", completedServices);

        return "dashboard/admin";
    }

    private String advisorDashboard(Model model) {
        // Get today's scheduled services
        List<ServiceRecord> todayServices = serviceRecordService.getServiceRecordsByDate(LocalDate.now());
        model.addAttribute("todayServices", todayServices);

        // Get services in progress
        List<ServiceRecord> inProgressServices = serviceRecordService.getServiceRecordsByStatus(ServiceStatus.IN_PROGRESS);
        model.addAttribute("inProgressServices", inProgressServices);

        // Get recently completed services
        List<ServiceRecord> completedServices = serviceRecordService.getRecentCompletedServices();
        model.addAttribute("completedServices", completedServices);

        return "dashboard/advisor";
    }
} 