package com.vetivet.config;

import com.vetivet.model.*;
import com.vetivet.model.Role.ERole;
import com.vetivet.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initRoles();
        initAdminUser();
    }

    private void initRoles() {
        for (ERole erole : ERole.values()) {
            if (roleRepository.findByName(erole).isEmpty()) {
                Role role = Role.builder().name(erole).build();
                roleRepository.save(role);
                log.info("Created role: {}", erole.name());
            }
        }
    }

    private void initAdminUser() {
        String adminEmail = "admin@vetivet.com";
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));
            Role superAdminRole = roleRepository.findByName(ERole.ROLE_SUPER_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Super admin role not found"));

            User admin = User.builder()
                    .username("admin")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("Admin")
                    .lastName("Vetivet")
                    .active(true)
                    .roles(Set.of(adminRole, superAdminRole))
                    .build();

            userRepository.save(admin);
            log.info("Created default admin user: admin@vetivet.com / admin123");
        }
    }
}
