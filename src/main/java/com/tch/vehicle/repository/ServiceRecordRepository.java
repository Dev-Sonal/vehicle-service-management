package com.tch.vehicle.repository;

import com.tch.vehicle.entity.ServiceRecord;
import com.tch.vehicle.entity.ServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ServiceRecordRepository extends JpaRepository<ServiceRecord, Long> {
    List<ServiceRecord> findByStatus(ServiceStatus status);
    
    @Query("SELECT sr FROM ServiceRecord sr WHERE DATE(sr.serviceDate) = :date")
    List<ServiceRecord> findByServiceDate(LocalDate date);
    
    List<ServiceRecord> findByVehicleRegistrationNumberContainingIgnoreCase(String registrationNumber);
    List<ServiceRecord> findTop5ByOrderByServiceDateDesc();
    long countByStatus(ServiceStatus status);
    List<ServiceRecord> findTop5ByStatusOrderByCompletionDateDesc(ServiceStatus status);
} 