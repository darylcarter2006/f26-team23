package com.spartanmarketplace.api.service;

import com.spartanmarketplace.api.dto.ProductDto;
import com.spartanmarketplace.api.entity.Category;
import com.spartanmarketplace.api.entity.Product;
import com.spartanmarketplace.api.entity.ProductPhoto;
import com.spartanmarketplace.api.entity.User;
import com.spartanmarketplace.api.entity.Vendor;
import com.spartanmarketplace.api.exception.BadRequestException;
import com.spartanmarketplace.api.exception.ConflictException;
import com.spartanmarketplace.api.exception.NotFoundException;
import com.spartanmarketplace.api.repository.CategoryRepository;
import com.spartanmarketplace.api.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository products;
    private final CategoryRepository categories;
    private final VendorService vendorService;
    private final AccessService access;

    // public browse: only active products from vendors in good standing (US-2)
    @Transactional(readOnly = true)
    public List<ProductDto.Response> search(UUID categoryId, Integer minPrice, Integer maxPrice, String q,
                                            String sort, UUID vendorId) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            p.add(cb.equal(root.get("status"), Product.Status.ACTIVE));
            p.add(cb.equal(root.join("vendor").join("user").get("status"), User.Status.ACTIVE));
            if (categoryId != null) {
                p.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (vendorId != null) {
                p.add(cb.equal(root.get("vendor").get("id"), vendorId));
            }
            if (minPrice != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                p.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                p.add(cb.or(cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(cb.coalesce(root.<String>get("description"), "")), like)));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
        return products.findAll(spec, sortFor(sort)).stream().map(ProductDto.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public ProductDto.Response get(UUID id) {
        return ProductDto.Response.from(find(id));
    }

    public ProductDto.Response create(ProductDto.Request req) {
        if (req.vendorId() == null) {
            throw new BadRequestException("vendorId is required");
        }
        Vendor vendor = vendorService.find(req.vendorId());
        access.requireTeamMember(vendor, req.actorId());
        Product product = new Product();
        product.setVendor(vendor);
        apply(product, req);
        return ProductDto.Response.from(products.save(product));
    }

    public ProductDto.Response update(UUID id, ProductDto.Request req) {
        Product product = find(id);
        access.requireTeamMember(product.getVendor(), req.actorId());
        apply(product, req);
        return ProductDto.Response.from(product);
    }

    // admin moderation (US-33); products with orders or reviews on file must be hidden instead
    public void delete(UUID adminId, UUID id) {
        access.requireAdmin(adminId);
        Product product = find(id);
        try {
            products.delete(product);
            products.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Product has orders or reviews on file; hide the vendor's products instead");
        }
    }

    public Product find(UUID id) {
        return products.findById(id).orElseThrow(() -> new NotFoundException("Product not found: " + id));
    }

    private void apply(Product product, ProductDto.Request req) {
        product.setTitle(req.title());
        product.setDescription(req.description());
        product.setPrice(req.price());
        product.setStockCount(req.stockCount());
        product.setCategory(req.categoryId() == null ? null : findCategory(req.categoryId()));
        if (req.photoUrls() != null) {
            product.getPhotos().clear();
            int order = 0;
            for (String url : req.photoUrls()) {
                ProductPhoto photo = new ProductPhoto();
                photo.setProduct(product);
                photo.setUrl(url);
                photo.setSortOrder(order++);
                product.getPhotos().add(photo);
            }
        }
    }

    private Category findCategory(UUID id) {
        return categories.findById(id).orElseThrow(() -> new NotFoundException("Category not found: " + id));
    }

    private Sort sortFor(String sort) {
        if (sort == null) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        return switch (sort) {
            case "priceAsc" -> Sort.by(Sort.Direction.ASC, "price");
            case "priceDesc" -> Sort.by(Sort.Direction.DESC, "price");
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> throw new BadRequestException("sort must be newest, priceAsc or priceDesc");
        };
    }
}
