package com.moneylog.backend.dto.response;

import com.moneylog.backend.entity.Category;
import com.moneylog.backend.entity.CategoryType;

public record CategoryResponse (
        Long id,
        String name,
        CategoryType type
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getType());
    }
}
