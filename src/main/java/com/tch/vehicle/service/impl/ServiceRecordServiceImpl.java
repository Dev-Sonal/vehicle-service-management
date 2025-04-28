package com.tch.vehicle.service.impl;

import com.tch.vehicle.entity.ServiceRecord;
import com.tch.vehicle.entity.ServiceStatus;
import com.tch.vehicle.repository.ServiceRecordRepository;
import com.tch.vehicle.service.ServiceRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ServiceRecordServiceImpl implements ServiceRecordService {

    @Autowired
    private ServiceRecordRepository serviceRecordRepository;

    @Override
    public List<ServiceRecord> getAllServiceRecords() {
        return serviceRecordRepository.findAll();
    }

    @Override
    public Optional<ServiceRecord> getServiceRecordById(Long id) {
        return serviceRecordRepository.findById(id);
    }

    @Override
    public ServiceRecord createServiceRecord(ServiceRecord serviceRecord) {
        return serviceRecordRepository.save(serviceRecord);
    }

    @Override
    public ServiceRecord updateServiceRecord(ServiceRecord serviceRecord) {
        return serviceRecordRepository.save(serviceRecord);
    }

    @Override
    public void deleteServiceRecord(Long id) {
        serviceRecordRepository.deleteById(id);
    }

    @Override
    public List<ServiceRecord> getServiceRecordsByStatus(ServiceStatus status) {
        return serviceRecordRepository.findByStatus(status);
    }

    @Override
    public List<ServiceRecord> getServiceRecordsByDate(LocalDate date) {
        return serviceRecordRepository.findByServiceDate(date);
    }

    @Override
    public List<ServiceRecord> getServiceRecordsByVehicleRegistration(String registrationNumber) {
        return serviceRecordRepository.findByVehicleRegistrationNumberContainingIgnoreCase(registrationNumber);
    }

    @Override
    public List<ServiceRecord> getRecentServiceRecords() {
        return serviceRecordRepository.findTop5ByOrderByServiceDateDesc();
    }

    @Override
    public long getServiceCountByStatus(ServiceStatus status) {
        return serviceRecordRepository.countByStatus(status);
    }

    @Override
    public List<ServiceRecord> getRecentCompletedServices() {
        return serviceRecordRepository.findTop5ByStatusOrderByCompletionDateDesc(ServiceStatus.COMPLETED);
    }
} 