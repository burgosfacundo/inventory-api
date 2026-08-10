package com.burgosfacundo.inventory.product.model;

import com.burgosfacundo.inventory.category.model.Category;
import com.burgosfacundo.inventory.common.exception.NameRequiredException;
import com.burgosfacundo.inventory.product.exception.CategoryRequiredException;
import com.burgosfacundo.inventory.product.exception.SalePriceNegativeException;
import com.burgosfacundo.inventory.product.exception.SalePriceRequiredException;
import com.burgosfacundo.inventory.product.exception.SkuRequiredException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProductTest {

    @Test
    public void shouldCreateProductWithValidData() {
        Category category = new Category("Category","Description");
        Product product = new Product("SKU1","Product 1", "Description", BigDecimal.valueOf(10), category);
        assertThat(product.getSku()).isEqualTo("SKU1");
        assertThat(product.getName()).isEqualTo("Product 1");
        assertThat(product.getDescription()).isEqualTo("Description");
        assertThat(product.getSalePrice()).isEqualTo(BigDecimal.valueOf(10));
        assertThat(product.getCategory()).isEqualTo(category);
    }


    @Test
    public void shouldCreateProductThrowExceptionWhenNameIsNull() {
        Category category = new Category("Category","Description");
        assertThrows(NameRequiredException.class,
                () -> new Product("SKU1",null, null, BigDecimal.valueOf(10.0), category));
    }

    @Test
    public void shouldCreateProductThrowExceptionWhenNameIsBlank() {
        Category category = new Category("Category","Description");
        assertThrows(NameRequiredException.class,
                () -> new Product("SKU1","", null, BigDecimal.valueOf(10.0), category));
    }


    @Test
    public void shouldCreateProductThrowExceptionWhenSkuIsNull() {
        Category category = new Category("Category","Description");
        assertThrows(SkuRequiredException.class,
                () -> new Product(null,"Product 1", null, BigDecimal.valueOf(10), category));
    }

    @Test
    public void shouldCreateProductThrowExceptionWhenSkuIsBlank() {
        Category category = new Category("Category","Description");
        assertThrows(SkuRequiredException.class,
                () -> new Product("","Product 1", null, BigDecimal.valueOf(10), category));
    }


    @Test
    public void shouldCreateProductThrowExceptionWhenSalePriceIsNull() {
        Category category = new Category("Category","Description");
        assertThrows(SalePriceRequiredException.class,
                () -> new Product("SKU1","Product 1", null, null, category));
    }

    @Test
    public void shouldCreateProductThrowExceptionWhenSalePriceIsNegative() {
        Category category = new Category("Category","Description");
        assertThrows(SalePriceNegativeException.class,
                () -> new Product("SKU1","Product 1", null, BigDecimal.valueOf(-1), category));
    }


    @Test
    public void shouldCreateProductThrowExceptionWhenCategoryIsNull() {
        assertThrows(CategoryRequiredException.class,
                () -> new Product("SKU1","Product 1", null, BigDecimal.valueOf(10), null));
    }


    @Test
    public void shouldUpdateProductWithValidData() {
        Category category = new Category("Category","Description");
        Product product = new Product("SKU1","Product 1", null, BigDecimal.valueOf(10), category);

        Category category2 = new Category("Category2","Description2");
        product.update("SKU2","Product 2", "Updated Description", BigDecimal.valueOf(15), category2);

        assertThat(product.getSku()).isEqualTo("SKU2");
        assertThat(product.getName()).isEqualTo("Product 2");
        assertThat(product.getDescription()).isEqualTo("Updated Description");
        assertThat(product.getSalePrice()).isEqualTo(BigDecimal.valueOf(15));
        assertThat(product.getCategory()).isEqualTo(category2);
    }


    @Test
    public void shouldUpdateProductThrowExceptionWhenNameIsNull() {
        Category category = new Category("Category","Description");
        Product product = new Product("SKU1","Product", null, BigDecimal.valueOf(10), category);
        assertThrows(NameRequiredException.class,
                () -> product.update("SKU2",null,null, BigDecimal.valueOf(10), category));
    }

    @Test
    public void shouldUpdateProductThrowExceptionWhenNameIsBlank() {
        Category category = new Category("Category","Description");
        Product product = new Product("SKU1","Product", null, BigDecimal.valueOf(10), category);
        assertThrows(NameRequiredException.class,
                () -> product.update("SKU2",null,null, BigDecimal.valueOf(10), category));
    }


    @Test
    public void shouldUpdateProductThrowExceptionWhenSkuIsNull() {
        Category category = new Category("Category","Description");
        Product product = new Product("SKU1","Product", null, BigDecimal.valueOf(10), category);
        assertThrows(SkuRequiredException.class,
                () -> product.update(null,"Product",null, BigDecimal.valueOf(10), category));
    }

    @Test
    public void shouldUpdateProductThrowExceptionWhenSkuIsBlank() {
        Category category = new Category("Category","Description");
        Product product = new Product("SKU1","Product", null, BigDecimal.valueOf(10), category);
        assertThrows(SkuRequiredException.class,
                () -> product.update("","Product",null, BigDecimal.valueOf(10), category));
    }

    @Test
    public void shouldUpdateProductThrowExceptionWhenSalePriceIsNull() {
        Category category = new Category("Category","Description");
        Product product = new Product("SKU1","Product", null, BigDecimal.valueOf(10), category);
        assertThrows(SalePriceRequiredException.class,
                () -> product.update("SKU2","Product",null, null, category));
    }

    @Test
    public void shouldUpdateProductThrowExceptionWhenSalePriceIsNegative() {
        Category category = new Category("Category","Description");
        Product product = new Product("SKU1","Product", null, BigDecimal.valueOf(10), category);
        assertThrows(SalePriceNegativeException.class,
                () -> product.update("SKU2","Product",null, BigDecimal.valueOf(-1), category));
    }


    @Test
    public void shouldUpdateProductThrowExceptionWhenCategoryIsNull() {
        Category category = new Category("Category","Description");
        Product product = new Product("SKU1","Product", null, BigDecimal.valueOf(10), category);
        assertThrows(NameRequiredException.class,
                () -> product.update("SKU2",null,null, BigDecimal.valueOf(1), null));
    }
}
