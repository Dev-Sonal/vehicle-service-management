package com.tch.vehicle.repository;

import com.tch.vehicle.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    List<Customer> findByFirstNameContainingOrLastNameContainingIgnoreCase(String firstName, String lastName);
    boolean existsByEmail(String email);
    List<Customer> findAllByOrderByCreatedAtDesc();
} 