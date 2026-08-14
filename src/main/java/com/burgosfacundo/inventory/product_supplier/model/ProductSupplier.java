package com.burgosfacundo.inventory.product_supplier.model;

import com.burgosfacundo.inventory.product.model.Product;
import com.burgosfacundo.inventory.product_supplier.exception.InvalidPurchasePriceException;
import com.burgosfacundo.inventory.product_supplier.exception.ProductRequiredException;
import com.burgosfacundo.inventory.product_supplier.exception.PurchasePriceRequiredException;
import com.burgosfacundo.inventory.product_supplier.exception.SupplierRequiredException;
import com.burgosfacundo.inventory.supplier.model.Supplier;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity @Table(name = "product_suppliers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_product_supplier",
                        columnNames = {"product_id", "supplier_id"}
                )
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED) @Getter
public class ProductSupplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "purchase_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal purchasePrice;

    public ProductSupplier(Product product, Supplier supplier, BigDecimal purchasePrice) {
        if (product == null) {
            throw new ProductRequiredException();
        }

        if (supplier == null) {
            throw new SupplierRequiredException();
        }

        validatePurchasePrice(purchasePrice);

        this.product = product;
        this.supplier = supplier;
        this.purchasePrice = purchasePrice;
    }

    private static void validatePurchasePrice(BigDecimal purchasePrice) {
        if (purchasePrice == null) {
            throw new PurchasePriceRequiredException();
        }

        if (purchasePrice.signum() < 0) {
            throw new InvalidPurchasePriceException();
        }
    }

    public void updatePurchasePrice(BigDecimal purchasePrice) {
        validatePurchasePrice(purchasePrice);
        this.purchasePrice = purchasePrice;
    }
}
