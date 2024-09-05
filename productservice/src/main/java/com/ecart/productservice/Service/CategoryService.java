package com.ecart.productservice.Service;

import com.ecart.productservice.DTO.CategoryDTO;
import com.ecart.productservice.DTO.ProductDTO;
import com.ecart.productservice.Model.Category;
import com.ecart.productservice.Model.Product;
import com.ecart.productservice.Repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public void addCategory(CategoryDTO categoryDTO)
    {
        Category category = Category.builder()
                .name(categoryDTO.getName())
                .build();

        categoryRepository.save(category);
    }

    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        return mapToDTO(category);
    }

    public List<CategoryDTO> fetch() {
        List<Category> categories = categoryRepository.findAll();

        return categories.stream()
                .map(this::mapToDTO) // Use this::mapToDTO to refer to the method
                .collect(Collectors.toList());
    }

    private CategoryDTO mapToDTO(Category category) {
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(category.getId());
        categoryDTO.setName(category.getName());
        return categoryDTO;
    }
}
