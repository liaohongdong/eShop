package org.eu.liaohongdong.security.util;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JwtTokenUtil 的单元测试。
 * secret/expiration/tokenHead 用反射注入，token 用相同密钥在测试内自行签发，
 * 以便构造“已过期 / 刚刷新 / 超窗口”等场景。
 */
class JwtTokenUtilTest {

    private static final String SECRET = "eShop-test-secret-0123456789";
    private static final long EXPIRATION = 3600L;
    private static final String TOKEN_HEAD = "Bearer ";
    private static final byte[] KEY = SECRET.getBytes(StandardCharsets.UTF_8);
    private static final int REFRESH_WINDOW_SECONDS = 30 * 60;

    private JwtTokenUtil jwtTokenUtil;

    @BeforeEach
    void setUp() throws Exception {
        jwtTokenUtil = new JwtTokenUtil();
        setField("secret", SECRET);
        setField("expiration", EXPIRATION);
        setField("tokenHead", TOKEN_HEAD);
    }

    // ---------------- 辅助方法 ----------------

    private void setField(String name, Object value) throws Exception {
        Field field = JwtTokenUtil.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(jwtTokenUtil, value);
    }

    private String invokeGenerateToken(Map<String, Object> claims) throws Exception {
        Method method = JwtTokenUtil.class.getDeclaredMethod("generateToken", Map.class);
        method.setAccessible(true);
        return (String) method.invoke(jwtTokenUtil, claims);
    }

    private byte[] invokeGetSignature() throws Exception {
        Method method = JwtTokenUtil.class.getDeclaredMethod("getSignature");
        method.setAccessible(true);
        return (byte[]) method.invoke(jwtTokenUtil);
    }

    private boolean invokeIsTokenExpired(String token) throws Exception {
        Method method = JwtTokenUtil.class.getDeclaredMethod("isTokenExpired", String.class);
        method.setAccessible(true);
        return (boolean) method.invoke(jwtTokenUtil, token);
    }

    private Date invokeGetExpiredDateFromToken(String token) throws Exception {
        Method method = JwtTokenUtil.class.getDeclaredMethod("getExpiredDateFromToken", String.class);
        method.setAccessible(true);
        return (Date) method.invoke(jwtTokenUtil, token);
    }

    private boolean invokeTokenRefreshJustBefore(String token, int time) throws Exception {
        Method method = JwtTokenUtil.class.getDeclaredMethod("tokenRefreshJustBefore", String.class, int.class);
        method.setAccessible(true);
        return (boolean) method.invoke(jwtTokenUtil, token, time);
    }

