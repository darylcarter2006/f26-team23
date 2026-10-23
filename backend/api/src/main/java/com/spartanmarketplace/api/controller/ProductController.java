package com.spartanmarketplace.api.controller;

import com.spartanmarketplace.api.dto.BuyRequestDto;
import com.spartanmarketplace.api.dto.ProductDto;
import com.spartanmarketplace.api.dto.ReviewDto;
import com.spartanmarketplace.api.service.BuyRequestService;
import com.spartanmarketplace.api.service.ProductService;
import com.spartanmarketplace.api.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;
    private final BuyRequestService buyRequests;
    private final ReviewService reviews;

    // sort: newest (default), priceAsc, priceDesc
    @GetMapping
    public List<ProductDto.Response> search(@RequestParam(required = false) UUID categoryId,
                                            @RequestParam(required = false) Integer minPrice,
                                            @RequestParam(required = false) Integer maxPrice,
                                            @RequestParam(required = false) String q,
                                            @RequestParam(required = false) String sort,
                                            @RequestParam(required = false) UUID vendorId) {
        return service.search(categoryId, minPrice, maxPrice, q, sort, vendorId);
    }

    @GetMapping("/{id}")
    public ProductDto.Response get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDto.Response create(@Valid @RequestBody ProductDto.Request req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    public ProductDto.Response update(@PathVariable UUID id, @Valid @RequestBody ProductDto.Request req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader("X-Admin-Id") UUID adminId, @PathVariable UUID id) {
        service.delete(adminId, id);
    }

    @GetMapping("/{id}/reviews")
    public List<ReviewDto.Response> reviews(@PathVariable UUID id) {
        return reviews.forProduct(id);
    }

    @PostMapping("/{id}/buy-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public BuyRequestDto.Response requestToBuy(@PathVariable UUID id,
                                               @Valid @RequestBody BuyRequestDto.CreateRequest req) {
        return buyRequests.create(id, req);
    }
}
