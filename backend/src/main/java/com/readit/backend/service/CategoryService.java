package com.readit.backend.service;

import com.readit.backend.dto.CategoryDTO;
import com.readit.backend.entity.Category;
import com.readit.backend.exception.ResourceNotFoundException;
import com.readit.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return toDTO(category);
    }

    @Transactional
    public CategoryDTO createCategory(CategoryDTO dto) {
        Category category = modelMapper.map(dto, Category.class);
        category.setId(null);
        if (category.getImageUrl() == null || category.getImageUrl().isBlank()) {
            category.setImageUrl(dto.getImg());
        }
        Category saved = categoryRepository.save(category);
        return toDTO(saved);
    }

    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setImageUrl(
                dto.getImageUrl() != null && !dto.getImageUrl().isBlank()
                        ? dto.getImageUrl()
                        : dto.getImg());
        Category updated = categoryRepository.save(category);
        return toDTO(updated);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category", "id", id);
        }
        categoryRepository.deleteById(id);
    }

    private CategoryDTO toDTO(Category category) {
        CategoryDTO dto = modelMapper.map(category, CategoryDTO.class);
        dto.setImg(category.getImageUrl());
        return dto;
    }
}
