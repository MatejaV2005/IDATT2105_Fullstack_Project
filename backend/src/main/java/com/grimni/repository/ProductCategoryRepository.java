package com.grimni.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.ProductCategory;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

}
