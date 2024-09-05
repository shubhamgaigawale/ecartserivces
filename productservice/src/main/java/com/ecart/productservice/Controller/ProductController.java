package com.ecart.productservice.Controller;

import com.ecart.productservice.Constants.Constants;
import com.ecart.productservice.DTO.ProductDTO;
import com.ecart.productservice.DTO.ResponseDto;
import com.ecart.productservice.Service.ProductService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController
{
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    @GetMapping("/find")
    public List<ProductDTO> getAllProducts(@RequestParam(required = false) String name,
                                           @RequestParam(required = false) String category,
                                           @RequestParam(required = false) Double minPrice,
                                           @RequestParam(required = false) Double maxPrice) {
        return productService.getAllProducts(name, category, minPrice, maxPrice);
    }

    @GetMapping("/fetch")
    public ResponseEntity<List<ProductDTO>> getAllProducts()
    {
         List<ProductDTO> productDTOList = productService.getAllProducts();

        return ResponseEntity.status(HttpStatus.OK).body(productDTOList);
    }

    @GetMapping("/fetch/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id)
    {
        ProductDTO productDTO = productService.getProductById(id);

        return ResponseEntity.status(HttpStatus.OK).body(productDTO);
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseDto> addProduct(@RequestBody ProductDTO productDTO, @RequestHeader(value = "X-Authenticated-Roles", required = false) String roles)
    {
        if (roles != null && roles.contains("ROLE_ADMIN"))
        {
            productService.addProduct(productDTO);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ResponseDto(Constants.STATUS_201, Constants.MESSAGE_201));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseDto(HttpStatus.FORBIDDEN.toString(), Constants.MESSAGE_403));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseDto> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO, @RequestHeader(value = "X-Authenticated-Roles", required = false) String roles)
    {
        if (roles != null && roles.contains("ROLE_ADMIN"))
        {
            boolean isUpdated = productService.updateProduct(id, productDTO);

            if(isUpdated)
            {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(new ResponseDto(Constants.STATUS_200, Constants.MESSAGE_200));
            } else
            {
                return ResponseEntity
                        .status(HttpStatus.EXPECTATION_FAILED)
                        .body(new ResponseDto(Constants.STATUS_417, Constants.MESSAGE_417_UPDATE));
            }
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseDto(HttpStatus.FORBIDDEN.toString(), Constants.MESSAGE_403));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseDto> deleteProduct(@PathVariable Long id, @RequestHeader(value = "X-Authenticated-Roles", required = false) String roles)
    {
        if (roles != null && roles.contains("ROLE_ADMIN"))
        {
            boolean isDeleted = productService.deleteProduct(id);

            if(isDeleted)
            {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(new ResponseDto(Constants.STATUS_200, Constants.MESSAGE_200));
            } else
            {
                return ResponseEntity
                        .status(HttpStatus.EXPECTATION_FAILED)
                        .body(new ResponseDto(Constants.STATUS_417, Constants.MESSAGE_417_DELETE));
            }
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseDto(HttpStatus.FORBIDDEN.toString(), Constants.MESSAGE_403));
    }

    @PostMapping("/upload")
    public ResponseEntity<ResponseDto> uploadProducts(@RequestParam("file") MultipartFile file, @RequestHeader(value = "X-Authenticated-Roles", required = false) String roles)
    {
        if (roles != null && roles.contains("ROLE_ADMIN"))
        {
            productService.uploadProducts(file);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ResponseDto(Constants.STATUS_201, Constants.MESSAGE_201));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseDto(HttpStatus.FORBIDDEN.toString(), Constants.MESSAGE_403));
    }

    // 2. Upload Product Image Endpoint
    @PostMapping("/upload-image")
    public ResponseEntity<ResponseDto> uploadProductImage(@RequestParam("imageFile") MultipartFile imageFile,
                                         @RequestParam("productId") Long productId,@RequestHeader(value = "X-Authenticated-Roles", required = false) String roles)
    {
        if (roles != null && roles.contains("ROLE_ADMIN"))
        {
            productService.uploadProductImage(imageFile, productId);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ResponseDto(Constants.STATUS_201, Constants.MESSAGE_201));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseDto(HttpStatus.FORBIDDEN.toString(), Constants.MESSAGE_403));
    }
}
