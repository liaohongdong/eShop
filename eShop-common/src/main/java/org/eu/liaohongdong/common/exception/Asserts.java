package org.eu.liaohongdong.common.exception;

import org.eu.liaohongdong.common.api.IErrorCode;

// 断言处理类，用于抛出各种API异常
public class Asserts {
    // 根据自定义异常信息抛出API异常
    public static void fail(String message) {
        throw new ApiException(message);
    }

    // 根据错误码抛出API异常
    public static void fail(IErrorCode errorCode) {
        throw new ApiException(errorCode);
    }
}
