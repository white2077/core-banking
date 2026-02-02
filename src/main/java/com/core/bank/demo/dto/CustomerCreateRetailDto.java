package com.core.bank.demo.dto;

import java.io.Serializable;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CustomerCreateRetailDto implements Serializable {

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    private String lastName;

    @Size(max = 50, message = "Customer code must not exceed 50 characters")
    private String customerCode;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Pattern(regexp = "^(\\+?[0-9]{10,15})?$", message = "Phone number must be valid (10-15 digits)")
    private String phoneNumber;

    @Email(message = "Email must be valid")
    private String email;

    @Size(max = 100, message = "Province must not exceed 100 characters")
    private String province;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;
}
