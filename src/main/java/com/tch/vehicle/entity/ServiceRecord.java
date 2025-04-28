package com.tch.vehicle.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "service_records")
public class ServiceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "service_advisor_id", nullable = false)
    private User serviceAdvisor;

    @Column(nullable = false)
    private LocalDateTime serviceDate;

    @Column(nullable = false)
    private LocalDateTime completionDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ServiceStatus status;

    private String notes;

    @OneToMany(mappedBy = "serviceRecord", cascade = CascadeType.ALL)
    private List<ServiceItem> serviceItems;

    private double totalCost;
    private boolean paymentProcessed;
} 