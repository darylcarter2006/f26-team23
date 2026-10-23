package com.spartanmarketplace.api.repository;

import com.spartanmarketplace.api.entity.MeetupLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MeetupLocationRepository extends JpaRepository<MeetupLocation, UUID> {

    List<MeetupLocation> findByActiveTrue();
}
