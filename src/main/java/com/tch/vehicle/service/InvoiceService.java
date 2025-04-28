package com.tch.vehicle.service;

import com.tch.vehicle.dto.InvoiceDTO;
import org.springframework.core.io.Resource;
import java.util.Optional;

public interface InvoiceService {
    InvoiceDTO generateInvoice(Long serviceRecordId);
    InvoiceDTO getInvoice(Long invoiceId);
    Optional<InvoiceDTO> findByServiceRecordId(Long serviceRecordId);
    Resource generatePdfInvoice(Long invoiceId);
    void markAsPaid(Long invoiceId);
} 