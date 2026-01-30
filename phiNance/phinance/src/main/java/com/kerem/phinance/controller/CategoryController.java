package com.kerem.phinance.controller;

import com.kerem.phinance.dto.CategoryDto;
import com.kerem.phinance.model.Category;
import com.kerem.phinance.security.UserPrincipal;
import com.kerem.phinance.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management APIs")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all categories")
    public ResponseEntity<List<CategoryDto>> getAllCategories(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(categoryService.getAllCategories(userPrincipal.getId()));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get categories by type")
    public ResponseEntity<List<CategoryDto>> getCategoriesByType(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Category.CategoryType type) {
        return ResponseEntity.ok(categoryService.getCategoriesByType(userPrincipal.getId(), type));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<CategoryDto> getCategoryById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id) {
        return ResponseEntity.ok(categoryService.getCategoryById(userPrincipal.getId(), id));
    }

    @PostMapping
    @Operation(summary = "Create a new category")
    public ResponseEntity<CategoryDto> createCategory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CategoryDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createCategory(userPrincipal.getId(), dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a category")
    public ResponseEntity<CategoryDto> updateCategory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id,
            @Valid @RequestBody CategoryDto dto) {
        return ResponseEntity.ok(categoryService.updateCategory(userPrincipal.getId(), id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category")
    public ResponseEntity<Map<String, String>> deleteCategory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id) {
        categoryService.deleteCategory(userPrincipal.getId(), id);
        return ResponseEntity.ok(Map.of("message", "Category deleted successfully"));
    }
}
