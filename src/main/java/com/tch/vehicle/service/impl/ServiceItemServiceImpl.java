package com.tch.vehicle.service.impl;

import com.tch.vehicle.entity.ServiceItem;
import com.tch.vehicle.repository.ServiceItemRepository;
import com.tch.vehicle.service.ServiceItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceItemServiceImpl implements ServiceItemService {
    
    private static final Logger logger = LoggerFactory.getLogger(ServiceItemServiceImpl.class);

    @Autowired
    private ServiceItemRepository serviceItemRepository;

    @Override
    public List<ServiceItem> getServiceItemsByRecordId(Long serviceRecordId) {
        logger.debug("Fetching service items for record ID: {}", serviceRecordId);
        return serviceItemRepository.findByServiceRecordId(serviceRecordId);
    }

    @Override
    public Optional<ServiceItem> getServiceItemById(Long id) {
        logger.debug("Fetching service item by ID: {}", id);
        return serviceItemRepository.findById(id);
    }

    @Override
    @Transactional
    public ServiceItem createServiceItem(ServiceItem serviceItem) {
        if (serviceItem == null) {
            throw new IllegalArgumentException("Service item cannot be null");
        }
        if (serviceItem.getServiceRecord() == null) {
            throw new IllegalArgumentException("Service record must be set");
        }
        
        logger.debug("Creating new service item: {}", serviceItem);
        
        // Ensure total price is calculated
        if (serviceItem.getQuantity() != null && serviceItem.getUnitPrice() != null) {
            double totalPrice = serviceItem.getQuantity() * serviceItem.getUnitPrice();
            serviceItem.setTotalPrice(totalPrice);
            logger.debug("Calculated total price: {}", totalPrice);
        }

        try {
            ServiceItem savedItem = serviceItemRepository.save(serviceItem);
            logger.info("Successfully created service item with ID: {}", savedItem.getId());
            return savedItem;
        } catch (Exception e) {
            logger.error("Error creating service item: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create service item: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public ServiceItem updateServiceItem(ServiceItem serviceItem) {
        if (!serviceItemRepository.existsById(serviceItem.getId())) {
            logger.error("Service item not found with id: {}", serviceItem.getId());
            throw new IllegalArgumentException("Service item not found with id: " + serviceItem.getId());
        }
        
        logger.debug("Updating service item: {}", serviceItem);
        
        try {
            ServiceItem updatedItem = serviceItemRepository.save(serviceItem);
            logger.info("Successfully updated service item with ID: {}", updatedItem.getId());
            return updatedItem;
        } catch (Exception e) {
            logger.error("Error updating service item: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update service item: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteServiceItem(Long id) {
        logger.debug("Deleting service item with ID: {}", id);
        try {
            serviceItemRepository.deleteById(id);
            logger.info("Successfully deleted service item with ID: {}", id);
        } catch (Exception e) {
            logger.error("Error deleting service item: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete service item: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteServiceItemsByRecordId(Long serviceRecordId) {
        logger.debug("Deleting all service items for record ID: {}", serviceRecordId);
        try {
            serviceItemRepository.deleteByServiceRecordId(serviceRecordId);
            logger.info("Successfully deleted all service items for record ID: {}", serviceRecordId);
        } catch (Exception e) {
            logger.error("Error deleting service items for record: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete service items: " + e.getMessage(), e);
        }
    }
} 