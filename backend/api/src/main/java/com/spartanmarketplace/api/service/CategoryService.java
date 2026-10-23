package com.spartanmarketplace.api.service;

import com.spartanmarketplace.api.entity.Category;
import com.spartanmarketplace.api.exception.ConflictException;
import com.spartanmarketplace.api.exception.NotFoundException;
import com.spartanmarketplace.api.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categories;
    private final AccessService access;

    @Transactional(readOnly = true)
    public List<Category> list() {
        return categories.findAll();
    }

    @Transactional(readOnly = true)
    public Category get(UUID id) {
        return categories.findById(id).orElseThrow(() -> new NotFoundException("Category not found: " + id));
    }

    public Category create(UUID adminId, Category req) {
        access.requireAdmin(adminId);
        if (categories.existsByNameIgnoreCase(req.getName())) {
            throw new ConflictException("Category already exists: " + req.getName());
        }
        Category category = new Category();
        category.setName(req.getName().trim());
        return categories.save(category);
    }

    public Category update(UUID adminId, UUID id, Category req) {
        access.requireAdmin(adminId);
        Category category = get(id);
        category.setName(req.getName().trim());
        return category;
    }

    public void delete(UUID adminId, UUID id) {
        access.requireAdmin(adminId);
        categories.delete(get(id));
    }
}
