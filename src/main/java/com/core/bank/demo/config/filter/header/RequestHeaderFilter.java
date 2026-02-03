package com.core.bank.demo.config.filter.header;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.core.bank.demo.config.exception.ErrorCode;
import com.core.bank.demo.contract.Response;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Order(Integer.MIN_VALUE)
@Component
@RequiredArgsConstructor
public class RequestHeaderFilter extends OncePerRequestFilter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final List<String> requestMatchers = List.of("/actuator/**", "/v2/**", "/v3/**",
            "/swagger-ui.html**", "/swagger-ui/**", "/swagger-resources/**");
    private final PathMatcher mvcPathMatcher;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        RequestContext ctx = new RequestContext();
        RequestContextHolder.set(ctx);
        ctx.setReceivedTime(System.currentTimeMillis());
        String clientIp = getRemoteAddress(request);
        ctx.setClientIp(clientIp);
        ctx.setPath(path);
        ThreadContext.put("clientIp", clientIp);
        ThreadContext.put("path", path);
        String clientMessageId = request.getHeader(HeaderEnum.CLIENT_MESSAGE_ID.getLabel());

        if (!validateIgnorePath(ctx, request)) {
            if (clientMessageId == null || clientMessageId.isEmpty()) {
                throwValidateHeaderError(response, request);
                return;
            }
            ctx.setClientMessageId(clientMessageId);
        }

        ctx.setClientMessageId(clientMessageId);
        String clientLocale = request.getHeader(HeaderEnum.CLIENT_LOCALE.getLabel());
        ThreadContext.put(HeaderEnum.CLIENT_MESSAGE_ID.getLabel(), ctx.getClientMessageId());
        ThreadContext.put(HeaderEnum.CLIENT_LOCALE.getLabel(), clientLocale);

        filterChain.doFilter(request, response);
    }

    private boolean validateIgnorePath(RequestContext ctx, HttpServletRequest request) {
        String requestPath = request.getRequestURI();

        boolean ignorePath = requestMatchers.stream().anyMatch(pattern -> mvcPathMatcher.match(pattern, requestPath));

        if (ignorePath) {
            ctx.setClientMessageId(UUID.randomUUID().toString());
            return true;
        }

        return false;
    }

    private static String getRemoteAddress(HttpServletRequest request) {
        String remoteAddress = "";
        if (request != null) {
            remoteAddress = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddress == null || remoteAddress.isEmpty()) {
                remoteAddress = request.getRemoteAddr();
            }
        }
        return remoteAddress;
    }

    private void throwValidateHeaderError(HttpServletResponse response, HttpServletRequest request) throws IOException {
        Response errorResponse = Response.error(ErrorCode.MISSING_HEADER, "Missing required header");
        ObjectMapper objectMapper = new ObjectMapper();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
