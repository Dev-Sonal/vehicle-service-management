package com.tch.vehicle.service;

import com.tch.vehicle.entity.ServiceRecord;
import com.tch.vehicle.entity.ServiceStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ServiceRecordService {
    List<ServiceRecord> getAllServiceRecords();
    Optional<ServiceRecord> getServiceRecordById(Long id);
    ServiceRecord createServiceRecord(ServiceRecord serviceRecord);
    ServiceRecord updateServiceRecord(ServiceRecord serviceRecord);
    void deleteServiceRecord(Long id);
    List<ServiceRecord> getServiceRecordsByStatus(ServiceStatus status);
    List<ServiceRecord> getServiceRecordsByDate(LocalDate date);
    List<ServiceRecord> getServiceRecordsByVehicleRegistration(String registrationNumber);
    List<ServiceRecord> getRecentServiceRecords();
    long getServiceCountByStatus(ServiceStatus status);
    List<ServiceRecord> getRecentCompletedServices();
} 