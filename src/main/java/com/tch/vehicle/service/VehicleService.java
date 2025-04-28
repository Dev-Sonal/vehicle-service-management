package com.tch.vehicle.service;

import com.tch.vehicle.entity.Vehicle;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VehicleService {
    Vehicle createVehicle(Vehicle vehicle);
    Vehicle updateVehicle(Vehicle vehicle);
    void deleteVehicle(Long id);
    Optional<Vehicle> getVehicleById(Long id);
    List<Vehicle> getAllVehicles();
    List<Vehicle> getVehiclesByCustomerId(Long customerId);
    List<Vehicle> getVehiclesDueForService(LocalDate date);
    List<Vehicle> getVehiclesByNextServiceDateBetween(LocalDate startDate, LocalDate endDate);
    Optional<Vehicle> getVehicleByRegistrationNumber(String registrationNumber);
    
    // New method for dashboard
    long getTotalVehicles();
} 