package com.spartanmarketplace.api.controller;

import com.spartanmarketplace.api.dto.StatisticsDto;
import com.spartanmarketplace.api.service.StatisticsService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService service;

    @GetMapping
    public StatisticsDto.Platform platform(@RequestHeader("X-Admin-Id") UUID adminId) {
        return service.platform(adminId);
    }
}
