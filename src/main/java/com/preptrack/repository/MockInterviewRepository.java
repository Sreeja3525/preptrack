package com.preptrack.repository;

import com.preptrack.domain.MockInterview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MockInterviewRepository extends JpaRepository<MockInterview, Long> {
    List<MockInterview> findByUserIdOrderByInterviewedAtDesc(Long userId);
    Optional<MockInterview> findByIdAndUserId(Long id, Long userId);
}
