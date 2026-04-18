package com.preptrack.service;

import com.preptrack.domain.StudyLog;
import com.preptrack.domain.Topic;
import com.preptrack.domain.User;
import com.preptrack.dto.request.StudyLogRequest;
import com.preptrack.dto.response.StudyLogResponse;
import com.preptrack.exception.ResourceNotFoundException;
import com.preptrack.repository.StudyLogRepository;
import com.preptrack.repository.TopicRepository;
import com.preptrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyLogService {

    private final StudyLogRepository studyLogRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;

    @Transactional
    public StudyLogResponse log(StudyLogRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // findByIdAndUserId ensures you can only log study for your own topics
        Topic topic = topicRepository.findByIdAndUserId(request.topicId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + request.topicId()));

        StudyLog studyLog = StudyLog.builder()
                .user(user)
                .topic(topic)
                .confidenceScore(request.confidenceScore())
                .studiedAt(request.studiedAt())
                .durationMinutes(request.durationMinutes())
                .notes(request.notes())
                .build();

        return StudyLogResponse.from(studyLogRepository.save(studyLog));
    }

    public List<StudyLogResponse> getAllByUser(Long userId) {
        return studyLogRepository.findByUserIdOrderByStudiedAtDesc(userId)
                .stream()
                .map(StudyLogResponse::from)
                .collect(Collectors.toList());
    }

    public List<StudyLogResponse> getByTopic(Long topicId, Long userId) {
        return studyLogRepository.findByUserIdAndTopicIdOrderByStudiedAtDesc(userId, topicId)
                .stream()
                .map(StudyLogResponse::from)
                .collect(Collectors.toList());
    }

    public Integer getTotalStudyMinutes(Long userId) {
        return studyLogRepository.getTotalStudyMinutesByUserId(userId);
    }
}
