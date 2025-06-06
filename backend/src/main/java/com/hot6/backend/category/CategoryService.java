package com.hot6.backend.category;


import com.hot6.backend.category.model.Category;
import com.hot6.backend.category.model.CategoryDto;
import com.hot6.backend.category.model.CategoryRepository;
import com.hot6.backend.category.model.CategoryType;
import com.hot6.backend.common.BaseResponseStatus;
import com.hot6.backend.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Transactional
    public void createCategory(CategoryDto.CategoryCreateRequest request) {
        // 문자열 → enum 변환
        CategoryType categoryType;
        try {
            categoryType = CategoryType.valueOf(request.getType());
        } catch (IllegalArgumentException e) {
            throw new BaseException(BaseResponseStatus.CATEGORY_INVALID_TYPE);
        }

        Category category = Category.builder()
                .name(request.getName())
                .color(request.getColor())
                .description(request.getDescription())
                .type(categoryType)
                .build();

        categoryRepository.save(category);
    }

    public List<CategoryDto.CategoryResponse> getCategoryList(CategoryType categoryType) {
        return categoryRepository.findByType(categoryType).stream()
                .map(category -> CategoryDto.CategoryResponse.builder()
                        .Idx(category.getIdx())
                        .name(category.getName())
                        .color(category.getColor())
                        .build())
                .toList();
    }

    @Transactional
    public void updateCategory(Long categoryIdx, CategoryDto.CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(categoryIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.CATEGORY_NOT_FOUND));

        category.setName(request.getName());
        category.setColor(request.getColor());
        category.setDescription(request.getDescription());

        categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long categoryIdx) {
        Category category = categoryRepository.findById(categoryIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.CATEGORY_NOT_FOUND));

        categoryRepository.delete(category);
    }
}
