package com.burgosfacundo.inventory.product_supplier.model;

import com.burgosfacundo.inventory.category.model.Category;
import com.burgosfacundo.inventory.product.model.Product;
import com.burgosfacundo.inventory.product_supplier.exception.InvalidPurchasePriceException;
import com.burgosfacundo.inventory.product_supplier.exception.ProductRequiredException;
import com.burgosfacundo.inventory.product_supplier.exception.PurchasePriceRequiredException;
import com.burgosfacundo.inventory.product_supplier.exception.SupplierRequiredException;
import com.burgosfacundo.inventory.supplier.model.Supplier;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductSupplierTest {

    private Product product() {
        Category category = new Category("Category", "Description");
        return new Product("SKU1", "Product 1", "Description", BigDecimal.TEN, category);
    }

    private Supplier supplier() {
        return new Supplier("name", "email@email.com", "2", "description");
    }

    @Test
    void shouldCreateProductSupplier() {
        Product product = product();
        Supplier supplier = supplier();

        ProductSupplier productSupplier =
                new ProductSupplier(product, supplier, new BigDecimal("80.00"));

        assertThat(productSupplier.getProduct()).isEqualTo(product);
        assertThat(productSupplier.getSupplier()).isEqualTo(supplier);
        assertThat(productSupplier.getPurchasePrice()).isEqualByComparingTo("80.00");
    }

    @Test
    void shouldThrowExceptionWhenProductIsNull() {
        assertThrows(ProductRequiredException.class,
                () -> new ProductSupplier(null, supplier(), new BigDecimal("80.00")));
    }

    @Test
    void shouldThrowExceptionWhenSupplierIsNull() {
        assertThrows(SupplierRequiredException.class,
                () -> new ProductSupplier(product(), null, new BigDecimal("80.00")));
    }

    @Test
    void shouldThrowExceptionWhenPurchasePriceIsNull() {
        assertThrows(PurchasePriceRequiredException.class,
                () -> new ProductSupplier(product(), supplier(), null));
    }

    @Test
    void shouldThrowExceptionWhenPurchasePriceIsNegative() {
        assertThrows(InvalidPurchasePriceException.class,
                () -> new ProductSupplier(product(), supplier(), new BigDecimal("-0.01")));
    }

    @Test
    void shouldAllowZeroPurchasePrice() {
        ProductSupplier productSupplier =
                new ProductSupplier(product(), supplier(), BigDecimal.ZERO);

        assertThat(productSupplier.getPurchasePrice()).isZero();
    }

    @Test
    void shouldUpdatePurchasePrice() {
        ProductSupplier productSupplier =
                new ProductSupplier(product(), supplier(), new BigDecimal("80.00"));

        productSupplier.updatePurchasePrice(new BigDecimal("95.50"));

        assertThat(productSupplier.getPurchasePrice()).isEqualByComparingTo("95.50");
    }

    @Test
    void shouldRejectNullPurchasePriceWhenUpdating() {
        ProductSupplier productSupplier =
                new ProductSupplier(product(), supplier(), new BigDecimal("80.00"));

        assertThrows(PurchasePriceRequiredException.class,
                () -> productSupplier.updatePurchasePrice(null));

        assertThat(productSupplier.getPurchasePrice()).isEqualByComparingTo("80.00");
    }

    @Test
    void shouldRejectNegativePurchasePriceWhenUpdating() {
        ProductSupplier productSupplier =
                new ProductSupplier(product(), supplier(), new BigDecimal("80.00"));

        assertThrows(InvalidPurchasePriceException.class,
                () -> productSupplier.updatePurchasePrice(new BigDecimal("-1.00")));

        assertThat(productSupplier.getPurchasePrice()).isEqualByComparingTo("80.00");
    }
}