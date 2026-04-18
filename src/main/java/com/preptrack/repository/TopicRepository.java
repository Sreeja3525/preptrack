package com.preptrack.repository;

import com.preptrack.domain.Topic;
import com.preptrack.domain.TopicCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
    List<Topic> findByUserId(Long userId);
    List<Topic> findByUserIdAndCategory(Long userId, TopicCategory category);
    Optional<Topic> findByIdAndUserId(Long id, Long userId);
    boolean existsByNameAndUserId(String name, Long userId);
}
