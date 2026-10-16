package org.eu.liaohongdong.security.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * SpringUtil 的单元测试。
 * applicationContext 为静态字段，每个用例前重置，避免相互影响。
 */
class SpringUtilTest {

    static class SampleBean {
    }

    private SpringUtil springUtil;

    @BeforeEach
    void setUp() throws Exception {
        resetStaticContext();
        springUtil = new SpringUtil();
    }

    private static void resetStaticContext() throws Exception {
        Field field = SpringUtil.class.getDeclaredField("applicationContext");
        field.setAccessible(true);
        field.set(null, null);
    }

    @Test
    void getApplicationContext_isNullBeforeSet() {
        assertNull(SpringUtil.getApplicationContext());
    }

    @Test
    void setApplicationContext_exposesContext() {
        ApplicationContext context = mock(ApplicationContext.class);

        springUtil.setApplicationContext(context);

        assertSame(context, SpringUtil.getApplicationContext());
    }

    @Test
    void setApplicationContext_ignoresSecondContext() {
        ApplicationContext first = mock(ApplicationContext.class);
        ApplicationContext second = mock(ApplicationContext.class);

        springUtil.setApplicationContext(first);
        springUtil.setApplicationContext(second);

        assertSame(first, SpringUtil.getApplicationContext());
    }

    @Test
    void getBean_byName_delegatesToContext() {
        ApplicationContext context = mock(ApplicationContext.class);
        Object bean = new Object();
        when(context.getBean("sampleBean")).thenReturn(bean);
        springUtil.setApplicationContext(context);

        assertSame(bean, SpringUtil.getBean("sampleBean"));
    }

    @Test
    void getBean_byNameAndType_delegatesToContext() {
        ApplicationContext context = mock(ApplicationContext.class);
        SampleBean bean = new SampleBean();
        when(context.getBean("sampleBean", SampleBean.class)).thenReturn(bean);
        springUtil.setApplicationContext(context);

        assertSame(bean, SpringUtil.getBean("sampleBean", SampleBean.class));
    }

    @Test
    void getBean_byType_delegatesToContext() {
        ApplicationContext context = mock(ApplicationContext.class);
        SampleBean bean = new SampleBean();
        when(context.getBean(SampleBean.class)).thenReturn(bean);
        springUtil.setApplicationContext(context);

        assertSame(bean, SpringUtil.getBean(SampleBean.class));
    }
}
