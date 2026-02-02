package com.core.bank.demo.config.function.registry;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import com.core.bank.demo.contract.Function;
import com.core.bank.demo.contract.Operation;
import com.core.bank.demo.contract.OperationHandler;
import com.core.bank.demo.contract.Request;
import com.core.bank.demo.contract.Response;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FunctionRegistry {

    private final Map<String, OperationHandler> handlers = new ConcurrentHashMap<>();

    public FunctionRegistry(ApplicationContext context) {
        Map<String, Object> beans = context.getBeansWithAnnotation(Function.class);
        log.info("Initializing FunctionRegistry with {} providers", beans.size());

        Set<String> registeredDestinations = new HashSet<>();

        for (Object bean : beans.values()) {
            Class<?> targetClass = AopUtils.getTargetClass(bean);
            Function function = AnnotationUtils.findAnnotation(targetClass, Function.class);

            if (function == null)
                continue;

            String destination = function.value();
            String className = targetClass.getSimpleName();

            if (!registeredDestinations.add(destination)) {
                throw new IllegalStateException("Duplicate @CoreFunction('" + destination + "') in " + className);
            }

            for (Method method : targetClass.getDeclaredMethods()) {
                Operation operation = AnnotationUtils.findAnnotation(method, Operation.class);
                if (operation == null)
                    continue;

                String action = operation.value();
                String key = destination + "." + action;
                String methodName = method.getName();
                boolean hasRequestParam = method.getParameterCount() == 1
                        && method.getParameterTypes()[0] == Request.class;

                OperationHandler handler = (req) -> invokeViaProxy(bean, methodName, req, hasRequestParam);
                handlers.put(key, handler);
                log.info("Registered: {} -> {}#{} (hasRequest={})", key, className, methodName, hasRequestParam);
            }
        }

        log.info("Total operations: {}", handlers.size());
    }

    private Response invokeViaProxy(Object proxy, String methodName, Request request, boolean hasRequestParam) {
        try {
            Method method;
            Object result;

            if (hasRequestParam) {
                method = proxy.getClass().getMethod(methodName, Request.class);
                result = method.invoke(proxy, request);
            } else {
                method = proxy.getClass().getMethod(methodName);
                result = method.invoke(proxy);
            }

            return (Response) result;
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException(cause);
        }
    }

    public OperationHandler get(String destination, String action) {
        return handlers.get(destination + "." + action);
    }
}
