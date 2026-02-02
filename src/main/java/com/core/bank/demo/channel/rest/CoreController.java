package com.core.bank.demo.channel.rest;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.core.bank.demo.config.exception.BusinessException;
import com.core.bank.demo.config.exception.ErrorCode;
import com.core.bank.demo.contract.Constant;
import com.core.bank.demo.contract.Request;
import com.core.bank.demo.contract.Response;
import com.core.bank.demo.dispatcher.FunctionDispatcher;

@RestController
@RequestMapping("/core")
public class CoreController {

    private final FunctionDispatcher dispatcher;

    public CoreController(FunctionDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @PostMapping("/execute")
    public Response execute(@Valid @RequestBody Request request) {
        return dispatcher.dispatch(request);
    }

    @PostMapping("/authenticate")
    public Response authenticate(@RequestBody Request request) {
        if (!Constant.AUTH.name().equals(request.getDestination())) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED, "Invalid authentication request");
        }
        return dispatcher.dispatch(request);
    }
}
