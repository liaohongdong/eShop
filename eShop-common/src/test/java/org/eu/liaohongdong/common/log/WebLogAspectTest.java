package org.eu.liaohongdong.common.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebLogAspectTest {

    private final WebLogAspect aspect = new WebLogAspect();

    private Logger logger;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(WebLogAspect.class);
        logger.setLevel(Level.INFO);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void doAround_returnsProceedResultAndLogsRequestInfo() throws Throwable {
        givenRequest();
        ProceedingJoinPoint joinPoint = joinPoint(DemoController.class.getDeclaredMethod("plain"), "ok");

        Object result = aspect.doAround(joinPoint);

        assertEquals("ok", result);
        verify(joinPoint).proceed();
        JSONObject log = loggedWebLog();
        assertEquals("GET", log.getStr("method"));
        assertEquals("http://localhost:8080/api/demo", log.getStr("url"));
        assertEquals("/api/demo", log.getStr("uri"));
        assertEquals("http://localhost:8080", log.getStr("basePath"));
        assertEquals("203.0.113.5", log.getStr("ip"));
        assertEquals("tester", log.getStr("username"));
        assertEquals("ok", log.getStr("result"));
        assertTrue(log.getInt("spendTime") >= 0);
        assertTrue(log.getLong("startTime") > 0);
    }

    @Test
    void doAround_withOperationAnnotation_usesSummaryAsDescription() throws Throwable {
        givenRequest();
        ProceedingJoinPoint joinPoint = joinPoint(DemoController.class.getDeclaredMethod("annotated"), "ok");

        aspect.doAround(joinPoint);

        assertEquals("查询演示", loggedWebLog().getStr("description"));
    }

    @Test
    void doAround_withoutOperationAnnotation_leavesDescriptionNull() throws Throwable {
        givenRequest();
        ProceedingJoinPoint joinPoint = joinPoint(DemoController.class.getDeclaredMethod("plain"), "ok");

        aspect.doAround(joinPoint);

        assertNull(loggedWebLog().getStr("description"));
    }

    @Test
    void doBeforeAndDoAfterReturning_areNoOps() {
        assertDoesNotThrow(() -> aspect.doBefore(null));
        assertDoesNotThrow(() -> aspect.doAfterReturning(new Object()));
    }

    @Test
    void getParameter_withAnnotatedParams_returnsNullAsCurrentlyImplemented() throws Exception {
        Method method = ParamSample.class.getDeclaredMethod("bodyAndParam", String.class, String.class);

        Object result = invokeGetParameter(method, new Object[]{"body", "tom"});

        assertNull(result);
    }

    @Test
    void getParameter_withoutAnnotatedParams_returnsEmptyList() throws Exception {
        Method method = ParamSample.class.getDeclaredMethod("noAnnotation", String.class);

        Object result = invokeGetParameter(method, new Object[]{"value"});

        assertTrue(result instanceof List);
        assertTrue(((List<?>) result).isEmpty());
    }

    @Test
    void getParameter_withNullRequestParamValue_returnsEmptyList() throws Exception {
        Method method = ParamSample.class.getDeclaredMethod("paramOnly", String.class);

        Object result = invokeGetParameter(method, new Object[]{null});

        assertTrue(result instanceof List);
        assertTrue(((List<?>) result).isEmpty());
    }

    private void givenRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/api/demo"));
        when(request.getRemoteUser()).thenReturn("tester");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/demo");
        when(request.getRemoteAddr()).thenReturn("203.0.113.5");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private ProceedingJoinPoint joinPoint(Method method, Object returnValue) throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature methodSignature = mock(MethodSignature.class);
        when(joinPoint.proceed()).thenReturn(returnValue);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        return joinPoint;
    }

    private JSONObject loggedWebLog() {
        return JSONUtil.parseObj(appender.list.get(0).getFormattedMessage());
    }

    private Object invokeGetParameter(Method method, Object[] args) throws Exception {
        Method getParameter = WebLogAspect.class.getDeclaredMethod("getParameter", Method.class, Object[].class);
        getParameter.setAccessible(true);
        return getParameter.invoke(aspect, method, args);
    }

    static class DemoController {
        @Operation(summary = "查询演示")
        public String annotated() {
            return "ok";
        }

        public String plain() {
            return "ok";
        }
    }

    static class ParamSample {
        public void bodyAndParam(@RequestBody String body, @RequestParam("name") String name) {
        }

        public void noAnnotation(String value) {
        }

        public void paramOnly(@RequestParam("name") String name) {
        }
    }
}
