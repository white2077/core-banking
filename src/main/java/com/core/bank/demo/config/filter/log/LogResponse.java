package com.core.bank.demo.config.filter.log;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LogResponse extends BaseLog {

    private String clientMessageId;
    private long responseTime;
    private int statusCode;
    private Object responseBody;
}