    /** 用相同密钥签发一个可指定 sub / created / exp 的 token */
    private String craftToken(String username, Long createdMillis, Long expMillis) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", username);
        claims.put("created", createdMillis);
        claims.put("exp", expMillis);
        return JWTUtil.createToken(claims, KEY);
    }

    private UserDetails user(String username) {
        return new User(username, "pwd", List.of());
    }

    private static long now() {
        return System.currentTimeMillis();
    }

    private static long future() {
        return now() + EXPIRATION * 1000;
    }

    private static String tamper(String token) {
        int index = token.length() / 2;
        char replacement = token.charAt(index) == 'A' ? 'B' : 'A';
        return token.substring(0, index) + replacement + token.substring(index + 1);
    }

    // ---------------- getSignature / generateToken(Map) ----------------

    @Test
    void getSignature_returnsSecretUtf8Bytes() throws Exception {
        assertArrayEquals(SECRET.getBytes(StandardCharsets.UTF_8), invokeGetSignature());
    }

    @Test
    void generateToken_producesThreePartJwt() throws Exception {
        String token = invokeGenerateToken(new HashMap<>(Map.of("sub", "admin")));

        assertNotNull(token);
        assertEquals(3, token.split("\\.").length, "JWT 应由 header.payload.signature 三部分组成");
    }

    @Test
    void generateToken_canBeVerifiedWithSecret() throws Exception {
        String token = invokeGenerateToken(new HashMap<>(Map.of("sub", "admin")));

        assertTrue(JWTUtil.verify(token, invokeGetSignature()));
    }

    @Test
    void generateToken_cannotBeVerifiedWithWrongSecret() throws Exception {
        String token = invokeGenerateToken(new HashMap<>(Map.of("sub", "admin")));

        assertFalse(JWTUtil.verify(token, "wrong-secret".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void generateToken_keepsOriginalClaims() throws Exception {
        String token = invokeGenerateToken(new HashMap<>(Map.of("sub", "admin")));

        JWT jwt = JWTUtil.parseToken(token);
        assertEquals("admin", jwt.getPayload("sub"));
    }

    @Test
    void generateToken_setsExpireTimeFromExpiration() throws Exception {
        long lower = System.currentTimeMillis() + EXPIRATION * 1000;
        String token = invokeGenerateToken(new HashMap<>());
        long upper = System.currentTimeMillis() + EXPIRATION * 1000;

        Object exp = JWTUtil.parseToken(token).getPayload("exp");
        assertNotNull(exp, "应写入 exp 声明");
        long expMillis = ((Number) exp).longValue();
        assertTrue(expMillis >= lower && expMillis <= upper,
                "exp 应为当前时间 + expiration 秒（毫秒值）");
    }

    // ---------------- generateToken(UserDetails) ----------------

    @Test
    void generateTokenFromUserDetails_containsSubCreatedAndExp() {
        String token = jwtTokenUtil.generateToken(user("admin"));

        assertNotNull(token);
        assertEquals("admin", jwtTokenUtil.getUsernameFromToken(token));
        Map<String, Object> payload = JWTUtil.parseToken(token).getPayloads();
        assertNotNull(payload.get("created"), "应写入 created");
        assertNotNull(payload.get("exp"), "应写入 exp");
    }

    // ---------------- getUsernameFromToken ----------------

    @Test
    void getUsernameFromToken_returnsUsername() {
        String token = craftToken("admin", now(), future());

        assertEquals("admin", jwtTokenUtil.getUsernameFromToken(token));
    }

    @Test
    void getUsernameFromToken_returnsNullForTamperedToken() {
        String token = craftToken("admin", now(), future());

        assertNull(jwtTokenUtil.getUsernameFromToken(tamper(token)));
    }

    @Test
    void getUsernameFromToken_returnsNullForGarbageToken() {
        assertNull(jwtTokenUtil.getUsernameFromToken("not-a-jwt"));
    }

    // ---------------- isTokenExpired ----------------

    @Test
    void isTokenExpired_falseWhenExpInFuture() throws Exception {
        assertFalse(invokeIsTokenExpired(craftToken("admin", now(), future())));
    }

    @Test
    void isTokenExpired_trueWhenExpInPast() throws Exception {
        assertTrue(invokeIsTokenExpired(craftToken("admin", now(), now() - 1000)));
    }

    @Test
    void isTokenExpired_trueForInvalidToken() throws Exception {
        assertTrue(invokeIsTokenExpired("not-a-jwt"));
    }

    // ---------------- getExpiredDateFromToken ----------------

    @Test
    void getExpiredDateFromToken_returnsDateFromExpClaim() throws Exception {
        long exp = future();

        Date expired = invokeGetExpiredDateFromToken(craftToken("admin", now(), exp));

        assertNotNull(expired);
        assertEquals(exp, expired.getTime());
    }

    @Test
    void getExpiredDateFromToken_returnsNullForInvalidToken() throws Exception {
        assertNull(invokeGetExpiredDateFromToken("not-a-jwt"));
    }

    // ---------------- validateToken ----------------

    @Test
    void validateToken_trueForMatchingUserAndNotExpired() {
        String token = craftToken("admin", now(), future());

        assertTrue(jwtTokenUtil.validateToken(token, user("admin")));
    }

    @Test
    void validateToken_falseForDifferentUsername() {
        String token = craftToken("admin", now(), future());

        assertFalse(jwtTokenUtil.validateToken(token, user("someone-else")));
    }

    @Test
    void validateToken_falseWhenExpired() {
        String token = craftToken("admin", now(), now() - 1000);

        assertFalse(jwtTokenUtil.validateToken(token, user("admin")));
    }

    @Test
    void validateToken_falseForInvalidToken() {
        assertFalse(jwtTokenUtil.validateToken("not-a-jwt", user("admin")));
    }

    // ---------------- refreshHeadToken ----------------

    @Test
    void refreshHeadToken_returnsNullForEmpty() {
        assertNull(jwtTokenUtil.refreshHeadToken(null));
        assertNull(jwtTokenUtil.refreshHeadToken(""));
    }

    @Test
    void refreshHeadToken_returnsNullForInvalidToken() {
        assertNull(jwtTokenUtil.refreshHeadToken(TOKEN_HEAD + "not-a-jwt"));
    }

    @Test
    void refreshHeadToken_returnsNullWhenExpired() {
        String token = craftToken("admin", now(), now() - 1000);

        assertNull(jwtTokenUtil.refreshHeadToken(TOKEN_HEAD + token));
    }

    @Test
    void refreshHeadToken_returnsOriginalWhenCreatedWithinWindow() {
        String token = craftToken("admin", now(), future());

        assertEquals(token, jwtTokenUtil.refreshHeadToken(TOKEN_HEAD + token));
    }

    @Test
    void refreshHeadToken_generatesNewTokenWhenCreatedOutsideWindow() throws Exception {
        String token = craftToken("admin", now() - 31 * 60 * 1000L, future());

        String refreshed = jwtTokenUtil.refreshHeadToken(TOKEN_HEAD + token);

        assertNotNull(refreshed);
        assertNotEquals(token, refreshed, "created 超过 30 分钟应重新签发");
        assertEquals("admin", jwtTokenUtil.getUsernameFromToken(refreshed));
        assertTrue(invokeTokenRefreshJustBefore(refreshed, REFRESH_WINDOW_SECONDS),
                "新 token 的 created 应为当前时间");
    }

    // ---------------- tokenRefreshJustBefore ----------------

    @Test
    void tokenRefreshJustBefore_trueWhenCreatedWithinWindow() throws Exception {
        String token = craftToken("admin", now(), future());

        assertTrue(invokeTokenRefreshJustBefore(token, REFRESH_WINDOW_SECONDS));
    }

    @Test
    void tokenRefreshJustBefore_falseWhenCreatedOutsideWindow() throws Exception {
        String token = craftToken("admin", now() - 31 * 60 * 1000L, future());

        assertFalse(invokeTokenRefreshJustBefore(token, REFRESH_WINDOW_SECONDS));
    }

    @Test
    void tokenRefreshJustBefore_falseForInvalidToken() throws Exception {
        assertFalse(invokeTokenRefreshJustBefore("not-a-jwt", REFRESH_WINDOW_SECONDS));
    }
}
