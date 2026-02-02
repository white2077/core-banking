package com.core.bank.demo.config.filter.log;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LogRequest extends BaseLog {

    private String clientMessageId;
    private Long requestTime;
    private Object requestBody;
}
