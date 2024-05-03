

package com.lyl.shortlink.admin.common.biz.user;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Enumeration;


//@RequiredArgsConstructor
//public class UserTransmitFilter implements Filter {
//
//    private final StringRedisTemplate stringRedisTemplate;
//
//    private static final List<String> IGNORE_URI = Lists.newArrayList(
//            "/api/short-link/admin/v1/user/login",
//            "/api/short-link/admin/v1/user/has-username"
////            "/*"
//    );
//
//    @SneakyThrows
//    @Override
//    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException {
//        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
//        String requestURI = httpServletRequest.getRequestURI();
//        String regex = "^/[a-zA-Z0-9\\s\\S]*$";
//
//        if (!IGNORE_URI.contains(requestURI) /*&& !Pattern.compile(regex).matcher(requestURI).matches()*/) {
//            String method = httpServletRequest.getMethod();
//            if (!(Objects.equals(requestURI, "/api/short-link/admin/v1/user") && Objects.equals(method, "POST"))) {
//                String username = httpServletRequest.getHeader("username");
//                String token = httpServletRequest.getHeader("token");
//                if (!StrUtil.isAllNotBlank(username, token)) {
//                    returnJson((HttpServletResponse) servletResponse, JSON.toJSONString(Results.failure(new ClientException(USER_TOKEN_FAIL))));
//                    return;
//                }
//                Object userInfoJsonStr;
//                try {
//                    userInfoJsonStr = stringRedisTemplate.opsForHash().get(USER_LOGIN_KEY + username, token);
//                    if (userInfoJsonStr == null) {
//                        throw new ClientException(USER_TOKEN_FAIL);
//                    }
//                } catch (Exception ex) {
//                    returnJson((HttpServletResponse) servletResponse, JSON.toJSONString(Results.failure(new ClientException(USER_TOKEN_FAIL))));
//                    return;
//                }
//                UserInfoDTO userInfoDTO = JSON.parseObject(userInfoJsonStr.toString(), UserInfoDTO.class);
//                UserContext.setUser(userInfoDTO);
//            }
//        }
//        try {
//            filterChain.doFilter(servletRequest, servletResponse);
//        } finally {
//            UserContext.removeUser();
//        }
//    }
//
//    private void returnJson(HttpServletResponse response, String json) throws Exception {
//        PrintWriter writer = null;
//        response.setCharacterEncoding("UTF-8");
//        response.setContentType("text/html; charset=utf-8");
//        try {
//            writer = response.getWriter();
//            writer.print(json);
//
//        } catch (IOException e) {
//        } finally {
//            if (writer != null)
//                writer.close();
//        }
//    }
//}


/**
 * 用户信息传输过滤器
 * 公众号：马丁玩编程，回复：加群，添加马哥微信（备注：link）获取项目资料
 */
@Slf4j
@RequiredArgsConstructor
public class UserTransmitFilter implements Filter {

//    private final StringRedisTemplate stringRedisTemplate;
    private final String secretKey = "123456";

    @SneakyThrows
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

//        String secretKeyFromRequest = httpServletRequest.getHeader("secretKey");
//        if (!Objects.equals(secretKeyFromRequest, secretKey)) {
//            // 设置响应状态码为401（未授权）
//            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//
//            // 设置响应的内容类型为JSON
//            httpServletResponse.setContentType("application/json;charset=UTF-8");
//
//            // 输出响应内容
//            PrintWriter writer = httpServletResponse.getWriter();
//            writer.print("{ \"message\": \"未授权访问\" }");
//            writer.flush();
//            writer.close();
//
//            // 避免继续执行过滤链中的其他过滤器和请求处理
//            return;
//        }
        // 打印请求url和请求方法
        log.debug("request url: {}, method: {}", httpServletRequest.getRequestURL(), httpServletRequest.getMethod());
        String username = httpServletRequest.getHeader("username");
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = httpServletRequest.getHeader(headerName);
            System.out.println("Header Name: " + headerName + ", Value: " + headerValue);
        }
//        String token = httpServletRequest.getHeader("token");
//        Object object = stringRedisTemplate.opsForHash().get(USER_LOGIN_KEY + username, token);
//        if(null != object){
//            UserInfoDTO userInfoDTO = JSON.parseObject(object.toString(), UserInfoDTO.class);
//            UserContext.setUser(userInfoDTO);
//        }
        if (StrUtil.isNotBlank(username)) {
            String userId = httpServletRequest.getHeader("userId");
            String realName = httpServletRequest.getHeader("realName");
            UserInfoDTO userInfoDTO = new UserInfoDTO(userId, username, realName);
            UserContext.setUser(userInfoDTO);
        }
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.removeUser();
        }
    }
}