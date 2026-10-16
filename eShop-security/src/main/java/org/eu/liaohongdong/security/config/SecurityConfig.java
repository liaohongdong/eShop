package org.eu.liaohongdong.security.config;

import org.eu.liaohongdong.security.component.DynamicAuthorizationManager;
import org.eu.liaohongdong.security.component.JwtAuthenticationTokenFilter;
import org.eu.liaohongdong.security.component.RestAuthenticationEntryPoint;
import org.eu.liaohongdong.security.component.RestfulAccessDeniedHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// SpringSecurity相关配置，仅用于配置SecurityFilterChain
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private IgnoreUrlsConfig ignoreUrlsConfig;
    @Autowired
    private RestfulAccessDeniedHandler restfulAccessDeniedHandler;
    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    @Autowired(required = false)
    private DynamicAuthorizationManager dynamicAuthorizationManager;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(registry -> {
                    // 不需要保护的资源路径允许访问
                    for (String url : ignoreUrlsConfig.getUrls()) {
                        registry.requestMatchers(url).permitAll();
                    }
                    // 允许跨域请求的OPTIONS请求
                    registry.requestMatchers(HttpMethod.OPTIONS).permitAll();
                })
                .authorizeHttpRequests(registry -> {
                    if (dynamicAuthorizationManager != null) {
                        // 启用动态数据库URL权限，全部请求进入自定义AuthorizationManager校验
                        registry.anyRequest().access(dynamicAuthorizationManager);
                    } else {
                        // 没有动态权限时，兜底配置
                        registry.anyRequest().authenticated();
                    }
                })
                //关闭跨站请求防护
                .csrf(AbstractHttpConfigurer::disable)
                //修改Session生成策略为无状态会话
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                //自定义权限拒绝处理类
                .exceptionHandling(ex -> ex.accessDeniedHandler(restfulAccessDeniedHandler).authenticationEntryPoint(restAuthenticationEntryPoint));
        //JWT过滤器放在账号密码认证过滤器前面
        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
