package com.ecart.productservice.Mapper;

import com.ecart.productservice.DTO.ProductDTO;
import com.ecart.productservice.Model.Category;
import com.ecart.productservice.Model.Image;
import com.ecart.productservice.Model.Product;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductConverter {

    public ProductDTO convertToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .categoryName(product.getCategory().getName())
                .imageUrls(product.getImages().stream()
                        .map(Image::getImageUrl)
                        .collect(Collectors.toList()))
                .build();
    }

    public Product convertToEntity(ProductDTO productDTO, Category category, List<Image> images) {
        return Product.builder()
                .id(productDTO.getId())
                .name(productDTO.getName())
                .description(productDTO.getDescription())
                .price(productDTO.getPrice())
                .stockQuantity(productDTO.getStockQuantity())
                .category(category)
                .images(images)
                .build();
    }
}
