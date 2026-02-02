package com.core.bank.demo.contract;

@FunctionalInterface
public interface OperationHandler {

    Response execute(Request request);
}
