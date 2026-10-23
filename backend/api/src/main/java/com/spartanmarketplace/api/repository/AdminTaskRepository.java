package com.spartanmarketplace.api.repository;

import com.spartanmarketplace.api.entity.AdminTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdminTaskRepository extends JpaRepository<AdminTask, UUID> {

    List<AdminTask> findByStatusOrderByCreatedAtDesc(AdminTask.Status status);

    List<AdminTask> findAllByOrderByCreatedAtDesc();

    boolean existsByTypeAndVendorIdAndStatus(AdminTask.Type type, UUID vendorId, AdminTask.Status status);

    boolean existsByTypeAndUserIdAndStatus(AdminTask.Type type, UUID userId, AdminTask.Status status);
}
