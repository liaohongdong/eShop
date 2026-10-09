package org.eu.liaohongdong.common.exception;

import org.eu.liaohongdong.common.api.CommonResult;
import org.eu.liaohongdong.common.api.ResultCode;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.sql.SQLSyntaxErrorException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleApiException_withErrorCode_usesErrorCode() {
        CommonResult result = handler.handle(new ApiException(ResultCode.UNAUTHORIZED));

        assertEquals(401, result.getCode());
        assertEquals(ResultCode.UNAUTHORIZED.getMessage(), result.getMsg());
    }

    @Test
    void handleApiException_withoutErrorCode_usesMessage() {
        CommonResult result = handler.handle(new ApiException("出错了"));

        assertEquals(500, result.getCode());
        assertEquals("出错了", result.getMsg());
    }

    @Test
    void handleMethodArgumentNotValidException_buildsFieldMessage() throws NoSuchMethodException {
        MethodParameter parameter = new MethodParameter(
                Sample.class.getDeclaredMethod("validate", String.class), 0);
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Sample(), "sample");
        bindingResult.addError(new FieldError("sample", "username", "不能为空"));
        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(parameter, bindingResult);

        CommonResult result = handler.handleValidException(ex);

        assertEquals(404, result.getCode());
        assertEquals("username不能为空", result.getMsg());
    }

    @Test
    void handleBindException_buildsFieldMessage() {
        BindException ex = new BindException(new Sample(), "sample");
        ex.addError(new FieldError("sample", "username", "不能为空"));

        CommonResult result = handler.handleValidException(ex);

        assertEquals(404, result.getCode());
        assertEquals("username不能为空", result.getMsg());
    }

    @Test
    void handleBindException_withoutErrors_returnsNullMessage() {
        BindException ex = new BindException(new Sample(), "sample");

        CommonResult result = handler.handleValidException(ex);

        assertEquals(404, result.getCode());
        assertNull(result.getMsg());
    }

    @Test
    void handleBindException_withOnlyGlobalError_returnsNullMessage() {
        BindException ex = new BindException(new Sample(), "sample");
        ex.addError(new ObjectError("sample", "全局错误"));

        CommonResult result = handler.handleValidException(ex);

        assertEquals(404, result.getCode());
        assertNull(result.getMsg());
    }

    @Test
    void handleSqlSyntaxError_withDenied_replacesWithFriendlyMessage() {
        CommonResult result = handler.handleSQLSyntaxErrorException(
                new SQLSyntaxErrorException("Access denied for user 'root'@'localhost'"));

        assertEquals(500, result.getCode());
        assertEquals("演示环境暂无修改权限，如需修改数据可本地搭建后台服务！", result.getMsg());
    }

    @Test
    void handleSqlSyntaxError_withoutDenied_keepsOriginalMessage() {
        CommonResult result = handler.handleSQLSyntaxErrorException(
                new SQLSyntaxErrorException("You have an error in your SQL syntax"));

        assertEquals(500, result.getCode());
        assertEquals("You have an error in your SQL syntax", result.getMsg());
    }

    static class Sample {
        void validate(String username) {
        }
    }
}
