package org.eu.liaohongdong.common.log;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import net.logstash.logback.marker.Markers;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.eu.liaohongdong.common.domian.WebLog;
import org.eu.liaohongdong.common.util.RequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

// 统一日志处理切面：对所有 Controller 请求做统一的访问日志记录
@Aspect // 声明这是一个切面类
@Component // 注册为 Spring 组件，交给容器管理
@Order(1) // 指定切面优先级，数字越小优先级越高，保证本切面最先进入、最后退出
public class WebLogAspect {
    // 日志记录器，用于输出访问日志
    private static final Logger LOGGER = LoggerFactory.getLogger(WebLogAspect.class);

    // 切入点：匹配 controller 包下的所有 public 方法（含一级/多级包路径两种写法）
    @Pointcut("execution(public * org.eu.liaohongdong.controller.*.*(..))||execution(public * org.eu.liaohongdong.*.controller.*.*(..))")
    public void webLog() {
    }

    // 前置通知：预留的扩展点，进入目标方法前执行（当前为空实现）
    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) {
    }

    // 返回通知：方法正常返回后执行，可拿到返回值（当前为空实现，预留扩展）
    @AfterReturning(value = "webLog()", returning = "returnValue")
    public void doAfterReturning(Object returnValue) {
    }

    // 环绕通知：请求访问日志的核心逻辑，包裹目标方法执行
    @Around("webLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 记录进入切面的起始时间，用于计算耗时
        long startTime = System.currentTimeMillis();
        // 从 Spring 的 RequestContextHolder 中取出当前线程绑定的请求属性（线程隔离，需在请求线程内获取）
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        // 从请求属性中取出当前 HttpServletRequest
        HttpServletRequest request = attributes.getRequest();
        // 创建访问日志实体，用于承载本次请求的各项信息
        WebLog webLog = new WebLog();
        // 执行目标方法（真正调用 Controller），并拿到返回值
        Object result = joinPoint.proceed();
        // 获取方法签名（包含目标方法信息）
        Signature signature = joinPoint.getSignature();
        // 强转为方法签名，以获取 java.lang.reflect.Method
        MethodSignature methodSignature = (MethodSignature) signature;
        // 获取被代理的目标方法
        Method method = methodSignature.getMethod();
        // 关键点：如果方法上标注了 SpringDoc 的 @Operation，则用其 summary 作为操作描述
        if (method.isAnnotationPresent(Operation.class)) {
            Operation log = method.getAnnotation(Operation.class);
            webLog.setDescription(log.summary());
        }
        long endTime = System.currentTimeMillis(); // 记录方法执行结束时间
        String urlStr = request.getRequestURL().toString(); // 获取完整请求 URL
        // 根路径 = 完整 URL 去掉路径部分，例如 http://host:port
        webLog.setBasePath(StrUtil.removeSuffix(urlStr, URLUtil.url(urlStr).getPath()));
        webLog.setUsername(request.getRemoteUser()); // 远程登录用户名
        webLog.setIp(RequestUtil.getRequestIp(request)); // 通过工具类解析真实客户端 IP
        webLog.setMethod(request.getMethod()); // HTTP 请求方法（GET/POST 等）
//        webLog.setParameter();
        webLog.setResult(result); // 方法返回值
        webLog.setSpendTime((int) (endTime - startTime)); // 请求耗时（毫秒）
        webLog.setStartTime(startTime); // 请求开始时间戳
        webLog.setUri(request.getRequestURI()); // 请求 URI
        webLog.setUrl(request.getRequestURL().toString()); // 完整请求 URL
        // 构造结构化日志字段，配合 logstash 编码器输出为可检索的键值对
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("url", webLog.getUrl());
        logMap.put("method", webLog.getMethod());
        logMap.put("parameter", webLog.getParameter());
        logMap.put("spendTime", webLog.getSpendTime());
        logMap.put("description", webLog.getDescription());
        // 使用 Logstash Marker 附加结构化字段，并把整个 WebLog 序列化为 JSON 输出
        LOGGER.info(Markers.appendEntries(logMap), JSONUtil.parse(webLog).toString());
        return result; // 返回业务方法结果，保证不改变原有调用链
    }

    // 根据方法和传入的参数获取请求参数
    private Object getParameter(Method method, Object[] args) {
        List<Object> argList = new ArrayList<>();
        Parameter[] parameters = method.getParameters(); // 获取方法形参列表
        for (int i = 0; i < parameters.length; i++) { // 遍历每个形参
            // 将 @RequestBody 注解修饰的参数作为请求参数
            RequestBody requestBody = parameters[i].getAnnotation(RequestBody.class);
            if (requestBody != null)
                argList.add(args[i]);
            // 将 @RequestParam 注解修饰的参数作为请求参数
            RequestParam requestParam = parameters[i].getAnnotation(RequestParam.class);
            if (requestParam != null) {
                Map<String, Object> map = new HashMap<>();
                String key = parameters[i].getName(); // 默认用形参名作为 key
                if (!StrUtil.isEmpty(requestParam.value())) { // 若注解显式指定了 value
                    key = requestParam.value(); // 则优先使用注解中的名称
                }
                if (args[i] != null) { // 仅当参数值非空时才收集
                    map.put(key, args[i]);
                    argList.add(map);
                }
            }
        }
        // 注意：当前实现中当收集到参数时（size>0）直接返回 null，逻辑存在疑问，这里按现状注释
        if (argList.size() > 0) {
            return null;
        } else if (argList.size() == 1) { // 仅一个参数时返回其本身
            return argList.get(0);
        } else { // 无参数时返回空列表
            return argList;
        }
    }
}
