package com.ecart.productservice.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.ecart.productservice.DTO.ProductDTO;
import com.ecart.productservice.Exception.ResourceNotFoundException;
import com.ecart.productservice.Mapper.ProductConverter;
import com.ecart.productservice.Model.Category;
import com.ecart.productservice.Model.Image;
import com.ecart.productservice.Model.Product;
import com.ecart.productservice.Repository.CategoryRepository;
import com.ecart.productservice.Repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private final CategoryRepository categoryRepository;

//    @Autowired
//    private final AmazonS3Client amazonS3Client;

    @Autowired
    private final ProductConverter productConverter;
    private final String bucketName = "productimagesforproject";

    @Autowired
    private final AmazonS3 amazonS3Client;

    // Retrieve all products with optional filters
    @Cacheable(value = "productsCache")
    public List<ProductDTO> getAllProducts(String name, String category, Double minPrice, Double maxPrice) {
        List<Product> products = productRepository.findProducts(name, category, minPrice, maxPrice);

        List<Product> productList = productRepository.findAll();

        return products.stream()
                .map(productConverter::convertToDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "productsCache")
    public List<ProductDTO> getAllProducts() {

        List<Product> products = productRepository.findAll();

        return products.stream()
                .map(productConverter::convertToDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "productCache", key = "#id")
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return productConverter.convertToDTO(product);
    }

    public void addProduct(ProductDTO productDTO)
    {
        Category category = categoryRepository.findByName(productDTO.getCategoryName());

        if (category == null)
        {
          throw new ResourceNotFoundException("Category not found");
        }

        List<Image> images = productDTO.getImageUrls().stream()
                .map(url -> Image.builder().imageUrl(url).build())
                .collect(Collectors.toList());

        Product product = productConverter.convertToEntity(productDTO, category, images);

        productRepository.save(product);
    }

    @CacheEvict(value = "productCache", key = "#id")
    public boolean updateProduct(Long id, ProductDTO productDTO)
    {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Category category = categoryRepository.findByName(productDTO.getCategoryName());

        if (category == null)
        {
            throw new ResourceNotFoundException("Category not found");
        }

        List<Image> images = productDTO.getImageUrls().stream()
                .map(url -> Image.builder().imageUrl(url).build())
                .collect(Collectors.toList());

        existingProduct.setName(productDTO.getName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setStockQuantity(productDTO.getStockQuantity());
        existingProduct.setCategory(category);
        existingProduct.setImages(images);

        productRepository.save(existingProduct);

        return true;
    }

    @CacheEvict(value = "productCache", key = "#id")
    public boolean deleteProduct(Long id)
    {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        productRepository.deleteById(id);

        return true;
    }

    // Handle bulk product uploads from a CSV or Excel file
    public void uploadProducts(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    // Skip header row if present
                    continue;
                }

                Product product = new Product();

                // Name
                if (row.getCell(0) != null) {
                    product.setName(row.getCell(0).getStringCellValue());
                }

                // Description
                if (row.getCell(1) != null) {
                    product.setDescription(row.getCell(1).getStringCellValue());
                }

                // Price
                if (row.getCell(2) != null) {
                    product.setPrice(row.getCell(2).getNumericCellValue());
                }

                // Stock quantity
                if (row.getCell(3) != null) {
                    product.setStockQuantity((int) row.getCell(3).getNumericCellValue());
                }

                // Category (assuming the category ID is in column 4)
                if (row.getCell(4) != null) {
                    Long categoryId = (long) row.getCell(4).getNumericCellValue();
                    Category category = categoryRepository.findById(categoryId)
                            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
                    product.setCategory(category);
                }

                // Save the product
                productRepository.save(product);
            }

            workbook.close(); // Close the workbook to avoid resource leaks
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse file", e);
        }
    }


    // Upload a product image to AWS S3 and return the image URL
    public void uploadProductImage(MultipartFile imageFile, Long productId)
    {
        String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
        try (InputStream is = imageFile.getInputStream())
        {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(is.available());
            amazonS3Client.putObject(new PutObjectRequest(bucketName, fileName, is, metadata));
            String imageUrl = amazonS3Client.getUrl(bucketName, fileName).toString();

            // Fetch the product by ID
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            // Create and associate the new image with the product
            Image image = Image.builder().imageUrl(imageUrl).product(product).build();
            product.getImages().add(image);

            // Save the updated product
            productRepository.save(product);

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image", e);
        }
    }

}
