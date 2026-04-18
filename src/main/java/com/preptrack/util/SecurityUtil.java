package com.preptrack.util;

import com.preptrack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

// Static utility to extract the currently authenticated user's ID
// Uses Spring's SecurityContextHolder which stores the auth in a ThreadLocal
@Component
public class SecurityUtil {

    private static UserRepository userRepository;

    // Spring injects the bean via setter, we store it statically so it's usable in static methods
    @Autowired
    public void setUserRepository(UserRepository repo) {
        SecurityUtil.userRepository = repo;
    }

    public static Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in DB"))
                .getId();
    }

    public static String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
