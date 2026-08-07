package com.burgosfacundo.inventory.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @Size(max = 100, message = "Name must be at most 100 characters long")
        @NotBlank(message = "Name is required")
        String name,
        @Size(max = 255, message = "Description must be at most 255 characters long")
        String description) {
}
