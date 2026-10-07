package org.eu.liaohongdong.common.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class CommonResultTest {

    @Test
    void successWithData_usesSuccessCodeAndMessage() {
        CommonResult<String> result = CommonResult.success("ok");
        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMsg());
        assertEquals("ok", result.getData());
    }

    @Test
    void successWithCustomMessage_keepsSuccessCode() {
        CommonResult<Integer> result = CommonResult.success(1, "自定义成功");
        assertEquals(200, result.getCode());
        assertEquals("自定义成功", result.getMsg());
        assertEquals(1, result.getData());
    }

    @Test
    void successWithNullData_keepsSuccessCode() {
        CommonResult<String> result = CommonResult.success(null);
        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void failedWithErrorCode_usesErrorCodeAndNullData() {
        CommonResult<Object> result = CommonResult.failed(ResultCode.VALIDATE_FAILED);
        assertEquals(404, result.getCode());
        assertEquals("参数检验失败", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void failedWithErrorCodeAndMessage_overridesMessageKeepsCode() {
        CommonResult<Object> result = CommonResult.failed(ResultCode.FORBIDDEN, "无权访问");
        assertEquals(403, result.getCode());
        assertEquals("无权访问", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void failedWithMessage_usesFailedCode() {
        CommonResult<Object> result = CommonResult.failed("出错了");
        assertEquals(500, result.getCode());
        assertEquals("出错了", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void failedWithCustomErrorCode_usesCustomCodeAndMessage() {
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
        CommonResult<Object> result = CommonResult.failed(custom);
        assertEquals(600, result.getCode());
        assertEquals("业务异常", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void validateFailed_usesValidateFailedCode() {
        CommonResult<Object> result = CommonResult.validateFailed();
        assertEquals(404, result.getCode());
        assertEquals("参数检验失败", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void validateFailedWithMessage_overridesMessage() {
        CommonResult<Object> result = CommonResult.validateFailed("用户名不能为空");
        assertEquals(404, result.getCode());
        assertEquals("用户名不能为空", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void unauthorized_uses401Code() {
        CommonResult<Object> result = CommonResult.unauthorized(null);
        assertEquals(401, result.getCode());
        assertEquals("暂未登录或token已经过期", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void forbidden_uses403Code() {
        CommonResult<Object> result = CommonResult.forbidden(null);
        assertEquals(403, result.getCode());
        assertEquals("没有相关权限", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void gettersAndSetters_roundTrip() {
        CommonResult<String> result = new CommonResult<>();
        result.setCode(201);
        result.setMsg("已创建");
        result.setData("created");
        assertEquals(201, result.getCode());
        assertEquals("已创建", result.getMsg());
        assertEquals("created", result.getData());
    }

    @Test
    void equalsAndHashCode_areFieldBased() {
        assertEquals(CommonResult.success("a"), CommonResult.success("a"));
        assertNotEquals(CommonResult.success("a"), CommonResult.success("b"));
        assertNotEquals(CommonResult.success("a"), CommonResult.failed("a"));
    }

    @Test
    void successPreservesSameDataInstance() {
        StringBuilder data = new StringBuilder("x");
        CommonResult<StringBuilder> result = CommonResult.success(data);
        assertSame(data, result.getData());
    }
}
