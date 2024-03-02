package de.bsi.ai4coding.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import de.bsi.ai4coding.product.model.Product;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
}
