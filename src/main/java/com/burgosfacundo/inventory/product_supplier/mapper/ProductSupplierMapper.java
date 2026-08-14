package com.burgosfacundo.inventory.product_supplier.mapper;

import com.burgosfacundo.inventory.product.model.Product;
import com.burgosfacundo.inventory.product.dto.ProductSummaryResponse;
import com.burgosfacundo.inventory.product_supplier.dto.ProductSupplierResponse;
import com.burgosfacundo.inventory.product_supplier.dto.SupplierSummaryResponse;
import com.burgosfacundo.inventory.product_supplier.model.ProductSupplier;
import com.burgosfacundo.inventory.supplier.model.Supplier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductSupplierMapper {
    public static ProductSupplier toEntity(Product product, Supplier supplier, BigDecimal purchasePrice) {
        return new ProductSupplier(product,supplier,purchasePrice);
    }

    public static ProductSupplierResponse toResponse(ProductSupplier entity) {
        var product = entity.getProduct();
        var supplier = entity.getSupplier();

        var productSum = new ProductSummaryResponse(product.getId(),product.getSku(),product.getName());
        var supplierSum = new SupplierSummaryResponse(supplier.getId(),supplier.getName(),supplier.getEmail());

        return new ProductSupplierResponse(entity.getId(),
                productSum,
                supplierSum,
                entity.getPurchasePrice()
        );
    }
}
