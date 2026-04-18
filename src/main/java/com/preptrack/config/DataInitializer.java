package com.preptrack.config;

import com.preptrack.domain.*;
import com.preptrack.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds default topics when the app starts for the first time.
 * CommandLineRunner.run() is called once after the Spring context is fully initialized.
 *
 * These are the actual topics you need for Java backend interviews —
 * seeded automatically so you don't have to add them manually.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final CompanyRepository companyRepository;
    private final ApplicationRepository applicationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Only seed if the database is empty
        if (userRepository.count() > 0) {
            log.info("Database already seeded. Skipping initialization.");
            return;
        }

        log.info("Seeding database with demo data...");

        // Create a demo user
        User demoUser = User.builder()
                .name("Demo User")
                .email("demo@preptrack.com")
                .password(passwordEncoder.encode("demo123"))
                .build();
        userRepository.save(demoUser);

        // Seed all Java backend interview topics
        List<Topic> topics = List.of(
                // JAVA_CORE
                buildTopic("OOP Principles", TopicCategory.JAVA_CORE, "Encapsulation, Inheritance, Polymorphism, Abstraction", demoUser),
                buildTopic("Collections Framework", TopicCategory.JAVA_CORE, "List, Set, Map, Queue — internals and complexity", demoUser),
                buildTopic("Exception Handling", TopicCategory.JAVA_CORE, "Checked vs unchecked, custom exceptions, try-with-resources", demoUser),
                buildTopic("Generics", TopicCategory.JAVA_CORE, "Type parameters, bounded wildcards, type erasure", demoUser),
                buildTopic("Multithreading", TopicCategory.JAVA_CORE, "Thread lifecycle, synchronization, deadlock, thread pools", demoUser),
                buildTopic("String Handling", TopicCategory.JAVA_CORE, "String pool, StringBuilder, immutability", demoUser),

                // JAVA_8
                buildTopic("Lambda Expressions", TopicCategory.JAVA_8, "Functional interfaces, syntax, capturing variables", demoUser),
                buildTopic("Stream API", TopicCategory.JAVA_8, "filter, map, reduce, collect, flatMap, parallel streams", demoUser),
                buildTopic("Optional", TopicCategory.JAVA_8, "Null-safe operations, map, flatMap, orElse, orElseThrow", demoUser),
                buildTopic("Functional Interfaces", TopicCategory.JAVA_8, "Predicate, Function, Consumer, Supplier, BiFunction", demoUser),
                buildTopic("Method References", TopicCategory.JAVA_8, "Static, instance, constructor references", demoUser),
                buildTopic("Date/Time API", TopicCategory.JAVA_8, "LocalDate, LocalDateTime, ZonedDateTime, Duration, Period", demoUser),
                buildTopic("CompletableFuture", TopicCategory.JAVA_8, "supplyAsync, thenApply, thenCompose, allOf, join", demoUser),
                buildTopic("Default Methods", TopicCategory.JAVA_8, "Interface default methods, multiple inheritance conflict", demoUser),

                // SPRING_BOOT
                buildTopic("Dependency Injection", TopicCategory.SPRING_BOOT, "IoC container, @Component, @Service, @Bean, constructor injection", demoUser),
                buildTopic("Spring MVC", TopicCategory.SPRING_BOOT, "@RestController, @RequestMapping, request/response lifecycle", demoUser),
                buildTopic("Spring Boot Auto-configuration", TopicCategory.SPRING_BOOT, "How SpringBoot auto-configures beans from classpath", demoUser),
                buildTopic("Spring Profiles", TopicCategory.SPRING_BOOT, "@Profile, application-{profile}.yml, active profiles", demoUser),
                buildTopic("Spring Actuator", TopicCategory.SPRING_BOOT, "Health, metrics, info endpoints", demoUser),

                // SPRING_SECURITY
                buildTopic("JWT Authentication", TopicCategory.SPRING_SECURITY, "Token structure, signing, validation, filter chain", demoUser),
                buildTopic("Spring Security Filter Chain", TopicCategory.SPRING_SECURITY, "SecurityFilterChain, OncePerRequestFilter, SecurityContext", demoUser),
                buildTopic("Method-level Security", TopicCategory.SPRING_SECURITY, "@PreAuthorize, @PostAuthorize, @Secured", demoUser),

                // DATABASE
                buildTopic("JPA Relationships", TopicCategory.DATABASE, "@OneToMany, @ManyToOne, @ManyToMany, cascade, fetch types", demoUser),
                buildTopic("Spring Data JPA", TopicCategory.DATABASE, "Repository methods, @Query, JPQL, native queries", demoUser),
                buildTopic("Transactions", TopicCategory.DATABASE, "@Transactional, propagation, isolation levels, rollback rules", demoUser),
                buildTopic("Database Indexing", TopicCategory.DATABASE, "B-tree indexes, composite indexes, query optimization", demoUser),
                buildTopic("SQL Queries", TopicCategory.DATABASE, "Joins, subqueries, GROUP BY, window functions", demoUser),

                // SYSTEM_DESIGN
                buildTopic("REST API Design", TopicCategory.SYSTEM_DESIGN, "HTTP methods, status codes, versioning, pagination", demoUser),
                buildTopic("Microservices Basics", TopicCategory.SYSTEM_DESIGN, "Service decomposition, communication patterns, tradeoffs", demoUser),
                buildTopic("Caching", TopicCategory.SYSTEM_DESIGN, "Cache-aside, write-through, Redis basics, TTL", demoUser),

                // DSA
                buildTopic("Arrays & Strings", TopicCategory.DSA, "Two pointers, sliding window, string manipulation", demoUser),
                buildTopic("Trees & Graphs", TopicCategory.DSA, "BFS, DFS, binary search tree, shortest path", demoUser),
                buildTopic("Dynamic Programming", TopicCategory.DSA, "Memoization, tabulation, common patterns", demoUser)
        );

        topicRepository.saveAll(topics);

        // Seed some sample companies
        Company swiggy = Company.builder()
                .name("Swiggy").type(CompanyType.PRODUCT)
                .difficulty(InterviewDifficulty.HARD).user(demoUser).build();
        Company tcs = Company.builder()
                .name("TCS").type(CompanyType.SERVICE)
                .difficulty(InterviewDifficulty.MEDIUM).user(demoUser).build();
        companyRepository.saveAll(List.of(swiggy, tcs));

        // Seed a sample application
        Application app = Application.builder()
                .user(demoUser).company(tcs).role("Java Backend Developer")
                .status(ApplicationStatus.APPLIED).appliedDate(LocalDate.now())
                .lastUpdated(LocalDateTime.now()).notes("Applied via TCS careers portal").build();
        applicationRepository.save(app);

        log.info("Seeded {} topics, 2 companies, 1 application for demo@preptrack.com (password: demo123)",
                topics.size());
    }

    private Topic buildTopic(String name, TopicCategory category, String description, User user) {
        return Topic.builder().name(name).category(category).description(description).user(user).build();
    }
}
