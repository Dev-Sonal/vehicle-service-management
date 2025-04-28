package com.tch.vehicle.controller;

import com.tch.vehicle.entity.ServiceItem;
import com.tch.vehicle.entity.ServiceRecord;
import com.tch.vehicle.service.ServiceItemService;
import com.tch.vehicle.service.ServiceRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/service-items")
@PreAuthorize("hasRole('SERVICE_ADVISOR')")
public class ServiceItemController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceItemController.class);

    @Autowired
    private ServiceItemService serviceItemService;

    @Autowired
    private ServiceRecordService serviceRecordService;

    @GetMapping("/record/{recordId}")
    public String listServiceItems(@PathVariable Long recordId, Model model) {
        Optional<ServiceRecord> serviceRecordOpt = serviceRecordService.getServiceRecordById(recordId);
        if (serviceRecordOpt.isPresent()) {
            ServiceRecord serviceRecord = serviceRecordOpt.get();
            model.addAttribute("serviceRecord", serviceRecord);
            List<ServiceItem> serviceItems = serviceItemService.getServiceItemsByRecordId(recordId);
            model.addAttribute("serviceItems", serviceItems);
            if (!model.containsAttribute("newServiceItem")) {
                model.addAttribute("newServiceItem", new ServiceItem());
            }
            return "service-items/list";
        }
        return "redirect:/service-records";
    }

    @PostMapping("/create")
    public String createServiceItem(@Valid ServiceItem serviceItem, 
                                  BindingResult result,
                                  @RequestParam Long serviceRecordId,
                                  RedirectAttributes redirectAttributes) {
        logger.debug("Creating service item for service record ID: {}", serviceRecordId);

        if (result.hasErrors()) {
            logger.warn("Validation errors occurred: {}", result.getAllErrors());
            redirectAttributes.addFlashAttribute("error", "Please correct the errors in the form.");
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.newServiceItem", result);
            redirectAttributes.addFlashAttribute("newServiceItem", serviceItem);
            return "redirect:/service-items/record/" + serviceRecordId;
        }

        try {
            Optional<ServiceRecord> serviceRecordOpt = serviceRecordService.getServiceRecordById(serviceRecordId);
            if (!serviceRecordOpt.isPresent()) {
                logger.error("Service record not found with ID: {}", serviceRecordId);
                redirectAttributes.addFlashAttribute("error", "Service record not found.");
                return "redirect:/service-records";
            }

            ServiceRecord serviceRecord = serviceRecordOpt.get();
            serviceItem.setServiceRecord(serviceRecord);

            // Calculate total price
            if (serviceItem.getQuantity() != null && serviceItem.getUnitPrice() != null) {
                double totalPrice = serviceItem.getQuantity() * serviceItem.getUnitPrice();
                serviceItem.setTotalPrice(totalPrice);
            }

            ServiceItem savedItem = serviceItemService.createServiceItem(serviceItem);
            logger.info("Successfully created service item with ID: {}", savedItem.getId());

            // Update service record total cost
            double newTotalCost = serviceRecord.getTotalCost() + serviceItem.getTotalPrice();
            serviceRecord.setTotalCost(newTotalCost);
            serviceRecordService.updateServiceRecord(serviceRecord);

            redirectAttributes.addFlashAttribute("success", "Service item added successfully.");
        } catch (Exception e) {
            logger.error("Error creating service item: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to create service item: " + e.getMessage());
        }

        return "redirect:/service-items/record/" + serviceRecordId;
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        logger.debug("Showing edit form for service item ID: {}", id);
        
        Optional<ServiceItem> serviceItemOpt = serviceItemService.getServiceItemById(id);
        if (!serviceItemOpt.isPresent()) {
            logger.error("Service item not found with ID: {}", id);
            redirectAttributes.addFlashAttribute("error", "Service item not found.");
            return "redirect:/service-records";
        }

        model.addAttribute("serviceItem", serviceItemOpt.get());
        return "service-items/edit";
    }

    @PostMapping("/update/{id}")
    public String updateServiceItem(@PathVariable Long id,
                                  @Valid ServiceItem serviceItem,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes) {
        logger.debug("Updating service item ID: {}", id);

        if (result.hasErrors()) {
            logger.warn("Validation errors occurred: {}", result.getAllErrors());
            return "service-items/edit";
        }

        try {
            Optional<ServiceItem> existingItemOpt = serviceItemService.getServiceItemById(id);
            if (!existingItemOpt.isPresent()) {
                logger.error("Service item not found with ID: {}", id);
                redirectAttributes.addFlashAttribute("error", "Service item not found.");
                return "redirect:/service-records";
            }

            ServiceItem existingItem = existingItemOpt.get();
            ServiceRecord serviceRecord = existingItem.getServiceRecord();
            
            // Update the service record's total cost
            double oldTotalPrice = existingItem.getTotalPrice();
            double newTotalPrice = serviceItem.getQuantity() * serviceItem.getUnitPrice();
            double costDifference = newTotalPrice - oldTotalPrice;
            
            serviceRecord.setTotalCost(serviceRecord.getTotalCost() + costDifference);
            serviceRecordService.updateServiceRecord(serviceRecord);

            // Update the service item
            existingItem.setItemName(serviceItem.getItemName());
            existingItem.setDescription(serviceItem.getDescription());
            existingItem.setQuantity(serviceItem.getQuantity());
            existingItem.setUnitPrice(serviceItem.getUnitPrice());
            existingItem.setTotalPrice(newTotalPrice);

            serviceItemService.updateServiceItem(existingItem);
            logger.info("Successfully updated service item with ID: {}", id);
            
            redirectAttributes.addFlashAttribute("success", "Service item updated successfully.");
        } catch (Exception e) {
            logger.error("Error updating service item: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to update service item: " + e.getMessage());
        }

        return "redirect:/service-records/" + serviceItem.getServiceRecord().getId();
    }

    @PostMapping("/delete/{id}")
    public String deleteServiceItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        logger.debug("Deleting service item ID: {}", id);

        try {
            Optional<ServiceItem> serviceItemOpt = serviceItemService.getServiceItemById(id);
            if (!serviceItemOpt.isPresent()) {
                logger.error("Service item not found with ID: {}", id);
                redirectAttributes.addFlashAttribute("error", "Service item not found.");
                return "redirect:/service-records";
            }

            ServiceItem serviceItem = serviceItemOpt.get();
            Long serviceRecordId = serviceItem.getServiceRecord().getId();

            // Update service record total cost
            ServiceRecord serviceRecord = serviceItem.getServiceRecord();
            serviceRecord.setTotalCost(serviceRecord.getTotalCost() - serviceItem.getTotalPrice());
            serviceRecordService.updateServiceRecord(serviceRecord);

            serviceItemService.deleteServiceItem(id);
            logger.info("Successfully deleted service item with ID: {}", id);
            
            redirectAttributes.addFlashAttribute("success", "Service item deleted successfully.");
            return "redirect:/service-records/" + serviceRecordId;
        } catch (Exception e) {
            logger.error("Error deleting service item: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to delete service item: " + e.getMessage());
            return "redirect:/service-records";
        }
    }
} 