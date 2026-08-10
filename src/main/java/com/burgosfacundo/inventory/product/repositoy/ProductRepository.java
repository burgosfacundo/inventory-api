package com.burgosfacundo.inventory.product.repositoy;

import com.burgosfacundo.inventory.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
