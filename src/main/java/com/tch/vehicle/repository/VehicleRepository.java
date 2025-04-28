package com.tch.vehicle.repository;

import com.tch.vehicle.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByNextServiceDateBetween(LocalDate startDate, LocalDate endDate);
    List<Vehicle> findByCustomerId(Long customerId);
    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);
    
    @Query("SELECT v FROM Vehicle v WHERE v.nextServiceDate <= :date AND v.id NOT IN " +
           "(SELECT sr.vehicle.id FROM ServiceRecord sr WHERE CAST(sr.serviceDate AS LocalDate) = :date AND sr.status = 'SCHEDULED')")
    List<Vehicle> findVehiclesDueForService(LocalDate date);
} 