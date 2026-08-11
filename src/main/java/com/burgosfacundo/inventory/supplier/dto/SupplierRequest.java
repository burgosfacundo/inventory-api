package com.burgosfacundo.inventory.supplier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupplierRequest(
                            @NotBlank(message = "Name is required")
                            @Size(max = 100, message = "Name must be at most 100 characters long")
                            String name,
                            @NotBlank(message = "Email is required")
                            @Size(max = 100, message = "Email must be at most 100 characters long")
                            @Email(message = "Email format is invalid")
                            String email,
                            @Size(max = 25, message = "Phone must be at most 25 characters long")
                            String phone,
                            @Size(max = 255, message = "Description must be at most 255 characters long")
                            String description) {
}
