package com.preptrack.service;

import com.preptrack.domain.Company;
import com.preptrack.domain.MockInterview;
import com.preptrack.domain.Topic;
import com.preptrack.domain.User;
import com.preptrack.dto.request.MockInterviewRequest;
import com.preptrack.dto.response.MockInterviewResponse;
import com.preptrack.exception.ResourceNotFoundException;
import com.preptrack.repository.CompanyRepository;
import com.preptrack.repository.MockInterviewRepository;
import com.preptrack.repository.TopicRepository;
import com.preptrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MockInterviewService {

    private final MockInterviewRepository mockInterviewRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final TopicRepository topicRepository;

    @Transactional
    public MockInterviewResponse log(MockInterviewRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Company company = null;
        if (request.companyId() != null) {
            company = companyRepository.findByIdAndUserId(request.companyId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + request.companyId()));
        }

        List<Topic> topics = Collections.emptyList();
        if (request.topicIds() != null && !request.topicIds().isEmpty()) {
            // Stream over requested topic IDs, fetch each, collect into list
            topics = request.topicIds().stream()
                    .map(id -> topicRepository.findByIdAndUserId(id, userId)
                            .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + id)))
                    .collect(Collectors.toList());
        }

        MockInterview interview = MockInterview.builder()
                .user(user)
                .company(company)
                .interviewedAt(request.interviewedAt())
                .overallScore(request.overallScore())
                .interviewer(request.interviewer())
                .topicsAsked(topics)
                .weakAreas(request.weakAreas())
                .notes(request.notes())
                .build();

        return MockInterviewResponse.from(mockInterviewRepository.save(interview));
    }

    public List<MockInterviewResponse> getAllByUser(Long userId) {
        return mockInterviewRepository.findByUserIdOrderByInterviewedAtDesc(userId)
                .stream()
                .map(MockInterviewResponse::from)
                .collect(Collectors.toList());
    }
}
