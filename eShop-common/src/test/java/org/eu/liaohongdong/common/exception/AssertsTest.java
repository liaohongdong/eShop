package org.eu.liaohongdong.common.exception;

import org.eu.liaohongdong.common.api.IErrorCode;
import org.eu.liaohongdong.common.api.ResultCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AssertsTest {

    @Test
    void failWithMessage_throwsApiExceptionCarryingMessage() {
        ApiException ex = assertThrows(ApiException.class, () -> Asserts.fail("出错了"));

        assertEquals("出错了", ex.getMessage());
        assertNull(ex.getErrorCode());
    }

    @Test
    void failWithErrorCode_throwsApiExceptionCarryingErrorCode() {
        ApiException ex = assertThrows(ApiException.class, () -> Asserts.fail(ResultCode.UNAUTHORIZED));

        assertEquals(ResultCode.UNAUTHORIZED.getMessage(), ex.getMessage());
        assertSame(ResultCode.UNAUTHORIZED, ex.getErrorCode());
    }

    @Test
    void failWithCustomErrorCode_keepsErrorCodeInstance() {
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

        ApiException ex = assertThrows(ApiException.class, () -> Asserts.fail(custom));

        assertEquals("业务异常", ex.getMessage());
        assertSame(custom, ex.getErrorCode());
    }

    @Test
    void failWithNullMessage_throwsApiExceptionWithNullMessage() {
        ApiException ex = assertThrows(ApiException.class, () -> Asserts.fail((String) null));

        assertNull(ex.getMessage());
        assertNull(ex.getErrorCode());
    }
}
