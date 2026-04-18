package com.preptrack.repository;

import com.preptrack.domain.CompanyTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyTopicRepository extends JpaRepository<CompanyTopic, Long> {

    List<CompanyTopic> findByCompanyId(Long companyId);

    // Used by ReadinessScoreService.calculateForAllCompanies()
    // Returns distinct company IDs that belong to this user (via company.user.id)
    @Query("SELECT DISTINCT ct.company.id FROM CompanyTopic ct WHERE ct.company.user.id = :userId")
    List<Long> findDistinctCompanyIdsByUserId(Long userId);

    void deleteByCompanyIdAndTopicId(Long companyId, Long topicId);
}
