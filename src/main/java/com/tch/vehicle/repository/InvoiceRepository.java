package com.tch.vehicle.repository;

import com.tch.vehicle.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByServiceRecordId(Long serviceRecordId);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
} 