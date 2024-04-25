

package com.lyl.shortlink.admin.common.biz.user;

import com.alibaba.fastjson2.JSON;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Enumeration;

import static com.lyl.shortlink.admin.common.constants.RedisCacheConstant.USER_LOGIN_KEY;

/**
 * 用户信息传输过滤器
 * 公众号：马丁玩编程，回复：加群，添加马哥微信（备注：link）获取项目资料
 */
@Slf4j
@RequiredArgsConstructor
public class UserTransmitFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;

    @SneakyThrows
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        // 打印请求url和请求方法
        log.debug("request url: {}, method: {}", httpServletRequest.getRequestURL(), httpServletRequest.getMethod());
        String username = httpServletRequest.getHeader("username");
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = httpServletRequest.getHeader(headerName);
            System.out.println("Header Name: " + headerName + ", Value: " + headerValue);
        }
        String token = httpServletRequest.getHeader("token");
        Object object = stringRedisTemplate.opsForHash().get(USER_LOGIN_KEY + username, token);
        if(null != object){
            UserInfoDTO userInfoDTO = JSON.parseObject(object.toString(), UserInfoDTO.class);
            UserContext.setUser(userInfoDTO);
        }
//        if (StrUtil.isNotBlank(username)) {
//            String userId = httpServletRequest.getHeader("userId");
//            String realName = httpServletRequest.getHeader("realName");
//            UserInfoDTO userInfoDTO = new UserInfoDTO(userId, username, realName);
//            UserContext.setUser(userInfoDTO);
//        }
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.removeUser();
        }
    }
}