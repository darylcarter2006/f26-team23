package com.spartanmarketplace.api.controller;

import com.spartanmarketplace.api.dto.UserDto;
import com.spartanmarketplace.api.entity.User;
import com.spartanmarketplace.api.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto.Response create(@Valid @RequestBody UserDto.Request req) {
        return service.create(req);
    }

    @GetMapping
    public List<UserDto.Response> list(@RequestHeader("X-Admin-Id") UUID adminId,
                                       @RequestParam(required = false) User.Status status) {
        return service.list(adminId, status);
    }

    @GetMapping("/{id}")
    public UserDto.Response get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    public UserDto.Response update(@PathVariable UUID id, @Valid @RequestBody UserDto.Request req) {
        return service.update(id, req);
    }

    @PostMapping("/{id}/verify-edu")
    public UserDto.Response verifyEdu(@PathVariable UUID id) {
        return service.verifyEdu(id);
    }

    @PutMapping("/{id}/status")
    public UserDto.Response updateStatus(@RequestHeader("X-Admin-Id") UUID adminId, @PathVariable UUID id,
                                         @Valid @RequestBody UserDto.StatusRequest req) {
        return service.updateStatus(adminId, id, req.status());
    }
}
