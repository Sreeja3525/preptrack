package com.preptrack.dto.response;

import com.preptrack.domain.Company;
import com.preptrack.domain.CompanyType;
import com.preptrack.domain.InterviewDifficulty;

public record CompanyResponse(
        Long id,
        String name,
        CompanyType type,
        InterviewDifficulty difficulty,
        String jobPortalUrl,
        int requiredTopicCount
) {
    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getType(),
                company.getDifficulty(),
                company.getJobPortalUrl(),
                company.getCompanyTopics().size()
        );
    }
}
