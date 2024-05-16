package com.lyl.shortlink.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TrueIPAddFilterGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

//    public static String getActualIp(HttpServletRequest request) {
//        String ipAddress = request.getHeader("X-Forwarded-For");
//        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
//            ipAddress = request.getHeader("Proxy-Client-IP");
//        }
//        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
//            ipAddress = request.getHeader("WL-Proxy-Client-IP");
//        }
//        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
//            ipAddress = request.getHeader("HTTP_CLIENT_IP");
//        }
//        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
//            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
//        }
//        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
//            ipAddress = request.getRemoteAddr();
//        }
//        log.info("用户访问IP地址：{}", ipAddress);
//        return ipAddress;
//    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            String ipAddress = String.valueOf(exchange.getRequest().getRemoteAddress().getAddress());
            // 如果ipAddress以/开头，去掉/
            String finalIpAddress = ipAddress.startsWith("/")? ipAddress.substring(1): ipAddress;
            // 将真实IP地址放入请求头中
            exchange.getRequest().mutate()
                    .headers(httpHeaders -> httpHeaders.add("X-Real-IP", finalIpAddress));

            log.info("用户访问IP地址：{}", finalIpAddress);
            return chain.filter(exchange);
        };
    }
}
