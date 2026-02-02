package com.core.bank.demo.dispatcher;

import org.springframework.stereotype.Component;

import com.core.bank.demo.config.exception.BusinessException;
import com.core.bank.demo.config.exception.ErrorCode;
import com.core.bank.demo.config.function.registry.FunctionRegistry;
import com.core.bank.demo.contract.OperationHandler;
import com.core.bank.demo.contract.Request;
import com.core.bank.demo.contract.Response;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FunctionDispatcher {

    private final FunctionRegistry registry;

    public Response dispatch(Request request) {
        String destination = request.getDestination();
        String action = request.getAction();

        log.debug("Dispatching: {}.{}", destination, action);

        OperationHandler handler = registry.get(destination, action);

        if (handler == null) {
            log.warn("Operation not found: {}.{}", destination, action);
            throw new BusinessException(ErrorCode.UNSUPPORTED_OPERATION,
                    String.format("Unsupported operation: %s.%s", destination, action));
        }

        return handler.execute(request);
    }
}
