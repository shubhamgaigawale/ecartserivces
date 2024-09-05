package com.ecart.productservice.Repository;

import com.ecart.productservice.Model.Product;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Cacheable(value = "productsCache")
    @Query("SELECT p FROM Product p WHERE " +
           "(:name IS NULL OR p.name LIKE %:name%) AND " +
           "(:category IS NULL OR p.category.name LIKE %:category%) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    List<Product> findProducts(@Param("name") String name,
                               @Param("category") String category,
                               @Param("minPrice") Double minPrice,
                               @Param("maxPrice") Double maxPrice);

    @Cacheable(value = "productCache", key = "#id")
    Optional<Product> findById(Long id);
}
