package com.tch.vehicle.service.impl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.tch.vehicle.dto.InvoiceDTO;
import com.tch.vehicle.dto.ServiceItemDTO;
import com.tch.vehicle.entity.Invoice;
import com.tch.vehicle.entity.ServiceRecord;
import com.tch.vehicle.repository.InvoiceRepository;
import com.tch.vehicle.repository.ServiceRecordRepository;
import com.tch.vehicle.service.InvoiceService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ServiceRecordRepository serviceRecordRepository;

    @Override
    @Transactional
    public InvoiceDTO generateInvoice(Long serviceRecordId) {
        ServiceRecord serviceRecord = serviceRecordRepository.findById(serviceRecordId)
                .orElseThrow(() -> new EntityNotFoundException("Service Record not found"));

        // Check if invoice already exists
        Optional<Invoice> existingInvoice = invoiceRepository.findByServiceRecordId(serviceRecordId);
        if (existingInvoice.isPresent()) {
            return convertToDTO(existingInvoice.get());
        }

        Invoice invoice = new Invoice();
        invoice.setServiceRecord(serviceRecord);
        invoice.setInvoiceNumber(generateInvoiceNumber(serviceRecord));
        invoice.setGeneratedDate(LocalDateTime.now());
        
        // Calculate service items total
        BigDecimal serviceItemsTotal = serviceRecord.getServiceItems().stream()
                .map(item -> BigDecimal.valueOf(item.getTotalPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate service fee (total cost minus items total)
        BigDecimal serviceFee = BigDecimal.valueOf(serviceRecord.getTotalCost()).subtract(serviceItemsTotal);
        
        // Total amount is service fee plus items total
        BigDecimal totalAmount = serviceFee.add(serviceItemsTotal);

        invoice.setSubtotal(totalAmount);
        invoice.setTaxAmount(BigDecimal.ZERO);
        invoice.setTotalAmount(totalAmount);
        invoice.setPaid(false);

        Invoice savedInvoice = invoiceRepository.save(invoice);
        return convertToDTO(savedInvoice);
    }

    @Override
    public InvoiceDTO getInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found"));
        return convertToDTO(invoice);
    }

    @Override
    public Optional<InvoiceDTO> findByServiceRecordId(Long serviceRecordId) {
        return invoiceRepository.findByServiceRecordId(serviceRecordId)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public void markAsPaid(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found"));
        invoice.setPaid(true);
        invoiceRepository.save(invoice);
    }

    @Override
    public Resource generatePdfInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found"));

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 90, 36);
            PdfWriter.getInstance(document, baos);

            document.open();
            addMetaData(document);
            addHeader(document, invoice);
            addInvoiceDetails(document, invoice);
            addServiceDetails(document, invoice);
            addTotals(document, invoice);
            addFooter(document);
            document.close();

            byte[] pdfBytes = baos.toByteArray();
            return new ByteArrayResource(pdfBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private void addMetaData(Document document) {
        document.addTitle("Service Invoice");
        document.addAuthor("Vehicle Service Management");
        document.addCreator("Vehicle Service Management System");
    }

    private void addHeader(Document document, Invoice invoice) throws DocumentException {
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Paragraph header = new Paragraph("Vehicle Service Invoice", headerFont);
        header.setAlignment(Element.ALIGN_CENTER);
        document.add(header);
        document.add(Chunk.NEWLINE);
    }

    private void addInvoiceDetails(Document document, Invoice invoice) throws DocumentException {
        ServiceRecord service = invoice.getServiceRecord();
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        addTableRow(table, "Invoice Number:", invoice.getInvoiceNumber());
        addTableRow(table, "Date:", invoice.getGeneratedDate().format(DATE_FORMATTER));
        addTableRow(table, "Vehicle:", service.getVehicle().getMake() + " " + service.getVehicle().getModel());
        addTableRow(table, "Registration:", service.getVehicle().getRegistrationNumber());
        addTableRow(table, "Customer:", service.getVehicle().getCustomer().getFirstName() + " " + 
                           service.getVehicle().getCustomer().getLastName());
        addTableRow(table, "Phone:", service.getVehicle().getCustomer().getPhoneNumber());

        document.add(table);
        document.add(Chunk.NEWLINE);
    }

    private void addServiceDetails(Document document, Invoice invoice) throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 6, 2, 2, 2});

        // Add table header
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        table.addCell(new PdfPCell(new Phrase("Item", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Description", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Quantity", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Unit Price", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Total", headerFont)));

        // Calculate service items total first
        ServiceRecord service = invoice.getServiceRecord();
        BigDecimal itemsTotal = service.getServiceItems().stream()
                .map(item -> BigDecimal.valueOf(item.getTotalPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate service fee (total cost minus items total)
        BigDecimal serviceFee = BigDecimal.valueOf(service.getTotalCost()).subtract(itemsTotal);

        // Add service fee as first item
        table.addCell("Service Fee");
        table.addCell("Basic Service Charge");
        table.addCell("1");
        table.addCell("₹" + serviceFee);
        table.addCell("₹" + serviceFee);

        // Add service items
        for (var item : service.getServiceItems()) {
            table.addCell(item.getItemName());
            table.addCell(item.getDescription() != null ? item.getDescription() : "-");
            table.addCell(String.valueOf(item.getQuantity()));
            table.addCell("₹" + item.getUnitPrice());
            table.addCell("₹" + item.getTotalPrice());
        }

        document.add(table);
        document.add(Chunk.NEWLINE);
    }

    private void addTotals(Document document, Invoice invoice) throws DocumentException {
        ServiceRecord service = invoice.getServiceRecord();
        
        // Calculate service items total
        BigDecimal itemsTotal = service.getServiceItems().stream()
                .map(item -> BigDecimal.valueOf(item.getTotalPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate service fee
        BigDecimal serviceFee = BigDecimal.valueOf(service.getTotalCost()).subtract(itemsTotal);
        
        // Calculate total amount
        BigDecimal totalAmount = serviceFee.add(itemsTotal);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(40);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setSpacingBefore(10f);

        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        
        // Add total row with double border
        PdfPCell labelCell = new PdfPCell(new Phrase("Total Amount:", boldFont));
        labelCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
        labelCell.setBorderWidthTop(0.5f);
        labelCell.setBorderWidthBottom(2f);
        labelCell.setPaddingTop(5f);
        labelCell.setPaddingBottom(5f);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase("₹" + totalAmount, boldFont));
        valueCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
        valueCell.setBorderWidthTop(0.5f);
        valueCell.setBorderWidthBottom(2f);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPaddingTop(5f);
        valueCell.setPaddingBottom(5f);
        table.addCell(valueCell);

        document.add(table);
    }

    private void addFooter(Document document) throws DocumentException {
        Paragraph footer = new Paragraph();
        footer.add(Chunk.NEWLINE);
        footer.add(new Chunk("Thank you for your business!", new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC)));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    private void addTableRow(PdfPTable table, String label, String value) {
        table.addCell(new Phrase(label, new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
        table.addCell(new Phrase(value, new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL)));
    }

    private String generateInvoiceNumber(ServiceRecord serviceRecord) {
        return String.format("INV-%d-%d", serviceRecord.getId(), 
                System.currentTimeMillis() % 100000);
    }

    private InvoiceDTO convertToDTO(Invoice invoice) {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setId(invoice.getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setGeneratedDate(invoice.getGeneratedDate());
        dto.setSubtotal(invoice.getSubtotal());
        dto.setTaxAmount(invoice.getTaxAmount());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setNotes(invoice.getNotes());
        dto.setPaid(invoice.isPaid());
        
        ServiceRecord service = invoice.getServiceRecord();
        dto.setServiceRecordId(service.getId());
        dto.setVehicleMake(service.getVehicle().getMake());
        dto.setVehicleModel(service.getVehicle().getModel());
        dto.setRegistrationNumber(service.getVehicle().getRegistrationNumber());
        dto.setCustomerName(service.getVehicle().getCustomer().getFirstName() + " " + 
                service.getVehicle().getCustomer().getLastName());
        dto.setCustomerEmail(service.getVehicle().getCustomer().getEmail());
        dto.setCustomerPhone(service.getVehicle().getCustomer().getPhoneNumber());
        
        dto.setServiceItems(service.getServiceItems().stream()
                .map(item -> {
                    ServiceItemDTO itemDTO = new ServiceItemDTO();
                    itemDTO.setId(item.getId());
                    itemDTO.setItemName(item.getItemName());
                    itemDTO.setDescription(item.getDescription());
                    itemDTO.setQuantity(item.getQuantity());
                    itemDTO.setUnitPrice(BigDecimal.valueOf(item.getUnitPrice()));
                    itemDTO.setTotalPrice(BigDecimal.valueOf(item.getTotalPrice()));
                    return itemDTO;
                })
                .collect(Collectors.toList()));
        
        return dto;
    }
} 