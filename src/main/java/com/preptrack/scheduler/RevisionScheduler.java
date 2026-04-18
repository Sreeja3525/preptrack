package com.preptrack.scheduler;

import com.preptrack.domain.User;
import com.preptrack.dto.response.RevisionScheduleResponse;
import com.preptrack.repository.UserRepository;
import com.preptrack.service.SpacedRepetitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduled jobs that run automatically without any HTTP trigger.
 *
 * @Scheduled(cron = "0 0 8 * * *") means:
 *   second=0, minute=0, hour=8, day=any, month=any, weekday=any
 *   → fires every day at 8:00 AM
 *
 * The cron format in Spring is: second minute hour day month weekday
 * (6 fields, unlike Unix cron's 5 fields — Spring adds the seconds field)
 *
 * @EnableScheduling in PreptrackApplication.java is required for this to work.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RevisionScheduler {

    private final UserRepository userRepository;
    private final SpacedRepetitionService spacedRepetitionService;

    // Runs every day at 8:00 AM — logs the revision digest for each user
    // In a production app this would send push notifications or emails
    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyRevisionDigest() {
        log.info("=== Daily Revision Digest — Running at 8 AM ===");

        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            List<RevisionScheduleResponse> dueTopics =
                    spacedRepetitionService.getTodaysRevisionList(user.getId());

            if (dueTopics.isEmpty()) {
                log.info("User [{}] — No topics due for revision today. Great job!", user.getName());
            } else {
                log.info("User [{}] — {} topic(s) due for revision today:", user.getName(), dueTopics.size());
                dueTopics.forEach(topic ->
                        log.info("  → [{}] {} (confidence: {}, status: {})",
                                topic.getCategory(), topic.getTopicName(),
                                topic.getConfidenceScore(), topic.getStatus())
                );
            }
        }
    }

    // Also runs every Sunday at 9:00 PM — a weekly summary reminder
    @Scheduled(cron = "0 0 21 * * SUN")
    public void sendWeeklySummaryReminder() {
        log.info("=== Weekly Summary Reminder — Sunday 9 PM ===");
        log.info("Check your weekly report at GET /api/reports/weekly");
    }
}
