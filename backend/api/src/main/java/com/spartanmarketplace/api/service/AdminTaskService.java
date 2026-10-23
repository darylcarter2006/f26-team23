package com.spartanmarketplace.api.service;

import com.spartanmarketplace.api.dto.AdminTaskDto;
import com.spartanmarketplace.api.entity.AdminTask;
import com.spartanmarketplace.api.exception.NotFoundException;
import com.spartanmarketplace.api.repository.AdminTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminTaskService {

    private final AdminTaskRepository tasks;
    private final AccessService access;

    @Transactional(readOnly = true)
    public List<AdminTaskDto.Response> list(UUID adminId, AdminTask.Status status) {
        access.requireAdmin(adminId);
        List<AdminTask> found = status == null ? tasks.findAllByOrderByCreatedAtDesc()
                : tasks.findByStatusOrderByCreatedAtDesc(status);
        return found.stream().map(AdminTaskDto.Response::from).toList();
    }

    public AdminTaskDto.Response resolve(UUID adminId, UUID id) {
        access.requireAdmin(adminId);
        AdminTask task = tasks.findById(id).orElseThrow(() -> new NotFoundException("Admin task not found: " + id));
        task.setStatus(AdminTask.Status.RESOLVED);
        return AdminTaskDto.Response.from(task);
    }
}
