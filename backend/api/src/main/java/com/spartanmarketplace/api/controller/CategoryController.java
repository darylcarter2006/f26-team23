package com.spartanmarketplace.api.controller;

import com.spartanmarketplace.api.entity.Category;
import com.spartanmarketplace.api.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService service;

    @GetMapping
    public List<Category> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public Category get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Category create(@RequestHeader("X-Admin-Id") UUID adminId, @Valid @RequestBody Category req) {
        return service.create(adminId, req);
    }

    @PutMapping("/{id}")
    public Category update(@RequestHeader("X-Admin-Id") UUID adminId, @PathVariable UUID id,
                           @Valid @RequestBody Category req) {
        return service.update(adminId, id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader("X-Admin-Id") UUID adminId, @PathVariable UUID id) {
        service.delete(adminId, id);
    }
}
