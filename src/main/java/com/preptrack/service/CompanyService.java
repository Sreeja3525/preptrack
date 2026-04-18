package com.preptrack.service;

import com.preptrack.domain.Company;
import com.preptrack.domain.CompanyTopic;
import com.preptrack.domain.Topic;
import com.preptrack.domain.User;
import com.preptrack.dto.request.CompanyRequest;
import com.preptrack.dto.request.CompanyTopicRequest;
import com.preptrack.dto.response.CompanyResponse;
import com.preptrack.exception.DuplicateResourceException;
import com.preptrack.exception.ResourceNotFoundException;
import com.preptrack.repository.CompanyRepository;
import com.preptrack.repository.CompanyTopicRepository;
import com.preptrack.repository.TopicRepository;
import com.preptrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyTopicRepository companyTopicRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;

    @Transactional
    public CompanyResponse create(CompanyRequest request, Long userId) {
        if (companyRepository.existsByNameAndUserId(request.name(), userId)) {
            throw new DuplicateResourceException("Company already exists: " + request.name());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Company company = Company.builder()
                .name(request.name())
                .type(request.type())
                .difficulty(request.difficulty())
                .jobPortalUrl(request.jobPortalUrl())
                .user(user)
                .build();

        return CompanyResponse.from(companyRepository.save(company));
    }

    public List<CompanyResponse> getAllByUser(Long userId) {
        return companyRepository.findByUserId(userId)
                .stream()
                .map(CompanyResponse::from)
                .collect(Collectors.toList());
    }

    // Link a topic to a company with an importance level (used in readiness score weighting)
    @Transactional
    public void addRequiredTopic(Long companyId, CompanyTopicRequest request, Long userId) {
        Company company = companyRepository.findByIdAndUserId(companyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + companyId));

        Topic topic = topicRepository.findByIdAndUserId(request.topicId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + request.topicId()));

        CompanyTopic companyTopic = CompanyTopic.builder()
                .company(company)
                .topic(topic)
                .importanceLevel(request.importanceLevel())
                .build();

        companyTopicRepository.save(companyTopic);
    }

    @Transactional
    public void removeRequiredTopic(Long companyId, Long topicId, Long userId) {
        companyRepository.findByIdAndUserId(companyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + companyId));
        companyTopicRepository.deleteByCompanyIdAndTopicId(companyId, topicId);
    }
}
