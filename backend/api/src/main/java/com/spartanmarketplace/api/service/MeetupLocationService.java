package com.spartanmarketplace.api.service;

import com.spartanmarketplace.api.entity.MeetupLocation;
import com.spartanmarketplace.api.exception.NotFoundException;
import com.spartanmarketplace.api.repository.MeetupLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MeetupLocationService {

    private final MeetupLocationRepository locations;
    private final AccessService access;

    // customers only see active spots
    @Transactional(readOnly = true)
    public List<MeetupLocation> listActive() {
        return locations.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public MeetupLocation get(UUID id) {
        return locations.findById(id).orElseThrow(() -> new NotFoundException("Meetup location not found: " + id));
    }

    public MeetupLocation create(UUID adminId, MeetupLocation req) {
        access.requireAdmin(adminId);
        MeetupLocation location = new MeetupLocation();
        location.setName(req.getName().trim());
        location.setDescription(req.getDescription());
        location.setActive(req.isActive());
        return locations.save(location);
    }

    public MeetupLocation update(UUID adminId, UUID id, MeetupLocation req) {
        access.requireAdmin(adminId);
        MeetupLocation location = get(id);
        location.setName(req.getName().trim());
        location.setDescription(req.getDescription());
        location.setActive(req.isActive());
        return location;
    }

    public void delete(UUID adminId, UUID id) {
        access.requireAdmin(adminId);
        locations.delete(get(id));
    }
}
