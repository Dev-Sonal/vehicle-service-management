package com.tch.vehicle.controller;

import com.tch.vehicle.entity.Vehicle;
import com.tch.vehicle.service.VehicleService;
import com.tch.vehicle.service.CustomerService;
import com.tch.vehicle.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/vehicles")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE_ADVISOR')")
    public String listVehicles(Model model) {
        List<Vehicle> vehicles = vehicleService.getAllVehicles();
        model.addAttribute("vehicles", vehicles);
        return "vehicles/list";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE_ADVISOR')")
    public String viewVehicle(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Vehicle vehicle = vehicleService.getVehicleById(id)
                    .orElseThrow(() -> new RuntimeException("Vehicle not found"));
            model.addAttribute("vehicle", vehicle);
            return "vehicles/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/vehicles";
        }
    }

    @GetMapping("/due")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE_ADVISOR')")
    public String listVehiclesDueForService(Model model) {
        List<Vehicle> vehicles = vehicleService.getVehiclesDueForService(LocalDate.now());
        model.addAttribute("vehicles", vehicles);
        return "vehicles/due";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateForm(Model model) {
        model.addAttribute("vehicle", new Vehicle());
        model.addAttribute("customers", customerService.getAllCustomers());
        model.addAttribute("serviceAdvisors", userService.getUsersByRole("SERVICE_ADVISOR"));
        return "vehicles/form";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String createVehicle(@Valid @ModelAttribute Vehicle vehicle,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("customers", customerService.getAllCustomers());
            model.addAttribute("serviceAdvisors", userService.getUsersByRole("SERVICE_ADVISOR"));
            return "vehicles/form";
        }

        try {
            vehicleService.createVehicle(vehicle);
            redirectAttributes.addFlashAttribute("message", "Vehicle created successfully!");
            return "redirect:/vehicles";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/vehicles/new";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Vehicle vehicle = vehicleService.getVehicleById(id)
                    .orElseThrow(() -> new RuntimeException("Vehicle not found"));
            model.addAttribute("vehicle", vehicle);
            model.addAttribute("customers", customerService.getAllCustomers());
            model.addAttribute("serviceAdvisors", userService.getUsersByRole("SERVICE_ADVISOR"));
            return "vehicles/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/vehicles";
        }
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateVehicle(@PathVariable Long id,
                              @Valid @ModelAttribute Vehicle vehicle,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("customers", customerService.getAllCustomers());
            return "vehicles/form";
        }

        try {
            vehicle.setId(id);
            vehicleService.updateVehicle(vehicle);
            redirectAttributes.addFlashAttribute("message", "Vehicle updated successfully!");
            return "redirect:/vehicles";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/vehicles/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteVehicle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            vehicleService.deleteVehicle(id);
            redirectAttributes.addFlashAttribute("message", "Vehicle deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vehicles";
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE_ADVISOR')")
    public String searchVehicles(@RequestParam String registrationNumber, Model model) {
        vehicleService.getVehicleByRegistrationNumber(registrationNumber)
                .ifPresent(vehicle -> model.addAttribute("vehicles", List.of(vehicle)));
        return "vehicles/list";
    }
} 