package com.kerem.phinance.service;

import com.kerem.phinance.dto.CategoryDto;
import com.kerem.phinance.exception.ResourceNotFoundException;
import com.kerem.phinance.model.Category;
import com.kerem.phinance.repository.CategoryRepository;
import com.kerem.phinance.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Page<CategoryDto> getCategoriesPaginated(Pageable pageable) {
        String userId = SecurityUtils.getCurrentUserId();
        return categoryRepository.findByUserIdOrIsDefaultTrueCaseInsensitive(userId, pageable)
                .map(this::mapToDto);
    }

    public List<CategoryDto> getCategoriesByType(Category.CategoryType type) {
        String userId = SecurityUtils.getCurrentUserId();
        return categoryRepository.findByUserIdAndType(userId, type).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public CategoryDto getCategoryById(String categoryId) {
        String userId = SecurityUtils.getCurrentUserId();
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
        return mapToDto(category);
    }

    public CategoryDto createCategory(CategoryDto dto) {
        String userId = SecurityUtils.getCurrentUserId();
        Category category = new Category();
        category.setUserId(userId);
        category.setName(dto.getName());
        category.setType(dto.getType());
        category.setIcon(dto.getIcon());
        category.setColor(dto.getColor());
        category.setParentCategoryId(dto.getParentCategoryId());

        Category saved = categoryRepository.save(category);
        return mapToDto(saved);
    }

    public CategoryDto updateCategory(String categoryId, CategoryDto dto) {
        String userId = SecurityUtils.getCurrentUserId();
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        category.setName(dto.getName());
        category.setType(dto.getType());
        category.setIcon(dto.getIcon());
        category.setColor(dto.getColor());
        category.setParentCategoryId(dto.getParentCategoryId());

        Category saved = categoryRepository.save(category);
        return mapToDto(saved);
    }

    public void deleteCategory(String categoryId) {
        String userId = SecurityUtils.getCurrentUserId();
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        // Delete subcategories
        List<Category> subcategories = categoryRepository.findByParentCategoryId(categoryId);
        categoryRepository.deleteAll(subcategories);

        categoryRepository.delete(category);
    }

    private CategoryDto mapToDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setType(category.getType());
        dto.setIcon(category.getIcon());
        dto.setColor(category.getColor());
        dto.setParentCategoryId(category.getParentCategoryId());
        return dto;
    }
}
