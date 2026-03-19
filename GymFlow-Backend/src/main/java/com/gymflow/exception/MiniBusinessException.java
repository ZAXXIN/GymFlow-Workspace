package com.gymflow.exception;

/**
 * 小程序业务异常
 */
public class MiniBusinessException extends BusinessException {

    public MiniBusinessException(String message) {
        super(message);
    }

    public MiniBusinessException(int code, String message) {
        super(code, message);
    }
}