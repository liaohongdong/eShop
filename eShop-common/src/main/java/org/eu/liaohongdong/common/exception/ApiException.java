package org.eu.liaohongdong.common.exception;

import org.eu.liaohongdong.common.api.IErrorCode;

// 自定义API异常，用于封装业务处理过程中的错误信息
public class ApiException extends RuntimeException {
    // 错误码
    private IErrorCode errorCode;

    // 根据错误码构造异常，异常信息取错误码的描述
    public ApiException(IErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    // 根据自定义异常信息构造异常
    public ApiException(String message) {
        super(message);
    }

    // 根据异常原因构造异常
    public ApiException(Throwable cause) {
        super(cause);
    }

    // 根据自定义异常信息和异常原因构造异常
    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }

    // 获取错误码
    public IErrorCode getErrorCode() {
        return errorCode;
    }
}
