package org.eu.liaohongdong.security.component;

import jakarta.servlet.http.HttpServletRequest;
import org.eu.liaohongdong.security.config.IgnoreUrlsConfig;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.util.AntPathMatcher;

import java.util.Collection;
import java.util.function.Supplier;

public class DynamicAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    @Autowired
    private DynamicSecurityService dynamicSecurityService;
    @Autowired
    private IgnoreUrlsConfig ignoreUrlsConfig;
    AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authentication, RequestAuthorizationContext context) {
        HttpServletRequest request = context.getRequest();
        String path = request.getRequestURI();
        //白名单直接放行（静态资源、登录接口等）
        for (String url : ignoreUrlsConfig.getUrls()) {
            if (antPathMatcher.match(url, path)) {
                return new AuthorizationDecision(true);
            }
        }
        // 对应跨域的预检请求直接放行
        if(request.getMethod().equals(HttpMethod.OPTIONS.name())){
            return new AuthorizationDecision(true);
        }
        // 从数据库查询 当前url需要的权限标识
        Collection<String> needAuthorityList = dynamicSecurityService.loadResourceByUrl(path);
        // 数据库无该url权限配置，默认拒绝访问
        if (needAuthorityList == null || needAuthorityList.isEmpty()) {
            return new AuthorizationDecision(false);
        }
        // 获取当前登录用户的权限
        Authentication currectAuth = authentication.get();
        Collection<? extends GrantedAuthority> authorities = currectAuth.getAuthorities();
        // 匹配 用户只要拥有其中任意一个权限即可访问
        boolean hasMatch = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(needAuthorityList::contains);
        return new AuthorizationDecision(hasMatch);
    }

}
