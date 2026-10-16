package org.eu.liaohongdong.security.component;

import java.util.Collection;

// 动态权限相关业务接口
public interface DynamicSecurityService {
    // 根据URL查询访问该资源需要的权限集合
    Collection<String> loadResourceByUrl(String url);
}
