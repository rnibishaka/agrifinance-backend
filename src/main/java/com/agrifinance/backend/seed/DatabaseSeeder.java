package com.agrifinance.backend.seed;

import com.agrifinance.backend.model.*;
import com.agrifinance.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class DatabaseSeeder {
    private final UserRepository userRepository;
    private final LoanRepository loanRepository;
    private final ProjectRepository projectRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            if (userRepository.count() == 0) {
                User admin = User.builder()
                        .id(UUID.randomUUID())
                        .email("admin@agrifinance.com")
                        .password(passwordEncoder.encode("admin123"))
                        .firstName("Admin")
                        .lastName("User")
                        .phone("1234567890")
                        .farmType("N/A")
                        .farmSize(0.0)
                        .location("HQ")
                        .role(Role.ADMIN)
                        .status("ACTIVE")
                        .address(Address.builder().street("1 Admin St").city("AgriCity").state("AgriState").zipCode("00000").build())
                        .build();
                User user = User.builder()
                        .id(UUID.randomUUID())
                        .email("user@agrifinance.com")
                        .password(passwordEncoder.encode("user123"))
                        .firstName("John")
                        .lastName("Farmer")
                        .phone("0987654321")
                        .farmType("CROP")
                        .farmSize(10.5)
                        .location("Village")
                        .role(Role.USER)
                        .status("ACTIVE")
                        .address(Address.builder().street("2 Farm Rd").city("AgriTown").state("AgriState").zipCode("11111").build())
                        .build();
                userRepository.saveAll(List.of(admin, user));
            }
            // Add similar logic for loans and projects if needed
        };
    }
}
