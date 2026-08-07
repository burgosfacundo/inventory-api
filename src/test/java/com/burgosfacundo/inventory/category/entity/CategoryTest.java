package com.burgosfacundo.inventory.category.entity;

import com.burgosfacundo.inventory.category.exception.NameRequiredException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class CategoryTest {

    @Test
    public void shouldCreateCategoryWithValidName() {
       Category category = new Category("Electronics", "Devices and gadgets");
       assertThat(category.getName()).isEqualTo("Electronics");
       assertThat(category.getDescription()).isEqualTo("Devices and gadgets");
    }

    @Test
    public void shouldCreateThrowExceptionWhenNameIsNull() {
        assertThrows(NameRequiredException.class,
                () -> new Category(null, "Devices and gadgets"));
    }

    @Test
    public void shouldCreateThrowExceptionWhenNameIsBlank() {
        assertThrows(NameRequiredException.class,
                () -> new Category("", "Devices and gadgets"));
    }


    @Test
    public void shouldUpdateCategoryWithValidName() {
        Category category = new Category("Electronics", "Devices and gadgets");

        category.update("Home Appliances", "Appliances for home use");
        assertThat(category.getName()).isEqualTo("Home Appliances");
        assertThat(category.getDescription()).isEqualTo("Appliances for home use");
    }

    @Test
    public void shouldUpdateThrowExceptionWhenNameIsNull() {
        var category = new Category("Electronics", "Devices and gadgets");

        assertThrows(NameRequiredException.class,
                () -> category.update(null, "Appliances for home use"));
    }


    @Test
    public void shouldUpdateThrowExceptionWhenNameIsBlank() {
        var category = new Category("Electronics", "Devices and gadgets");

        assertThrows(NameRequiredException.class,
                () -> category.update("", "Appliances for home use"));
    }
}
