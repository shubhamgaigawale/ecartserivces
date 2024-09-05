package com.ecart.productservice.Repository;

import com.ecart.productservice.Model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>
{
    Category findByName(String name);
}
