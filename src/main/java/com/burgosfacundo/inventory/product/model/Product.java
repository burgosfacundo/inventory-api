package com.burgosfacundo.inventory.product.model;

import com.burgosfacundo.inventory.category.model.Category;
import com.burgosfacundo.inventory.common.exception.NameRequiredException;
import com.burgosfacundo.inventory.product.exception.CategoryRequiredException;
import com.burgosfacundo.inventory.product.exception.SalePriceNegativeException;
import com.burgosfacundo.inventory.product.exception.SalePriceRequiredException;
import com.burgosfacundo.inventory.product.exception.SkuRequiredException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity @Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED) @Getter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true,length = 50)
    private String sku;

    @Column(nullable = false, length = 100)
    private String name;

    @Column
    private String description;

    @Column(nullable = false,precision = 10,scale = 2)
    private BigDecimal salePrice;

    @Column(nullable = false)
    private Boolean active;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;


    public Product(String sku, String name, String description, BigDecimal salePrice,Category category) {
        validateSku(sku);
        validateName(name);
        validateSalePrice(salePrice);
        validateCategory(category);
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.salePrice = salePrice;
        this.category = category;
        activate();
    }

    private static void validateSku(String sku){
        if(sku == null || sku.isBlank()){
            throw new SkuRequiredException();
        }
    }

    private static void validateName(String name){
        if(name == null || name.isBlank()){
            throw new NameRequiredException();
        }
    }

    private static void validateSalePrice(BigDecimal salePrice) {
        if (salePrice == null) {
            throw new SalePriceRequiredException();
        }

        if (salePrice.signum() < 0) {
            throw new SalePriceNegativeException();
        }
    }

    private static void validateCategory(Category category) {
        if (category == null) {
            throw new CategoryRequiredException();
        }
    }

    public void activate(){
        this.active = true;
    }

    public void deactivate(){
        this.active = false;
    }

    public void update(String sku, String name, String description, BigDecimal salePrice, Category category) {
        validateSku(sku);
        validateName(name);
        validateSalePrice(salePrice);
        validateCategory(category);
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.salePrice = salePrice;
        this.category = category;
    }

}
