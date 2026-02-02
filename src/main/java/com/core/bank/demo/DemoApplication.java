package com.core.bank.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.core.bank.demo.entity.User;
import com.core.bank.demo.enums.Role;
import com.core.bank.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@SpringBootApplication
@EnableAspectJAutoProxy
@RequiredArgsConstructor
public class DemoApplication implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        User user = new User();
        user.setUsername("admin");
        user.setPassword(passwordEncoder.encode("1"));
        user.setRole(Role.TELLER);
        userRepository.save(user);
    }
}
