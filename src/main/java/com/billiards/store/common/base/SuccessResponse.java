package com.billiards.store.common.base;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SuccessResponse <T> {
    private int code;
    private String message;
    private T data;
    private LocalDateTime timestamp;
}
