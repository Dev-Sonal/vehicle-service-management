package com.tch.vehicle.controller;

import com.tch.vehicle.dto.InvoiceDTO;
import com.tch.vehicle.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @PostMapping("/generate/{serviceRecordId}")
    @PreAuthorize("hasAnyRole('SERVICE_ADVISOR', 'ADMIN')")
    public String generateInvoice(@PathVariable Long serviceRecordId, RedirectAttributes redirectAttributes) {
        try {
            InvoiceDTO invoice = invoiceService.generateInvoice(serviceRecordId);
            redirectAttributes.addFlashAttribute("success", "Invoice generated successfully");
            return "redirect:/invoices/" + invoice.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to generate invoice: " + e.getMessage());
            return "redirect:/service-records/" + serviceRecordId;
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SERVICE_ADVISOR', 'ADMIN')")
    public String viewInvoice(@PathVariable Long id, Model model) {
        InvoiceDTO invoice = invoiceService.getInvoice(id);
        model.addAttribute("invoice", invoice);
        return "invoices/view";
    }

    @PostMapping("/{id}/mark-paid")
    @PreAuthorize("hasAnyRole('SERVICE_ADVISOR', 'ADMIN')")
    public String markAsPaid(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            invoiceService.markAsPaid(id);
            redirectAttributes.addFlashAttribute("success", "Invoice marked as paid");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to mark invoice as paid: " + e.getMessage());
        }
        return "redirect:/invoices/" + id;
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('SERVICE_ADVISOR', 'ADMIN')")
    public ResponseEntity<Resource> downloadInvoice(@PathVariable Long id) {
        InvoiceDTO invoice = invoiceService.getInvoice(id);
        Resource pdfResource = invoiceService.generatePdfInvoice(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"invoice-" + invoice.getInvoiceNumber() + ".pdf\"")
                .body(pdfResource);
    }
} 