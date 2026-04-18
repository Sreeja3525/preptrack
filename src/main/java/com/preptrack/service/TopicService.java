package com.preptrack.service;

import com.preptrack.domain.Topic;
import com.preptrack.domain.TopicCategory;
import com.preptrack.domain.User;
import com.preptrack.dto.request.TopicRequest;
import com.preptrack.dto.response.TopicResponse;
import com.preptrack.exception.DuplicateResourceException;
import com.preptrack.exception.ResourceNotFoundException;
import com.preptrack.repository.TopicRepository;
import com.preptrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;
    private final UserRepository userRepository;

    @Transactional
    public TopicResponse create(TopicRequest request, Long userId) {
        if (topicRepository.existsByNameAndUserId(request.name(), userId)) {
            throw new DuplicateResourceException("Topic already exists: " + request.name());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Topic topic = Topic.builder()
                .name(request.name())
                .category(request.category())
                .description(request.description())
                .user(user)
                .build();

        return TopicResponse.from(topicRepository.save(topic));
    }

    public List<TopicResponse> getAllByUser(Long userId) {
        // Stream API: transform each Topic entity into a TopicResponse DTO
        return topicRepository.findByUserId(userId)
                .stream()
                .map(TopicResponse::from)   // method reference instead of lambda
                .collect(Collectors.toList());
    }

    public List<TopicResponse> getByCategory(Long userId, TopicCategory category) {
        return topicRepository.findByUserIdAndCategory(userId, category)
                .stream()
                .map(TopicResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public TopicResponse update(Long topicId, TopicRequest request, Long userId) {
        Topic topic = topicRepository.findByIdAndUserId(topicId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + topicId));

        topic.setName(request.name());
        topic.setCategory(request.category());
        topic.setDescription(request.description());

        return TopicResponse.from(topicRepository.save(topic));
    }

    @Transactional
    public void delete(Long topicId, Long userId) {
        Topic topic = topicRepository.findByIdAndUserId(topicId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + topicId));
        topicRepository.delete(topic);
    }
}
