package com.burgosfacundo.inventory.supplier.model;

import com.burgosfacundo.inventory.common.exception.NameRequiredException;
import com.burgosfacundo.inventory.supplier.exception.EmailInvalidException;
import com.burgosfacundo.inventory.supplier.exception.EmailRequiredException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity @Table(name = "suppliers")
@NoArgsConstructor(access = AccessLevel.PROTECTED) @Getter
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Column(length = 25)
    private String phone;

    @Column
    private String description;

    @Column(nullable = false)
    private boolean active;


    public Supplier(String name, String email, String phone, String description) {
        validateName(name);
        validateEmail(email);
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.description = description;
        activate();
    }

    public void activate(){
        this.active = true;
    }

    public void deactivate(){
        this.active = false;
    }

    public void update(String name, String email, String phone, String description){
        validateName(name);
        validateEmail(email);
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.description = description;
    }


    private static void validateName(String name){
        if(name == null || name.isBlank()){
            throw new NameRequiredException();
        }
    }

    private static void validateEmail(String email){
        if(email == null || email.isBlank()){
            throw new EmailRequiredException();
        }

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new EmailInvalidException();
        }
    }
}