package com.burgosfacundo.inventory.supplier.model;

import com.burgosfacundo.inventory.common.exception.NameRequiredException;
import com.burgosfacundo.inventory.supplier.exception.EmailInvalidException;
import com.burgosfacundo.inventory.supplier.exception.EmailRequiredException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SupplierTest {

    @Test
    void shouldCreateSupplierWithValidData() {
        Supplier supplier = new Supplier("Name","supplier@email.com","123444",null);

        assertThat(supplier.getName()).isEqualTo("Name");
        assertThat(supplier.getEmail()).isEqualTo("supplier@email.com");
        assertThat(supplier.getPhone()).isEqualTo("123444");
        assertThat(supplier.isActive()).isTrue();
    }

    @Test
    void shouldActivateAndDeactivateSupplier() {
        Supplier supplier = new Supplier(
                "Name",
                "supplier@email.com",
                "123444",
                null
        );

        supplier.deactivate();

        assertThat(supplier.isActive()).isFalse();

        supplier.activate();

        assertThat(supplier.isActive()).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        var exception = assertThrows(NameRequiredException.class,
                ()-> new Supplier(null,"supplier@email.com","123444",null));

        assertThat(exception.getMessage()).isEqualTo("Name is required");
        assertThat(exception.getErrorCode()).isEqualTo("NAME_REQUIRED");
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        var exception = assertThrows(NameRequiredException.class,
                ()-> new Supplier("","supplier@email.com","123444",null));

        assertThat(exception.getMessage()).isEqualTo("Name is required");
        assertThat(exception.getErrorCode()).isEqualTo("NAME_REQUIRED");
    }


    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        var exception = assertThrows(EmailRequiredException.class,
                ()-> new Supplier("Name",null,"123444",null));

        assertThat(exception.getMessage()).isEqualTo("Email is required");
        assertThat(exception.getErrorCode()).isEqualTo("EMAIL_REQUIRED");
    }

    @Test
    void shouldThrowExceptionWhenEmailIsBlank() {
        var exception = assertThrows(EmailRequiredException.class,
                ()-> new Supplier("Name","","123444",null));
        assertThat(exception.getMessage()).isEqualTo("Email is required");
        assertThat(exception.getErrorCode()).isEqualTo("EMAIL_REQUIRED");
    }

    @Test
    void shouldThrowExceptionWhenEmailIsInvalid(){
        var exception = assertThrows(EmailInvalidException.class,
                ()-> new Supplier("Name","error","123444",null));

        assertThat(exception.getMessage()).isEqualTo("Email format is invalid");
        assertThat(exception.getErrorCode()).isEqualTo("EMAIL_FORMAT_INVALID");
    }

    @Test
    void shouldUpdateSupplierWithValidData() {
        Supplier supplier = new Supplier("Name","supplier@email.com","123444",null);

        supplier.update("New name","supplier2@email.com","22","new description");

        assertThat(supplier.getName()).isEqualTo("New name");
        assertThat(supplier.getEmail()).isEqualTo("supplier2@email.com");
        assertThat(supplier.getPhone()).isEqualTo("22");
        assertThat(supplier.getDescription()).isEqualTo("new description");
    }

    @Test
    void shouldThrowUpdateExceptionWhenNameIsNull() {
        Supplier supplier = new Supplier("Name","supplier@email.com","123444",null);

        var exception = assertThrows(NameRequiredException.class,
                ()-> supplier.update(null,"supplier@email.com","123444",null));

        assertThat(exception.getMessage()).isEqualTo("Name is required");
        assertThat(exception.getErrorCode()).isEqualTo("NAME_REQUIRED");
    }

    @Test
    void shouldThrowUpdateExceptionWhenNameIsBlank() {
        Supplier supplier = new Supplier("Name","supplier@email.com","123444",null);

        var exception = assertThrows(NameRequiredException.class,
                ()-> supplier.update("","supplier@email.com","123444",null));

        assertThat(exception.getMessage()).isEqualTo("Name is required");
        assertThat(exception.getErrorCode()).isEqualTo("NAME_REQUIRED");
    }


    @Test
    void shouldThrowUpdateExceptionWhenEmailIsNull() {
        Supplier supplier = new Supplier("Name","supplier@email.com","123444",null);

        var exception = assertThrows(EmailRequiredException.class,
                ()-> supplier.update("Name",null,"123444",null));

        assertThat(exception.getMessage()).isEqualTo("Email is required");
        assertThat(exception.getErrorCode()).isEqualTo("EMAIL_REQUIRED");
    }

    @Test
    void shouldThrowUpdateExceptionWhenEmailIsBlank() {
        Supplier supplier = new Supplier("Name","supplier@email.com","123444",null);

        var exception = assertThrows(EmailRequiredException.class,
                ()-> supplier.update("Name","","123444",null));

        assertThat(exception.getMessage()).isEqualTo("Email is required");
        assertThat(exception.getErrorCode()).isEqualTo("EMAIL_REQUIRED");
    }

    @Test
    void shouldThrowUpdateExceptionWhenEmailIsInvalid(){
        Supplier supplier = new Supplier("Name","supplier@email.com","123444",null);

        var exception = assertThrows(EmailInvalidException.class,
                ()-> supplier.update("Name","error","123444",null));

        assertThat(exception.getMessage()).isEqualTo("Email format is invalid");
        assertThat(exception.getErrorCode()).isEqualTo("EMAIL_FORMAT_INVALID");
    }


}
