package com.imperium.api.exception;

/**
 * 议案业务异常
 */
public class DocketException extends RuntimeException {

    private final String code;

    public DocketException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static DocketException notFound(String id) {
        return new DocketException("DOCKET_NOT_FOUND", "议案不存在：" + id);
    }

    public static DocketException invalidTransition(String from, String to) {
        return new DocketException("INVALID_TRANSITION",
            "不合法的状态流转：" + from + " → " + to);
    }
}
