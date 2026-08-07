package com.burgosfacundo.inventory_api.category.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity @Table(name = "categories")
@NoArgsConstructor @Getter
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column
    private String description;

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
