package com.spartanmarketplace.api.controller;

import com.spartanmarketplace.api.entity.MeetupLocation;
import com.spartanmarketplace.api.service.MeetupLocationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/meetup-locations")
@RequiredArgsConstructor
public class MeetupLocationController {

    private final MeetupLocationService service;

    @GetMapping
    public List<MeetupLocation> list() {
        return service.listActive();
    }

    @GetMapping("/{id}")
    public MeetupLocation get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MeetupLocation create(@RequestHeader("X-Admin-Id") UUID adminId,
                                 @Valid @RequestBody MeetupLocation req) {
        return service.create(adminId, req);
    }

    @PutMapping("/{id}")
    public MeetupLocation update(@RequestHeader("X-Admin-Id") UUID adminId, @PathVariable UUID id,
                                 @Valid @RequestBody MeetupLocation req) {
        return service.update(adminId, id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader("X-Admin-Id") UUID adminId, @PathVariable UUID id) {
        service.delete(adminId, id);
    }
}
