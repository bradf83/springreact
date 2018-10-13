package com.bradf.springreact;
import com.bradf.springreact.model.Role;
import com.bradf.springreact.model.RoleName;
import com.bradf.springreact.model.User;
import com.bradf.springreact.repository.RoleRepository;
import com.bradf.springreact.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

//TODO: Replace with Flyway

@Component
public class Initializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Initializer(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create Roles
        Stream.of(RoleName.ROLE_ADMIN, RoleName.ROLE_USER).forEach(name ->
                roleRepository.save(new Role(name))
        );

        // Create users
        User testUser = new User();
        testUser.setName("test");
        testUser.setUsername("test");
        testUser.setEmail("test@example.com");
        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository.findByName(RoleName.ROLE_USER).get());
        testUser.setRoles(roles);
        testUser.setPassword(passwordEncoder.encode("secret"));
        this.userRepository.save(testUser);
    }
}
