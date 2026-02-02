package com.core.bank.demo.config.filter.header;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RequestContext {

    private String clientMessageId;
    private long receivedTime;
    private String path;
    private String clientIp;
}
