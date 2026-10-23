package com.spartanmarketplace.api.repository;

import com.spartanmarketplace.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmail(String email);

    List<User> findByStatus(User.Status status);

    long countByCreatedAtAfter(Instant since);

    @Query("select cast(u.createdAt as LocalDate), count(u) from User u where u.createdAt >= :since "
            + "group by cast(u.createdAt as LocalDate) order by cast(u.createdAt as LocalDate)")
    List<Object[]> signupsPerDaySince(Instant since);
}
