package com.tch.vehicle.service.impl;

import com.tch.vehicle.entity.Vehicle;
import com.tch.vehicle.repository.VehicleRepository;
import com.tch.vehicle.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class VehicleServiceImpl implements VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Override
    public Vehicle createVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    @Override
    public Vehicle updateVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    @Override
    public void deleteVehicle(Long id) {
        vehicleRepository.deleteById(id);
    }

    @Override
    public Optional<Vehicle> getVehicleById(Long id) {
        return vehicleRepository.findById(id);
    }

    @Override
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    @Override
    public List<Vehicle> getVehiclesByCustomerId(Long customerId) {
        return vehicleRepository.findByCustomerId(customerId);
    }

    @Override
    public List<Vehicle> getVehiclesDueForService(LocalDate date) {
        return vehicleRepository.findVehiclesDueForService(date);
    }

    @Override
    public List<Vehicle> getVehiclesByNextServiceDateBetween(LocalDate startDate, LocalDate endDate) {
        return vehicleRepository.findByNextServiceDateBetween(startDate, endDate);
    }

    @Override
    public Optional<Vehicle> getVehicleByRegistrationNumber(String registrationNumber) {
        return vehicleRepository.findByRegistrationNumber(registrationNumber);
    }

    @Override
    public long getTotalVehicles() {
        return vehicleRepository.count();
    }
} 