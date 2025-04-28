package com.tch.vehicle.controller;

import com.tch.vehicle.entity.ServiceRecord;
import com.tch.vehicle.entity.ServiceStatus;
import com.tch.vehicle.entity.User;
import com.tch.vehicle.entity.ServiceItem;
import com.tch.vehicle.service.ServiceRecordService;
import com.tch.vehicle.service.VehicleService;
import com.tch.vehicle.service.UserService;
import com.tch.vehicle.service.ServiceItemService;
import com.tch.vehicle.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.core.io.Resource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/service-records")
public class ServiceRecordController {

    @Autowired
    private ServiceRecordService serviceRecordService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private UserService userService;

    @Autowired
    private ServiceItemService serviceItemService;

    @Autowired
    private InvoiceService invoiceService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE_ADVISOR')")
    public String listServiceRecords(
            @RequestParam(required = false) ServiceStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {
        List<ServiceRecord> serviceRecords;
        
        if (status != null) {
            serviceRecords = serviceRecordService.getServiceRecordsByStatus(status);
        } else if (date != null) {
            serviceRecords = serviceRecordService.getServiceRecordsByDate(date);
        } else {
            serviceRecords = serviceRecordService.getAllServiceRecords();
        }
        
        model.addAttribute("serviceRecords", serviceRecords);
        return "service-records/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('SERVICE_ADVISOR')")
    public String showCreateForm(Model model) {
        ServiceRecord serviceRecord = new ServiceRecord();
        model.addAttribute("serviceRecord", serviceRecord);
        model.addAttribute("vehicles", vehicleService.getAllVehicles());
        model.addAttribute("statuses", ServiceStatus.values());
        return "service-records/form";
    }

    @PostMapping
    @PreAuthorize("hasRole('SERVICE_ADVISOR')")
    public String createServiceRecord(@ModelAttribute ServiceRecord serviceRecord, 
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            // Set the service advisor
            User serviceAdvisor = userService.findByUsername(authentication.getName());
            serviceRecord.setServiceAdvisor(serviceAdvisor);

            // Set initial status if not set
            if (serviceRecord.getStatus() == null) {
                serviceRecord.setStatus(ServiceStatus.SCHEDULED);
            }

            serviceRecordService.createServiceRecord(serviceRecord);
            redirectAttributes.addFlashAttribute("message", "Service record created successfully!");
            return "redirect:/service-records";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/service-records/new";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('SERVICE_ADVISOR')")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            serviceRecordService.getServiceRecordById(id).ifPresent(serviceRecord -> {
                model.addAttribute("serviceRecord", serviceRecord);
                model.addAttribute("vehicles", vehicleService.getAllVehicles());
                model.addAttribute("statuses", ServiceStatus.values());
            });
            return "service-records/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/service-records";
        }
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('SERVICE_ADVISOR')")
    public String updateServiceRecord(@PathVariable Long id, 
                                    @ModelAttribute ServiceRecord serviceRecord,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            // Get the existing service record
            Optional<ServiceRecord> existingRecordOpt = serviceRecordService.getServiceRecordById(id);
            if (!existingRecordOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Service record not found");
                return "redirect:/service-records";
            }
            
            ServiceRecord existingRecord = existingRecordOpt.get();
            
            // Preserve the service advisor
            serviceRecord.setServiceAdvisor(existingRecord.getServiceAdvisor());
            serviceRecord.setId(id);
            
            serviceRecordService.updateServiceRecord(serviceRecord);
            redirectAttributes.addFlashAttribute("message", "Service record updated successfully!");
            return "redirect:/service-records";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/service-records/" + id + "/edit";
        }
    }

    @GetMapping("/{id}/view")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE_ADVISOR')")
    public String viewServiceRecord(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            serviceRecordService.getServiceRecordById(id).ifPresent(serviceRecord -> {
                model.addAttribute("serviceRecord", serviceRecord);
                // Add service items to the model
                List<ServiceItem> serviceItems = serviceItemService.getServiceItemsByRecordId(id);
                model.addAttribute("serviceItems", serviceItems);
                
                // Add invoice information if available
                invoiceService.findByServiceRecordId(id).ifPresent(invoice -> 
                    model.addAttribute("invoice", invoice));
            });
            return "service-records/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/service-records";
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('SERVICE_ADVISOR')")
    public String deleteServiceRecord(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            serviceRecordService.deleteServiceRecord(id);
            redirectAttributes.addFlashAttribute("message", "Service record deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/service-records";
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE_ADVISOR')")
    public String searchServiceRecords(@RequestParam String registrationNumber, Model model) {
        List<ServiceRecord> serviceRecords = serviceRecordService.getServiceRecordsByVehicleRegistration(registrationNumber);
        model.addAttribute("serviceRecords", serviceRecords);
        return "service-records/list";
    }

    @PostMapping("/{id}/generate-invoice")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE_ADVISOR')")
    public String generateInvoice(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ServiceRecord serviceRecord = serviceRecordService.getServiceRecordById(id)
                .orElseThrow(() -> new RuntimeException("Service record not found"));
            
            if (!serviceRecord.getStatus().equals(ServiceStatus.COMPLETED)) {
                throw new RuntimeException("Cannot generate invoice for incomplete service");
            }
            
            invoiceService.generateInvoice(id);
            redirectAttributes.addFlashAttribute("success", "Invoice generated successfully");
            return "redirect:/service-records/" + id + "/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to generate invoice: " + e.getMessage());
            return "redirect:/service-records/" + id + "/view";
        }
    }

    @GetMapping("/{id}/download-invoice")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE_ADVISOR')")
    public ResponseEntity<Resource> downloadInvoice(@PathVariable Long id) {
        try {
            Resource pdfResource = invoiceService.generatePdfInvoice(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment()
                .filename("invoice-" + id + ".pdf").build());
            
            return new ResponseEntity<>(pdfResource, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
} 