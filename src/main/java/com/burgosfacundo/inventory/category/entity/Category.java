package com.burgosfacundo.inventory.category.entity;

import com.burgosfacundo.inventory.category.exception.NameRequiredException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity @Table(name = "categories")
@NoArgsConstructor(access = AccessLevel.PROTECTED) @Getter
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column
    private String description;

    public Category(String name, String description) {
        validateName(name);
        this.name = name;
        this.description = description;
    }

    public void update(String name, String description) {
        validateName(name);
        this.name = name;
        this.description = description;
    }


    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new NameRequiredException();
        }
    }
}
