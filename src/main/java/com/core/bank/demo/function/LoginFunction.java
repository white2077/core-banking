package com.core.bank.demo.function;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import com.core.bank.demo.contract.Auditable;
import com.core.bank.demo.contract.Function;
import com.core.bank.demo.contract.Operation;
import com.core.bank.demo.contract.Response;
import com.core.bank.demo.dto.TokenDto;
import com.core.bank.demo.entity.User;
import com.core.bank.demo.enums.TokenType;
import com.core.bank.demo.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;

import lombok.RequiredArgsConstructor;

@Function("AUTH")
@RequiredArgsConstructor
public class LoginFunction {

    private final AuthenticationManager authManager;
    private final HttpServletRequest request;
    private final UserRepository userRepository;

    @Value("${jwt.signer.key}")
    private String signerKey;

    @Operation("LOGIN")
    @Transactional
    @Auditable(action = "AUTH_LOGIN", objectType = "USER")
    public Response login() {
        // Request parameter is available but we use HttpServletRequest for Basic Auth
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return Response.error("Missing or invalid Authorization header");
        }

        String base64Credentials = authHeader.substring("Basic ".length());
        byte[] credDecoded = java.util.Base64.getDecoder().decode(base64Credentials);
        String credentials = new String(credDecoded);
        final String[] values = credentials.split(":", 2);
        String username = values[0];
        String password = values[1];

        Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            return Response.error("User not found");
        }

        SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            TokenDto tokenDto = new TokenDto(generateToken(user.get(), 1, TokenType.ACCESS_TOKEN),
                    generateToken(user.get(), 7, TokenType.REFRESH_TOKEN));
            return Response.ok(tokenDto);
        } catch (JOSEException e) {
            return Response.error(e.getMessage());
        }
    }

    public String generateToken(User user, int expirationDay, TokenType tokenType) throws JOSEException {
        Date now = new Date();
        Instant nowInstant = now.toInstant();
        Instant expirationInstant = nowInstant.plus(expirationDay, ChronoUnit.DAYS);
        Date expirationTime = Date.from(expirationInstant);
        JWSHeader header;

        if (tokenType == TokenType.ACCESS_TOKEN) {
            header = new JWSHeader(JWSAlgorithm.HS256);
        } else {
            header = new JWSHeader(JWSAlgorithm.HS512);
        }

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().subject(user.getUsername()).issuer("dev-white2077")
                .issueTime(now).expirationTime(expirationTime).jwtID(UUID.randomUUID().toString())
                .subject(user.getUsername()).audience(user.getUsername()).claim("scope", buildScope(user)).build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        jwsObject.sign(new MACSigner(signerKey.getBytes()));

        return jwsObject.serialize();
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (user.getRole() != null) {
            stringJoiner.add(user.getRole().name());
        }

        return stringJoiner.toString();
    }
}
