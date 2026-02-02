package com.core.bank.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import lombok.Getter;
import lombok.Setter;

@Entity(name = "customer_retail")
@Getter
@Setter
public class CustomerRetail extends BaseEntity {

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "customer_code")
    private String customerCode;

    @Column(name = "address")
    private String address;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "province")
    private String province;

    @Column(name = "city")
    private String city;
}
