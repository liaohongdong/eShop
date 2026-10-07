package org.eu.liaohongdong.common.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ResultCodeTest {

    @Test
    void codes_matchExpectedValues() {
        assertEquals(200, ResultCode.SUCCESS.getCode());
        assertEquals(500, ResultCode.FAILED.getCode());
        assertEquals(404, ResultCode.VALIDATE_FAILED.getCode());
        assertEquals(401, ResultCode.UNAUTHORIZED.getCode());
        assertEquals(403, ResultCode.FORBIDDEN.getCode());
    }

    @Test
    void messages_matchExpectedValues() {
        assertEquals("操作成功", ResultCode.SUCCESS.getMessage());
        assertEquals("操作失败", ResultCode.FAILED.getMessage());
        assertEquals("参数检验失败", ResultCode.VALIDATE_FAILED.getMessage());
        assertEquals("暂未登录或token已经过期", ResultCode.UNAUTHORIZED.getMessage());
        assertEquals("没有相关权限", ResultCode.FORBIDDEN.getMessage());
    }

    @Test
    void allValues_implementIErrorCode() {
        for (ResultCode resultCode : ResultCode.values()) {
            System.out.println(resultCode);
            assertInstanceOf(IErrorCode.class, resultCode);
        }
        assertEquals(5, ResultCode.values().length);
    }

    @Test
    void valueOf_roundTripsByName() {
        assertEquals(ResultCode.SUCCESS, ResultCode.valueOf("SUCCESS"));
        assertEquals(ResultCode.UNAUTHORIZED, ResultCode.valueOf("UNAUTHORIZED"));
    }
}
