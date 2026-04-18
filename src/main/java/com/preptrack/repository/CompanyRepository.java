package com.preptrack.repository;

import com.preptrack.domain.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    List<Company> findByUserId(Long userId);
    Optional<Company> findByIdAndUserId(Long id, Long userId);
    boolean existsByNameAndUserId(String name, Long userId);
}
