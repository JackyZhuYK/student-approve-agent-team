package com.agentteam.studentapproval.config;

import com.agentteam.studentapproval.entity.User;
import com.agentteam.studentapproval.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create admin user
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                .username("admin")
                .email("admin@example.com")
                .passwordHash(passwordEncoder.encode("admin123"))
                .displayName("System Administrator")
                .role(User.Role.ADMIN)
                .department("IT")
                .enabled(true)
                .build();
            userRepository.save(admin);
            System.out.println("Created admin user");
        }

        // Create approver user
        if (!userRepository.existsByUsername("approver")) {
            User approver = User.builder()
                .username("approver")
                .email("approver@example.com")
                .passwordHash(passwordEncoder.encode("approver123"))
                .displayName("Default Approver")
                .role(User.Role.APPROVER)
                .department("Academic Affairs")
                .enabled(true)
                .build();
            userRepository.save(approver);
            System.out.println("Created approver user");
        }

        // Create student user
        if (!userRepository.existsByUsername("student")) {
            User student = User.builder()
                .username("student")
                .email("student@example.com")
                .passwordHash(passwordEncoder.encode("student123"))
                .displayName("测试学生")
                .role(User.Role.STUDENT)
                .department("计算机系")
                .studentId("2024001")
                .enabled(true)
                .build();
            userRepository.save(student);
            System.out.println("Created student user");
        }
    }
}
