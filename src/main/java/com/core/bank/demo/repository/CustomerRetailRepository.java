package com.core.bank.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.core.bank.demo.entity.CustomerRetail;

public interface CustomerRetailRepository extends JpaRepository<CustomerRetail, String> {
}
