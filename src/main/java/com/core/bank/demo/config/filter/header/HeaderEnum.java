package com.core.bank.demo.config.filter.header;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HeaderEnum {

    CLIENT_MESSAGE_ID("clientMessageId"), CLIENT_LOCALE("clientLocale");
    private final String label;
}
