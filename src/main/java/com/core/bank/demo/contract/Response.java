package com.core.bank.demo.contract;

import com.core.bank.demo.config.exception.ErrorCode;
import com.core.bank.demo.config.filter.header.RequestContextHolder;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;

@Getter
public class Response {

    private String code;
    private String message;
    private String path;
    private String clientMessageId;
    private Object data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object errors;

    public static Response ok(Object data) {
        Response r = new Response();
        r.code = ErrorCode.SUCCESS.getCode();
        r.message = ErrorCode.SUCCESS.getMessage();
        r.data = data;
        r.populateContext();
        return r;
    }

    public static Response error(String message) {
        Response r = new Response();
        r.code = ErrorCode.SYSTEM_ERROR.getCode();
        r.message = message;
        r.populateContext();
        return r;
    }

    public static Response error(ErrorCode errorCode) {
        Response r = new Response();
        r.code = errorCode.getCode();
        r.message = errorCode.getMessage();
        r.populateContext();
        return r;
    }

    public static Response error(ErrorCode errorCode, String message) {
        Response r = new Response();
        r.code = errorCode.getCode();
        r.message = message;
        r.populateContext();
        return r;
    }

    public static Response error(ErrorCode errorCode, String message, Object errors) {
        Response r = new Response();
        r.code = errorCode.getCode();
        r.message = message;
        r.errors = errors;
        r.populateContext();
        return r;
    }

    private void populateContext() {
        var context = RequestContextHolder.get();
        if (context != null) {
            this.path = context.getPath();
            this.clientMessageId = context.getClientMessageId();
        }
    }
}
