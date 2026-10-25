package qltv.com.config;

import qltv.com.entity.User;
import qltv.com.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(User.Role.ADMIN);
                admin.setEmail("admin@gmail.com");
                admin.setEnabled(true);
                admin.setFullName("Administrator");
                userRepository.save(admin);
                System.out.println("Admin created!");
            }
        };
    }
}