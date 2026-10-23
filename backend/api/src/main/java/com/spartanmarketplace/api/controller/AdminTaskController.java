package com.spartanmarketplace.api.controller;

import com.spartanmarketplace.api.dto.AdminTaskDto;
import com.spartanmarketplace.api.entity.AdminTask;
import com.spartanmarketplace.api.service.AdminTaskService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin-tasks")
@RequiredArgsConstructor
public class AdminTaskController {

    private final AdminTaskService service;

    @GetMapping
    public List<AdminTaskDto.Response> list(@RequestHeader("X-Admin-Id") UUID adminId,
                                            @RequestParam(required = false) AdminTask.Status status) {
        return service.list(adminId, status);
    }

    @PutMapping("/{id}/resolve")
    public AdminTaskDto.Response resolve(@RequestHeader("X-Admin-Id") UUID adminId, @PathVariable UUID id) {
        return service.resolve(adminId, id);
    }
}
