package org.eu.liaohongdong.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RequestUtilTest {

    private HttpServletRequest request() {
        return mock(HttpServletRequest.class);
    }

    @Test
    void usesXForwardedForWhenPresent() {
        HttpServletRequest request = request();
        when(request.getHeader("x-forwarded-for")).thenReturn("192.168.1.1");

        assertEquals("192.168.1.1", RequestUtil.getRequestIp(request));
    }

    @Test
    void fallsBackToProxyClientIpWhenXForwardedForMissing() {
        HttpServletRequest request = request();
        when(request.getHeader("x-forwarded-for")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn("10.0.0.1");

        assertEquals("10.0.0.1", RequestUtil.getRequestIp(request));
    }

    @Test
    void fallsBackToWlProxyClientIpWhenPreviousHeadersMissing() {
        HttpServletRequest request = request();
        when(request.getHeader("x-forwarded-for")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn("10.0.0.2");

        assertEquals("10.0.0.2", RequestUtil.getRequestIp(request));
    }

    @Test
    void fallsBackToRemoteAddrWhenAllHeadersMissing() {
        HttpServletRequest request = request();
        when(request.getHeader("x-forwarded-for")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("203.0.113.5");

        assertEquals("203.0.113.5", RequestUtil.getRequestIp(request));
    }

    @Test
    void treatsUnknownXForwardedForAsMissingCaseInsensitively() {
        HttpServletRequest request = request();
        when(request.getHeader("x-forwarded-for")).thenReturn("UNKNOWN");
        when(request.getHeader("Proxy-Client-IP")).thenReturn("10.0.0.3");

        assertEquals("10.0.0.3", RequestUtil.getRequestIp(request));
    }

    @Test
    void treatsEmptyXForwardedForAsMissing() {
        HttpServletRequest request = request();
        when(request.getHeader("x-forwarded-for")).thenReturn("");
        when(request.getHeader("Proxy-Client-IP")).thenReturn("10.0.0.4");

        assertEquals("10.0.0.4", RequestUtil.getRequestIp(request));
    }

    @Test
    void treatsUnknownProxyClientIpAsMissing() {
        HttpServletRequest request = request();
        when(request.getHeader("x-forwarded-for")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn("unknown");
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn("10.0.0.5");

        assertEquals("10.0.0.5", RequestUtil.getRequestIp(request));
    }

    @Test
    void extractsFirstIpFromCommaSeparatedList() {
        HttpServletRequest request = request();
        when(request.getHeader("x-forwarded-for")).thenReturn("192.168.1.100, 10.0.0.1, 10.0.0.2");

        assertEquals("192.168.1.100", RequestUtil.getRequestIp(request));
    }

    @Test
    void extractsFirstIpWhenSeparatorHasNoSpace() {
        HttpServletRequest request = request();
        when(request.getHeader("x-forwarded-for")).thenReturn("192.168.1.100,10.0.0.1");

        assertEquals("192.168.1.100", RequestUtil.getRequestIp(request));
    }

    @Test
    void keepsLongValueWithoutCommaIntact() {
        HttpServletRequest request = request();
        when(request.getHeader("x-forwarded-for")).thenReturn("abcdefghijklmnop");

        assertEquals("abcdefghijklmnop", RequestUtil.getRequestIp(request));
    }

    @Test
    void keepsShortValueIntactWithoutSplitting() {
        HttpServletRequest request = request();
        when(request.getHeader("x-forwarded-for")).thenReturn("10.0.0.1");

        assertEquals("10.0.0.1", RequestUtil.getRequestIp(request));
    }

    @Test
    void resolvesLocalhostRemoteAddrToHostAddress() throws UnknownHostException {
        HttpServletRequest request = request();
        when(request.getHeader("x-forwarded-for")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        String result = RequestUtil.getRequestIp(request);

        assertNotNull(result);
        assertEquals(InetAddress.getLocalHost().getHostAddress(), result);
    }

    @Test
    void resolvesIpv6LocalhostRemoteAddrToHostAddress() throws UnknownHostException {
        HttpServletRequest request = request();
        when(request.getHeader("x-forwarded-for")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("0:0:0:0:0:0:0:1");

        String result = RequestUtil.getRequestIp(request);

        assertNotNull(result);
        assertEquals(InetAddress.getLocalHost().getHostAddress(), result);
    }
}
