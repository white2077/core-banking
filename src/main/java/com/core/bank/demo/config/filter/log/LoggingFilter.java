package com.core.bank.demo.config.filter.log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.core.bank.demo.config.filter.header.HeaderEnum;
import com.core.bank.demo.config.filter.header.RequestContextHolder;
import com.core.bank.demo.util.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Order
@Slf4j
@RequiredArgsConstructor
public class LoggingFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, 1048576);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } catch (Exception ex) {
            log.error("Exception during request processing: {}", ex.getMessage(), ex);
            throw ex;
        } finally {
            try {
                logRequestResponse(wrappedRequest, wrappedResponse, startTime);
            } finally {
                RequestContextHolder.remove();
            }
        }
    }

    private void logRequestResponse(ContentCachingRequestWrapper wrappedRequest,
            ContentCachingResponseWrapper wrappedResponse, long startTime) throws IOException {
        String requestBody = getRequestBody(wrappedRequest);
        String responseBody = getResponseBody(wrappedResponse);

        LogRequest logRequest = new LogRequest();
        logRequest.setClientMessageId(wrappedRequest.getHeader(HeaderEnum.CLIENT_MESSAGE_ID.getLabel()));
        logRequest.setIp(getRemoteAddress(wrappedRequest));
        logRequest.setPath(wrappedRequest.getRequestURI());
        logRequest.setRequestTime(startTime);

        try {
            JsonNode parsedReqBody = requestBody == null ? null : objectMapper.readTree(requestBody);
            logRequest.setRequestBody(JsonUtils.maskSensitiveFields(parsedReqBody));
        } catch (Exception e) {
            logRequest.setRequestBody(requestBody);
        }
        log.info("Request: {}", objectMapper.writeValueAsString(logRequest));

        long endTime = System.currentTimeMillis();

        if (responseBody != null && !responseBody.isEmpty()) {
            LogResponse logResponse = new LogResponse();
            logResponse.setStatusCode(wrappedResponse.getStatus());
            logResponse.setClientMessageId(wrappedRequest.getHeader(HeaderEnum.CLIENT_MESSAGE_ID.getLabel()));
            logResponse.setResponseTime(endTime);
            logResponse.setIp(getRemoteAddress(wrappedRequest));
            logResponse.setPath(wrappedRequest.getRequestURI());

            try {
                JsonNode parsedResBody = objectMapper.readTree(responseBody);
                logResponse.setResponseBody(JsonUtils.maskSensitiveFields(parsedResBody));
            } catch (Exception e) {
                logResponse.setResponseBody(responseBody);
            }
            log.info("Response: {}", objectMapper.writeValueAsString(logResponse));
            wrappedResponse.copyBodyToResponse();
        } else {
            wrappedResponse.copyBodyToResponse();
        }

        log.info("Duration: {} ms", endTime - startTime);
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

    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] buf = request.getContentAsByteArray();
        if (buf.length == 0)
            return null;
        try {
            request.getCharacterEncoding();
            return new String(buf, request.getCharacterEncoding());
        } catch (Exception ex) {
            return null;
        }
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] buf = response.getContentAsByteArray();
        if (buf.length == 0)
            return null;
        try {
            return new String(buf,
                    response.getCharacterEncoding() != null
                            ? response.getCharacterEncoding()
                            : StandardCharsets.UTF_8.name());
        } catch (Exception ex) {
            return null;
        }
    }

}
