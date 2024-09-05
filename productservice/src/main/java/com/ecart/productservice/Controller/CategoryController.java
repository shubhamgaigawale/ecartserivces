package com.ecart.productservice.Controller;

import com.ecart.productservice.Constants.Constants;
import com.ecart.productservice.DTO.CategoryDTO;
import com.ecart.productservice.DTO.ResponseDto;
import com.ecart.productservice.Service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping("/create")
    public ResponseEntity<ResponseDto> addCategory(@RequestBody CategoryDTO categoryDTO)
    {
        categoryService.addCategory(categoryDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto(Constants.STATUS_201, Constants.MESSAGE_201));
    }

    @GetMapping("/fetch/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id)
    {
        CategoryDTO categoryById = categoryService.getCategoryById(id);

        return ResponseEntity.status(HttpStatus.OK).body(categoryById);
    }

    @GetMapping("/fetch")
    public ResponseEntity<List<CategoryDTO>> fetch()
    {
        List<CategoryDTO> categoryDTOS = categoryService.fetch();

        return ResponseEntity.status(HttpStatus.OK).body(categoryDTOS);
    }
}
