package com.core.bank.demo.config.filter.header;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.core.bank.demo.config.exception.ErrorCode;
import com.core.bank.demo.contract.Response;
import com.fasterxml.jackson.databind.ObjectMapper;

@Order(Integer.MIN_VALUE)
@Component
public class RequestHeaderFilter extends OncePerRequestFilter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
        if (clientMessageId == null || clientMessageId.isEmpty()) {
            sendHeaderValidationError(response, "Missing required header: " + HeaderEnum.CLIENT_MESSAGE_ID.getLabel());
            return;
        }

        ctx.setClientMessageId(clientMessageId);
        String clientLocale = request.getHeader(HeaderEnum.CLIENT_LOCALE.getLabel());
        ThreadContext.put(HeaderEnum.CLIENT_MESSAGE_ID.getLabel(), ctx.getClientMessageId());
        ThreadContext.put(HeaderEnum.CLIENT_LOCALE.getLabel(), clientLocale);

        filterChain.doFilter(request, response);
    }

    private void sendHeaderValidationError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Response errorResponse = Response.error(ErrorCode.MISSING_HEADER, message);
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(errorResponse));
    }
}
