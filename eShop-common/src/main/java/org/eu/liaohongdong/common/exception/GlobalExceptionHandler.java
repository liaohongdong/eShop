package org.eu.liaohongdong.common.exception;

import cn.hutool.core.util.StrUtil;
import org.eu.liaohongdong.common.api.CommonResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.SQLSyntaxErrorException;

// 全局异常处理类，统一处理控制层抛出的各类异常
@ControllerAdvice // 标记为控制器增强，对全局所有 Controller 生效
public class GlobalExceptionHandler { // 定义全局异常处理类

    // 处理自定义的业务异常 ApiException
    @ResponseBody
    @ExceptionHandler(value = ApiException.class) // 指定捕获 ApiException 异常
    public CommonResult handle(ApiException e) {
        if (e.getErrorCode() != null) { // 判断异常是否携带了错误码
            return CommonResult.failed(e.getErrorCode()); // 携带错误码则用错误码构造失败响应
        }
        return CommonResult.failed(e.getMessage()); // 没有错误码则用异常消息构造失败响应
    }

    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class) // 指定捕获方法参数校验异常
    public CommonResult handleValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult(); // 获取参数绑定与校验结果对象
        String message = null;
        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldError(); // 获取第一个字段校验错误
            if (fieldError != null) {
                message = fieldError.getField() + fieldError.getDefaultMessage();
            }
        }
        return CommonResult.validateFailed(message);
    }

    @ResponseBody
    @ExceptionHandler(value = BindException.class) // 指定捕获绑定异常
    public CommonResult handleValidException(BindException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = null;
        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldError();
            if (fieldError != null) {
                message = fieldError.getField() + fieldError.getDefaultMessage(); // 拼接字段名和错误提示作为消息
            }
        }
        return CommonResult.validateFailed(message);
    }

    @ResponseBody
    @ExceptionHandler(value = SQLSyntaxErrorException.class) // 指定捕获 SQL 语法异常
    public CommonResult handleSQLSyntaxErrorException(SQLSyntaxErrorException e) {
        String message = e.getMessage();
        if (StrUtil.isNotEmpty(message) && message.contains("denied")) { // 判断消息非空且包含 denied（权限被拒）
            message = "演示环境暂无修改权限，如需修改数据可本地搭建后台服务！"; // 替换为对用户友好的提示信息
        }
        return CommonResult.failed(message);
    }
}
