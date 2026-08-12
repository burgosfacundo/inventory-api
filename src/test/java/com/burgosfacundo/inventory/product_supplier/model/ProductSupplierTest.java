package com.burgosfacundo.inventory.product_supplier.model;

import com.burgosfacundo.inventory.category.model.Category;
import com.burgosfacundo.inventory.product.model.Product;
import com.burgosfacundo.inventory.product_supplier.exception.ProductRequiredException;
import com.burgosfacundo.inventory.product_supplier.exception.SupplierRequiredException;
import com.burgosfacundo.inventory.supplier.model.Supplier;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProductSupplierTest {

    @Test
    void shouldCreateProductSupplier() {
        Category category = new Category("Category", "Description");
        Product product = new Product("SKU1", "Product 1", "Description", BigDecimal.valueOf(10), category);
        Supplier supplier = new Supplier("name", "email@email.com", "2", "description");
        ProductSupplier productSupplier = new ProductSupplier(product, supplier);

        assertThat(productSupplier.getProduct()).isEqualTo(product);
        assertThat(productSupplier.getSupplier()).isEqualTo(supplier);
    }

    @Test
    void shouldThrowExceptionWhenProductIsNull() {
        Supplier supplier = new Supplier("name", "email@email.com", "2", "description");

        assertThrows(ProductRequiredException.class,
                () -> new ProductSupplier(null, supplier));
    }

    @Test
    void shouldThrowExceptionWhenSupplierIsNull() {
        Category category = new Category("Category", "Description");
        Product product = new Product("SKU1", "Product 1", "Description", BigDecimal.valueOf(10), category);

        assertThrows(SupplierRequiredException.class,
                () -> new ProductSupplier(product, null));
    }
}
