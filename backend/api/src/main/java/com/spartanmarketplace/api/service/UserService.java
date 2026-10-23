package com.spartanmarketplace.api.service;

import com.spartanmarketplace.api.dto.UserDto;
import com.spartanmarketplace.api.entity.User;
import com.spartanmarketplace.api.exception.BadRequestException;
import com.spartanmarketplace.api.exception.ConflictException;
import com.spartanmarketplace.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository users;
    private final AccessService access;

    // comma-separated emails that become SysAdmins on signup (resolves the "who is an admin" open issue)
    @Value("${app.admin-emails:}")
    private String adminEmails;

    public UserDto.Response create(UserDto.Request req) {
        String email = req.email().trim().toLowerCase();
        if (!email.endsWith(".edu")) {
            throw new BadRequestException("A campus .edu email is required");
        }
        if (users.existsByEmail(email)) {
            throw new ConflictException("Email already registered: " + email);
        }
        User user = new User();
        user.setEmail(email);
        user.setDisplayName(req.displayName());
        user.setBio(req.bio());
        if (adminEmailSet().contains(email)) {
            user.setRole(User.Role.ADMIN);
            user.setEduVerifiedAt(Instant.now());
        }
        return UserDto.Response.from(users.save(user));
    }

    @Transactional(readOnly = true)
    public UserDto.Response get(UUID id) {
        return UserDto.Response.from(access.getUser(id));
    }

    @Transactional(readOnly = true)
    public List<UserDto.Response> list(UUID adminId, User.Status status) {
        access.requireAdmin(adminId);
        List<User> found = status == null ? users.findAll() : users.findByStatus(status);
        return found.stream().map(UserDto.Response::from).toList();
    }

    public UserDto.Response update(UUID id, UserDto.Request req) {
        User user = access.requireActive(id);
        String email = req.email().trim().toLowerCase();
        if (!email.equals(user.getEmail())) {
            if (!email.endsWith(".edu")) {
                throw new BadRequestException("A campus .edu email is required");
            }
            if (users.existsByEmail(email)) {
                throw new ConflictException("Email already registered: " + email);
            }
            user.setEmail(email);
            user.setEduVerifiedAt(null);
        }
        user.setDisplayName(req.displayName());
        user.setBio(req.bio());
        return UserDto.Response.from(user);
    }

    // stands in for the emailed confirmation-code flow
    public UserDto.Response verifyEdu(UUID id) {
        User user = access.requireActive(id);
        if (user.getEduVerifiedAt() == null) {
            user.setEduVerifiedAt(Instant.now());
        }
        return UserDto.Response.from(user);
    }

    public UserDto.Response updateStatus(UUID adminId, UUID id, User.Status status) {
        access.requireAdmin(adminId);
        User user = access.getUser(id);
        if (user.getRole() == User.Role.ADMIN) {
            throw new BadRequestException("Admin accounts cannot be restricted or banned");
        }
        user.setStatus(status);
        return UserDto.Response.from(user);
    }

    private Set<String> adminEmailSet() {
        return Arrays.stream(adminEmails.split(","))
                .map(s -> s.trim().toLowerCase())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}
