package com.core.bank.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class TokenDto {

    private final String accessToken;
    private final String refreshToken;
}
