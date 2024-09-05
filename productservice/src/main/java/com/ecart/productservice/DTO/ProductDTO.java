package com.ecart.productservice.DTO;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private double price;
    private int stockQuantity;
    private String categoryName; // This will hold the name of the category

    private List<String> imageUrls; // List of image URLs associated with the product
}
