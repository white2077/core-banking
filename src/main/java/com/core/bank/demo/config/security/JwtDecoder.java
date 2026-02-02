package com.core.bank.demo.config.security;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtDecoder implements org.springframework.security.oauth2.jwt.JwtDecoder {

    @Value("${jwt.signer.key}")
    private String signerKey;

    @Override
    public Jwt decode(String token) throws JwtException {

        SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), MacAlgorithm.HS256.getName());

        return NimbusJwtDecoder.withSecretKey(secretKeySpec).macAlgorithm(MacAlgorithm.HS256).build().decode(token);
    }
}
