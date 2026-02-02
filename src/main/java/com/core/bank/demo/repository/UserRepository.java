package com.core.bank.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.core.bank.demo.entity.User;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);
}
