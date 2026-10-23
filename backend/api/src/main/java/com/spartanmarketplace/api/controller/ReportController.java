package com.spartanmarketplace.api.controller;

import com.spartanmarketplace.api.dto.ReportDto;
import com.spartanmarketplace.api.entity.Report;
import com.spartanmarketplace.api.service.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService service;

    @PostMapping("/reports")
    @ResponseStatus(HttpStatus.CREATED)
    public ReportDto.Response create(@Valid @RequestBody ReportDto.Request req) {
        return service.create(req);
    }

    @GetMapping("/reports")
    public List<ReportDto.Response> list(@RequestHeader("X-Admin-Id") UUID adminId,
                                         @RequestParam(required = false) Report.Status status) {
        return service.list(adminId, status);
    }

    @GetMapping("/reports/{id}")
    public ReportDto.Response get(@RequestHeader("X-Admin-Id") UUID adminId, @PathVariable UUID id) {
        return service.get(adminId, id);
    }

    @PutMapping("/reports/{id}")
    public ReportDto.Response updateStatus(@RequestHeader("X-Admin-Id") UUID adminId, @PathVariable UUID id,
                                           @Valid @RequestBody ReportDto.StatusRequest req) {
        return service.updateStatus(adminId, id, req.status());
    }

    @GetMapping("/users/{userId}/reports")
    public List<ReportDto.Response> history(@RequestHeader("X-Admin-Id") UUID adminId, @PathVariable UUID userId) {
        return service.historyForUser(adminId, userId);
    }
}
