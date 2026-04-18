package com.preptrack.repository;

import com.preptrack.domain.Application;
import com.preptrack.domain.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByUserIdOrderByLastUpdatedDesc(Long userId);

    Optional<Application> findByIdAndUserId(Long id, Long userId);

    List<Application> findByUserIdAndStatus(Long userId, ApplicationStatus status);

    // Used by ReportService to find applications that changed status this week
    List<Application> findByUserIdAndLastUpdatedAfter(Long userId, LocalDateTime since);
}
