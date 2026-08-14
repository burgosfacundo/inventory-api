package com.burgosfacundo.inventory.product_supplier.service;

import com.burgosfacundo.inventory.product.exception.ProductNotFoundException;
import com.burgosfacundo.inventory.product.repository.ProductRepository;
import com.burgosfacundo.inventory.product_supplier.dto.ProductSupplierPriceRequest;
import com.burgosfacundo.inventory.product_supplier.dto.ProductSupplierResponse;
import com.burgosfacundo.inventory.product_supplier.dto.ProductSupplierRequest;
import com.burgosfacundo.inventory.product_supplier.exception.ProductSupplierAlreadyExistsException;
import com.burgosfacundo.inventory.product_supplier.exception.ProductSupplierNotFoundException;
import com.burgosfacundo.inventory.product_supplier.mapper.ProductSupplierMapper;
import com.burgosfacundo.inventory.product_supplier.repository.ProductSupplierRepository;
import com.burgosfacundo.inventory.supplier.exception.SupplierNotFoundException;
import com.burgosfacundo.inventory.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductSupplierServiceImpl implements ProductSupplierService {
    private final ProductSupplierRepository repository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;


    @Transactional
    @Override
    public ProductSupplierResponse save(ProductSupplierRequest request) {
        var product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ProductNotFoundException(request.productId()));

        var supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new SupplierNotFoundException(request.supplierId()));

        if (repository.existsByProductIdAndSupplierId(request.productId(), request.supplierId())) {
            throw new ProductSupplierAlreadyExistsException(
                    request.productId(),
                    request.supplierId()
            );
        }

        var saved = repository.save(ProductSupplierMapper.toEntity(product,supplier,request.purchasePrice()));

        return ProductSupplierMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public ProductSupplierResponse findById(Long id) {
        var ps = repository.findWithRelationsById(id).orElseThrow(
                ()-> new ProductSupplierNotFoundException(id));

        return ProductSupplierMapper.toResponse(ps);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ProductSupplierResponse> findAll(Long productId, Long supplierId,Pageable pageable) {
        return repository.findAllFiltered(productId,supplierId,pageable)
                .map(ProductSupplierMapper::toResponse);
    }

    @Transactional
    @Override
    public ProductSupplierResponse updatePurchasePrice(Long id, ProductSupplierPriceRequest request) {
        var productSupplier = repository.findWithRelationsById(id)
                .orElseThrow(() -> new ProductSupplierNotFoundException(id));

        productSupplier.updatePurchasePrice(request.purchasePrice());

        return ProductSupplierMapper.toResponse(productSupplier);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ProductSupplierNotFoundException(id);
        }

        repository.deleteById(id);
    }
}
