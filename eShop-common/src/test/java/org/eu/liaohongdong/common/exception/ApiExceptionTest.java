package org.eu.liaohongdong.common.exception;

import org.eu.liaohongdong.common.api.IErrorCode;
import org.eu.liaohongdong.common.api.ResultCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiExceptionTest {

    @Test
    void errorCodeConstructor_usesMessageFromErrorCode() {
        ApiException ex = new ApiException(ResultCode.UNAUTHORIZED);

        assertEquals(ResultCode.UNAUTHORIZED.getMessage(), ex.getMessage());
        assertSame(ResultCode.UNAUTHORIZED, ex.getErrorCode());
    }

    @Test
    void errorCodeConstructor_keepsCustomErrorCodeInstance() {
        IErrorCode custom = new IErrorCode() {
            @Override
            public long getCode() {
                return 600;
            }

            @Override
            public String getMessage() {
                return "业务异常";
            }
        };

        ApiException ex = new ApiException(custom);

        assertEquals("业务异常", ex.getMessage());
        assertSame(custom, ex.getErrorCode());
    }

    @Test
    void messageConstructor_setsMessageAndLeavesErrorCodeNull() {
        ApiException ex = new ApiException("出错了");

        assertEquals("出错了", ex.getMessage());
        assertNull(ex.getErrorCode());
        assertNull(ex.getCause());
    }

    @Test
    void messageConstructor_acceptsNullMessage() {
        ApiException ex = new ApiException((String) null);

        assertNull(ex.getMessage());
        assertNull(ex.getErrorCode());
    }

    @Test
    void causeConstructor_wrapsCauseAndDerivesMessage() {
        IllegalStateException cause = new IllegalStateException("底层失败");

        ApiException ex = new ApiException(cause);

        assertSame(cause, ex.getCause());
        assertEquals(cause.toString(), ex.getMessage());
        assertNull(ex.getErrorCode());
    }

    @Test
    void messageAndCauseConstructor_setsBoth() {
        IllegalArgumentException cause = new IllegalArgumentException("非法参数");

        ApiException ex = new ApiException("业务失败", cause);

        assertEquals("业务失败", ex.getMessage());
        assertSame(cause, ex.getCause());
        assertNull(ex.getErrorCode());
    }

    @Test
    void isRuntimeException_soCallersDoNotNeedToDeclareIt() {
        assertTrue(RuntimeException.class.isAssignableFrom(ApiException.class));
        assertThrows(ApiException.class, () -> {
            throw new ApiException(ResultCode.FAILED);
        });
    }
}
