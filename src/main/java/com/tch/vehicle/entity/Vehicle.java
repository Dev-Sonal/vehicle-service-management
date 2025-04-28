package com.tch.vehicle.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "vehicles")
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String make;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private String registrationNumber;

    @Column(nullable = false)
    private String chassisNumber;

    @Column(nullable = false)
    private LocalDate purchaseDate;

    @Column(nullable = false)
    private LocalDate lastServiceDate;

    @Column(nullable = false)
    private LocalDate nextServiceDate;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "service_advisor_id")
    private User serviceAdvisor;

    private String color;
    private String engineNumber;
    private String fuelType;
    private String transmissionType;

    @Override
    public String toString() {
        return "Vehicle{" +
                "id=" + id +
                ", make='" + make + '\'' +
                ", model='" + model + '\'' +
                ", registrationNumber='" + registrationNumber + '\'' +
                ", chassisNumber='" + chassisNumber + '\'' +
                ", customerId=" + (customer != null ? customer.getId() : null) +
                ", serviceAdvisorId=" + (serviceAdvisor != null ? serviceAdvisor.getId() : null) +
                '}';
    }
} 