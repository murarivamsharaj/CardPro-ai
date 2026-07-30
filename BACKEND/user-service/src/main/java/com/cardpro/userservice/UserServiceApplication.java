package com.cardpro.userservice;

import com.cardpro.userservice.entity.User;
import com.cardpro.userservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    // === UPDATED TO USE firstName AND lastName ===
    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                User testUser = new User();
                testUser.setEmail("student@cardpro.ai");
                testUser.setFirstName("Ch");
                testUser.setLastName("Mur");
                testUser.setPhoneNumber("9876543210");
                testUser.setRole("ROLE_USER");
                testUser.setActive(true);

                userRepository.save(testUser);
                System.out.println(">>> Sample User inserted with ID: " + testUser.getId());
            }
        };
    }
}