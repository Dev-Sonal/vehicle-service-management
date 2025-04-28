package com.tch.vehicle.service;

import com.tch.vehicle.entity.ServiceItem;
import java.util.List;
import java.util.Optional;

public interface ServiceItemService {
    List<ServiceItem> getServiceItemsByRecordId(Long serviceRecordId);
    Optional<ServiceItem> getServiceItemById(Long id);
    ServiceItem createServiceItem(ServiceItem serviceItem);
    ServiceItem updateServiceItem(ServiceItem serviceItem);
    void deleteServiceItem(Long id);
    void deleteServiceItemsByRecordId(Long serviceRecordId);
} 