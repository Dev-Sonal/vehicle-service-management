package com.tch.vehicle.repository;

import com.tch.vehicle.entity.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {
    List<ServiceItem> findByServiceRecordId(Long serviceRecordId);
    void deleteByServiceRecordId(Long serviceRecordId);
} 