package com.burgosfacundo.inventory.supplier.service;

import com.burgosfacundo.inventory.supplier.dto.SupplierRequest;
import com.burgosfacundo.inventory.supplier.dto.SupplierResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SupplierService {
    SupplierResponse save(SupplierRequest request);
    SupplierResponse findById(Long id);
    Page<SupplierResponse> findAll(Boolean active,Pageable pageable);
    SupplierResponse update(Long id, SupplierRequest request);
    SupplierResponse updateStatus(Long id, boolean status);
    void delete(Long id);
}
